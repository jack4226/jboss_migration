package jpa.service.msgout;

import java.util.Date;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import jpa.constant.MailServerType;
import jpa.model.SmtpServer;
import jpa.util.EmailAddrUtil;
import jpa.util.ExceptionUtil;

import org.apache.log4j.Logger;

/** 
  * SmtpConnection initializes a SMTP connection and provides methods to send 
  * email off.
  * <pre>
  * if send failed to some addresses, the addresses are stored in a map
  * object:
  *		invalid 	addresses - map.put("invalid",addresses);
  *		validUnsent addresses - map.put("validUnsent",addresses);
  *		validSent   addresses - map.put("validSent", addresses)
  * </pre>
 */
public final class SmtpConnection implements java.io.Serializable {
	private static final long serialVersionUID = -2482207330299000973L;
	static final Logger logger = Logger.getLogger(SmtpConnection.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private final SmtpServer smtpConnVo;

	private final String smtpHost, userId, password, serverType;
	private final int smtpPort;
	private final MailServerType protocol;

	private javax.mail.Session session = null;
	private Transport transport = null;
	private boolean persistent = false;

	static final int[] retryFreqArraySmtp = { 10, 20, 30, 60, 60, 60, 60 }; // in seconds
	static final int[] retryFreqArrayExch = { 10, 20, 30, 60, 60, 60, 120, 120, 120 }; // in seconds
	private final int[] retryFreqArray;
	private int totalRetries = 0; // infinite retries if less than zero
	private int retryFreq = 0;
	private long time_stopped_total = 0;
	private int time_stopped_session = 0;
	private int TIME_TO_ISSUE_ALERT = 0;

	/**
	 * create a SmtpConnection instance
	 * 
	 * @param _props -
	 *            smtp server properties
	 */
	public SmtpConnection(SmtpServer vo) {
		this.smtpConnVo = vo;

		// retrieve server properties
		smtpHost = vo.getSmtpHostName();
		smtpPort = vo.getSmtpPortNumber();
		
		userId = vo.getUserId();
		password = vo.getUserPswd();
		serverType = vo.getServerType();

		// enable RFC2231 support in parameter lists, since javamail 1.4
		// Since very few programs support RFC2231, disable it for now
		/*
		System.setProperty("mail.mime.encodeparameters", "true");
		System.setProperty("mail.mime.decodeparameters", "true");
		System.setProperty("mail.mime.encodefilename", "true");
		System.setProperty("mail.mime.decodefilename", "true");
		*/
		
		Properties sys_props = (Properties) System.getProperties().clone();
		if (smtpHost != null) {
			sys_props.put("mail.smtp.host", smtpHost);
		}
		if (vo.getIsUseSsl()) {
			sys_props.put("mail.smtps.auth", "true");
			sys_props.put("mail.user", userId);
			protocol = MailServerType.SMTPS;
		}
		else {
			if (vo.getIsUseAuth()!=null && vo.getIsUseAuth()) {
				sys_props.put("mail.smtp.auth", "true");
			}
			protocol = MailServerType.SMTP;
		}

		// javamail set the default mail host to localhost, however a 
		// windows box does not have a SMTP server installed.
		sys_props.put("mail.host", vo.getSmtpHostName());

		// properties of com.sun.mail.smtp 
		sys_props.put("mail.smtp.connectiontimeout", String.valueOf(3 * 60 * 1000));
			// socket connection timeout value in milliseconds
		sys_props.put("mail.smtp.timeout", String.valueOf(5 * 60 * 1000));
			// socket i/o timeout value in milliseconds
		
		// Certain IMAP servers do not implement the IMAP Partial FETCH
		// functionality properly
		// set Partial fetch to false to workaround exchange server 5.5 bug
		sys_props.put("mail.smtp.sendpartial", "false");

		// mail.smtp.dsn.notify
		// mail.smtp.dsn.ret
		
		// Get a Session object
		session = Session.getInstance(sys_props);
		if (isDebugEnabled)
			session.setDebug(true);

		int extraRetryTime = 0;
		extraRetryTime = vo.getMaximumRetries();
		retryFreq = vo.getRetryFrequence();
		TIME_TO_ISSUE_ALERT = vo.getAlertAfter() == null ? 15 : vo.getAlertAfter();
		
		if (retryFreq <= 0) {
			retryFreq = 5; // default
		}
		if (retryFreq > 30) {
			retryFreq = 30; // up to 30 minutes
		}
		logger.info("Extra SMTP retries to be attempted for " + smtpHost + ": " + extraRetryTime
				+ " minutes, freq=" + retryFreq);

		if (TIME_TO_ISSUE_ALERT < 5) {
			TIME_TO_ISSUE_ALERT = 5; // at least 5 minutes
		}
		TIME_TO_ISSUE_ALERT *= 60;

		if (MailServerType.EXCHANGE.equals(serverType)) {
			retryFreqArray = retryFreqArrayExch;
		}
		else {
			retryFreqArray = retryFreqArraySmtp;
		}
		int default_retry_time = 0;
		for (int i = 0; i < retryFreqArray.length; i++) {
			default_retry_time += retryFreqArray[i];
		}

		totalRetries = retryFreqArray.length;

		if (extraRetryTime >= 0) {
			totalRetries += Math.round((float)extraRetryTime / (float)retryFreq);
			logger.info(smtpHost + " - will retry for up to "
					+ (default_retry_time / 60 + extraRetryTime) + " minutes.");
			logger.info(smtpHost + " - maximum retry attempts " + totalRetries);
		}
		else {
			totalRetries = -1;
			logger.info(smtpHost + " - will retry forever.");
		}

		/*
		 * to use persistent SMTP connection, default is yes and need to handle
		 * connection time out in sendMail method:
		 *  - MessagingException is thrown during transport.sendMessage() if the
		 * connection is dead or not in the connected state (make sure to
		 * exclude SendFailedException) - reissue transport.connect() and re-send
		 * the message
		 */
		if (vo.getIsPersistence()) {
			persistent = true;
		}
		logger.info("Persistent SMTP Connection used for " + smtpHost + "? " + persistent);
		if (isDebugEnabled)
			logger.debug("Smtp Properties:" + vo);
	}
	
	/**
	 * create a SmtpConnection instance
	 * 
	 * @param _props -
	 *            smtp server properties
	 */
	public SmtpConnection(Properties _props) {
		this(propertiesToVo(_props));
	}

	static SmtpServer propertiesToVo(Properties props) {
		SmtpServer vo = new SmtpServer();
		
		vo.setSmtpHostName(props.getProperty("smtphost"));

		int port = -1;
		try {
			port = Integer.parseInt(props.getProperty("smtpport", "-1"));
		}
		catch (NumberFormatException e) {
			logger.warn("NumberFormatException caught: " + e.getMessage());
			port = -1; // default
		}
		finally {
			vo.setSmtpPortNumber(port);
		}
		
		vo.setServerName(props.getProperty("server_name", vo.getSmtpHostName()));
		vo.setUserId(props.getProperty("userid"));
		vo.setUserPswd(props.getProperty("password"));
		String server_type = props.getProperty("server_type", MailServerType.SMTP.value());
		vo.setServerType(server_type);
		vo.setIsUseSsl("yes".equalsIgnoreCase(props.getProperty("use_ssl")));
		vo.setIsPersistence("yes".equalsIgnoreCase(props.getProperty("persistence")));
		
		try {
			vo.setNumberOfThreads(Integer.parseInt(props.getProperty("threads", "2")));
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
		}
		
		try {
			vo.setMessageCount(Integer.parseInt(props.getProperty("message_count", "0")));
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
		}
		
		try {
			vo.setMaximumRetries(Integer.parseInt(props.getProperty("retry", "0")));
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
		}
		
		try {
			vo.setRetryFrequence(Integer.parseInt(props.getProperty("freq", "5")));
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
		}
		
		try {
			vo.setAlertAfter(Integer.parseInt(props.getProperty("alert_after", "15")));
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
		}
		vo.setAlertLevel(props.getProperty("alert_level", "fatal"));
		
		return vo;
	}

	/**
	 * sendMail method to actually send an email off via a SMTP server.
	 * 
	 * @param msg -
	 *            Message object
	 * @param errors -
	 *            a map object containing errors if Exception is thrown
	 * @throws SmtpException
	 * @throws MessagingException
	 */
	public void sendMail(Message msg, Map<String, Address[]> errors) throws SmtpException,
			MessagingException {
		try {
			// use a Transport instance to send message via a specified SMTP
			// server
			if (isDebugEnabled) {
				logger.debug("sendMail() - Message Content Type: "
						+ ((MimeMessage) msg).getContentType());
			}
			// get the SMTP transport
			obtainTransport(0); // with retry logic
			msg.saveChanges();
			// send the thing off
			transport.sendMessage(msg, msg.getAllRecipients());
			logger.info("sendMail() - Mail from: " + EmailAddrUtil.addressToString(msg.getFrom(), false)
					+ " was sent to: " + EmailAddrUtil.addressToString(msg.getAllRecipients(), false)
					+ ", via " + smtpHost + ", " + new Date());
		}
		catch (SendFailedException sfex) {
			logger.error("SendFailedException caught during send: (" + smtpHost + ") ", sfex);
			getUnsentAddresses(sfex, errors);
			if (ExceptionUtil.findException(sfex, java.io.UnsupportedEncodingException.class) == null) {
				// Not UnsupportedEncodingException, re-throw the SendFailedException back to caller
				throw sfex;
			}
		}
		catch (MessagingException mex) {
			logger.error("MessagingException caught during send: (" + smtpHost + ")", mex);
			Exception nexte = mex.getNextException();
			if (// Broken Pipe - iPlanet
				(nexte != null && nexte instanceof java.io.IOException)
				// old ms exchange server error
				|| (mex.getMessage() == null || mex.getMessage().length() == 0)
				// exchange server time out
				|| (mex.getMessage() != null && mex.getMessage().indexOf("Timeout") >= 0)
				// exchange server error: 421 Too many errors on this
				// connection---closing
				|| (mex.getMessage() != null && mex.getMessage().indexOf("421 Too many errors") >= 0)) {
				
				// connection timed out
				logger.info("sendMail() - connection timed out. Retry... (" + smtpHost + ")");
				obtainTransport(0, true); // with retry logic
				try {
					transport.sendMessage(msg, msg.getAllRecipients());
					logger.info("sendMail() - Retry was successfully, mail sent via " + smtpHost
							+ ", " + new Date());
				}
				catch (SendFailedException sfex) {
					logger.error("SendFailedException caught during resend: (" + smtpHost + ")",
							sfex);
					getUnsentAddresses(sfex, errors);
					// re-throw the SendFailedException back to caller
					throw sfex;
				}
			}
			else {
				// throw SmtpException back to caller
				throw new SmtpException("MessagingException caught during send mail, "
						+ mex.getMessage());
			}
		}
		finally {
			if (!persistent) {
				try {
					if (transport != null) {
						if (isDebugEnabled)
							logger.debug("sendMail() - closing transport...");
						transport.close();
					}
				}
				catch (MessagingException e) {
					logger.warn("MessagingException caught: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Obtain a transport and close it.
	 * 
	 * @throws MessagingException
	 */
	public void testConnection() throws MessagingException {
		testConnection(false);
	}

	/**
	 * Obtain a new transport and close it.
	 * 
	 * @param forceConnect -
	 *            force reconnect
	 * @throws MessagingException
	 */
	public void testConnection(boolean forceConnect) throws MessagingException {
		try {
			obtainTransport(0, forceConnect);
		}
		finally {
			if (transport != null)
				transport.close();
		}
	}

	/**
	 * retrieve unsent addresses from SendFailedException
	 * 
	 * @param sfex -
	 *            SendFailedException
	 */
	void getUnsentAddresses(SendFailedException sfex, Map<String, Address[]> errors) {
		logger.error("getUnsentAddresses() - SendFailedException passed: " + sfex);

		Address[] invalid = sfex.getInvalidAddresses();
		// save invalid addresses
		if (invalid != null) {
			logger.error("    ** Invalid Addresses");
			for (int i = 0; i < invalid.length; i++) {
				logger.error("         " + invalid[i]);
			}
			errors.put("invalid", invalid);
		}
		// save valid unsent addresses
		Address[] validUnsent = sfex.getValidUnsentAddresses();
		if (validUnsent != null) {
			logger.error("    ** ValidUnsent Addresses");
			for (int i = 0; i < validUnsent.length; i++) {
				logger.error("         " + validUnsent[i]);
			}
			errors.put("validUnsent", validUnsent);
		}
		Address[] validSent = sfex.getValidSentAddresses();
		if (validSent != null) {
			logger.error("    ** ValidSent Addresses");
			for (int i = 0; i < validSent.length; i++) {
				logger.error("         " + validSent[i]);
			}
			errors.put("validSent", validSent);
		}
	}

	/**
	 * obtain a transport
	 * 
	 * @param retries -
	 *            number of retries before giving up
	 */
	private void obtainTransport(int retries) throws MessagingException {
		obtainTransport(retries, false);
	}

	/**
	 * obtain a transport
	 * 
	 * @param retries -
	 *            number of retries before giving up
	 * @param forceReconnect -
	 *            force reconnect
	 * @throws MessagingException
	 */
	private void obtainTransport(int retries, boolean forceReconnect) throws MessagingException {
		if (retries == 0) {
			time_stopped_total = 0;
			time_stopped_session = 0;
		}
		// get the SMTP transport
		if (transport == null || !transport.isConnected() || forceReconnect) {
			if (transport != null) { // clean up first
				try {
					transport.close();
				}
				catch (MessagingException e) {
					logger.warn("MessagingException caught: " + e.getMessage());
				}
			}
			// get a transport
			transport = session.getTransport(protocol.value());
			try {
				if (isDebugEnabled)
					logger.debug("Obtaining SMTP Transport... (" + smtpHost + "/" + smtpPort + ")");
				String user_id = userId;
				String pass_wd = password;
				if (smtpPort > 0) {
					transport.connect(smtpHost, smtpPort, user_id, pass_wd);
				}
				else {
					transport.connect(smtpHost, user_id, pass_wd);
				}
			}
			catch (MessagingException e) {
				logger.warn("MessagingException caught: " + e.getMessage());
				String alert_lvl = smtpConnVo.getAlertLevel();
				if ("infor".equalsIgnoreCase(alert_lvl)) {
//					JbMain.getEventAlert().issueExcepAlert(JbMain.SMTP_ALERT,
//							"failed to connect to " + smtpHost + " after " + retries + " retries",
//							e);
				}
				// retry
				if (totalRetries < 0 || retries < totalRetries) {
					int sleep_for = 0;
					if (retries < retryFreqArray.length) {
						sleep_for = retryFreqArray[retries];
					}
					else {
						sleep_for = retryFreq * 60;
					}
					logger.error("Failed to connect to smtp server " + smtpHost + ", retry after "
							+ sleep_for + " seconds.");
					try {
						Thread.sleep(sleep_for * 1000);
					}
					catch (InterruptedException ie) {
						logger.error("ObtainTransport().sleep() was interrupted", ie);
					}
					logger.error("Connecting to " + smtpHost + ", number of retries attempted: "
							+ retries);
					time_stopped_session += sleep_for;
					time_stopped_total += sleep_for;
					retries++;
					if (time_stopped_session >= TIME_TO_ISSUE_ALERT) {
						if (!"nolog".equalsIgnoreCase(alert_lvl)) {
//							JbMain.getEventAlert().issueExcepAlert(
//									JbMain.SMTP_ALERT,
//									"Couldn't connect to smtp for " + time_stopped_total
//											+ " seconds, target host " + smtpHost, e);
							logger.warn("Couldn't connect to smtp for " + time_stopped_total
									+ " seconds, target host " + smtpHost, e);
						}
						time_stopped_session = 0;
					}
					obtainTransport(retries, true);
				}
				else {
//					JbMain.getEventAlert().issueFatalAlert(
//							JbMain.SMTP_ALERT,
//							"All smtp retries failed (for " + time_stopped_total
//									+ " seconds), target host " + smtpHost, e);
					logger.warn("All smtp retries failed (for " + time_stopped_total
							+ " seconds), target host " + smtpHost, e);
					throw e;
				}
			}
		}
	}

	public void close() throws MessagingException {
		if (persistent && transport != null) {
			transport.close();
		}
	}

	/**
	 * override finalize method to close transport
	 */
	protected void finalize() throws Throwable {
		try {
			if (transport != null) {
				transport.close();
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught", e);
		}
		super.finalize();
	}

	public SmtpServer getSmtpServer() {
		return smtpConnVo;
	}
}
