package jpa.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.constant.MailProtocol;
import jpa.constant.MailServerType;
import jpa.constant.XHeaderName;

/**
 * A simple class that performs email delivery to SMTP server. It accepts either
 * a MessageBean or a java MimeMessage as its input. For simplicity, it catches
 * Exceptions inside its methods and throws a MessageException.
 */
public class SimpleEmailSender implements java.io.Serializable {
	private static final long serialVersionUID = -4393953612684807498L;
	protected static final Logger logger = LogManager.getLogger(SimpleEmailSender.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();
	
	private final Properties smtpProps;
	
	private final String smtphost;
	private final String smtpport;
	private final String protocol;
	private final String host;
	private final String user;
	private final String password;
	
	private Session session=null;
	private Transport transport=null;
	private final boolean persistent;
	private boolean debugSession = Level.DEBUG.equals(logger.getLevel());
	
	String LF = System.getProperty("line.separator","\n");
	
    /**
	 * Constructor, with smtp server properties as its parameter
	 * 
	 * @throws MessagingException
	 * @throws NumberFormatException
	 */
	public SimpleEmailSender(Properties smtpProps) throws NumberFormatException, MessagingException {
		this.smtpProps = smtpProps;
		smtphost = this.smtpProps.getProperty("smtphost", "localhost");
		smtpport = (String) this.smtpProps.getProperty("smtpport", "25");
		protocol = this.smtpProps.getProperty("protocol", MailProtocol.POP3.value());
		user = this.smtpProps.getProperty("user", "jwang");
		password = this.smtpProps.getProperty("password", "jwang");
		host = this.smtpProps.getProperty("host", smtphost);
		
		persistent = "yes".equalsIgnoreCase(this.smtpProps.getProperty("persistent", "yes"));
		/*	need to handle connection time out in sendMail method:
		
		 - MessagingException is thrown during transport.sendMessage()
		   if the connection is dead or not in the connected state
		   (make sure to exclude SendFailedException)
		 - reissue transport.connect() and re-send the message
		 */
		if (persistent) {
			initTransport();
		}
	}
	
	private void initTransport() throws NumberFormatException, MessagingException {
		Properties props = System.getProperties();
		// could use Session.getTransport() and Transport.connect()
		props.setProperty("mail.smtp.host", smtphost);
		props.setProperty("mail.smtp.port", smtpport);
		props.setProperty("mail.user", user);
		props.setProperty("mail.password", password);
		
		session = Session.getDefaultInstance(props);
		// get the smtp transport
		transport = session.getTransport(MailServerType.SMTP.value());
		transport.connect(smtphost, Integer.parseInt(smtpport), user, password);
	}

	public void sendMessage(MessageBean mBean) throws NumberFormatException, MessagingException {
		if (persistent) {
			sendUsePersistent(mBean);
		}
		else {
			sendUseTransient(mBean);
		}
	}

	public void sendMessage(Message msg) throws NumberFormatException, MessagingException {
		if (persistent) {
			sendUsePersistent(msg);
		}
		else {
			sendUseTransient(msg);
		}
	}

	/** 
	 * This method constructs a message from the MessageBean and sends it out 
	 * @throws MessagingException
	 */
	private void sendUseTransient(Message msg) throws MessagingException {
		if (isDebugEnabled) {
			logger.debug("Entering sendUseTransient method");
		}
		Properties props = System.getProperties();
		if (smtphost != null) {
			props.put("mail.smtp.host", smtphost);
		}
		
		// Get a Session object
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(debugSession);
		msg.setSentDate(new Date());
		// send the thing off
		transportSend(msg, 0);
		if (isDebugEnabled) {
			logger.debug("Mail To " + msg.getRecipients(RecipientType.TO)[0] + " - " + msg.getSubject()
					+ " was sent successfully.");
		}
	}
	
	
	/**
	 * This method constructs a message from the MessageBean and sends it out
	 * 
	 * @param m -
	 *            MessageBean object
	 * @throws MessagingException
	 */
	private void sendUseTransient(MessageBean m) throws MessagingException {
		if (isDebugEnabled) {
			logger.debug("Entering sendUseTransient method");
		}
		Properties props = System.getProperties();
		if (smtphost != null) {
			props.put("mail.smtp.host", smtphost);
		}
		// Get a Session object
		Session session = Session.getDefaultInstance(props, null);
		
		session.setDebug(debugSession);
		// construct the message
		Message msg = new MimeMessage(session);
		if (m.getFrom() != null && m.getFrom().length > 0) {
			msg.setFrom(m.getFrom()[0]);
		}
		else {
			msg.setFrom();
		}
		msg.setRecipients(RecipientType.TO, m.getTo());
		if (m.getCc() != null) {
			msg.setRecipients(RecipientType.CC, m.getCc());
		}
		if (m.getBcc() != null) {
			msg.setRecipients(RecipientType.BCC, m.getBcc());
		}
		msg.setSubject(m.getSubject());
		msg.setText(m.getBody());
		if (m.getXmailer() != null && m.getXmailer().length > 0) {
			msg.setHeader(XHeaderName.MAILER.value(), m.getXmailer()[0]);
		}
		msg.setSentDate(new Date());
		// send the thing off
		transportSend(msg, 0);
		if (isDebugEnabled) {
			logger.debug("Mail TO " + msg.getRecipients(RecipientType.TO)[0] + " - " + m.getSubject()
					+ " was sent successfully.");
		}
		// IMAP
		String url = null;
		String record = null; // name of folder in which to record mail
		// Keep a copy, if requested.
		keepACopy(record, url, msg, m);
	} // end of sendUseTransient
	
	void keepACopy(String record, String url, Message msg, MessageBean m) throws MessagingException {
		if (record != null && MailProtocol.IMAP.value().equalsIgnoreCase(protocol)) {
			// Get a Store object
			Store store = null;
			if (url != null) {
				URLName urln = new URLName(url);
				store = session.getStore(urln);
				store.connect();
			}
			else {
				if (protocol != null) {
					store = session.getStore(protocol);
				}
				else {
					store = session.getStore();
				}
				// Connect
				if (host != null || user != null || password != null) {
					store.connect(host, user, password);
				}
				else {
					store.connect();
				}
			}

			// Get record Folder.  Create if it does not exist.
			Folder folder = store.getFolder(record);
			if (folder == null) {
				logger.error("Can't get record folder.");
			}
			if (!folder.exists()) {
				folder.create(Folder.HOLDS_MESSAGES);
			}
			Message[] msgs = new Message[1];
			msgs[0] = msg;
			folder.appendMessages(msgs);
			if (isDebugEnabled) {
				logger.debug("Mail TO " + msg.getRecipients(RecipientType.TO)[0] + " - " + m.getSubject()
						+ " was recorded successfully.");
			}
		}
	}

	private void transportSend(Message msg, int retries) throws MessagingException {
		try {
			Transport.send(msg);
		}
		catch (MessagingException e) {
			String err = e.toString();
			if (err.indexOf("Could not connect") > 0 && err.indexOf("Address already in use") > 0 && retries < 10) {
				try {
					Thread.sleep(1000);
				}
				catch (InterruptedException e2) {}
				transportSend(msg, retries + 1);
			}
			else {
				throw e;
			}
		}
	}

	private void sendUsePersistent(Message msg) throws NumberFormatException, MessagingException {
		if (isDebugEnabled) {
			logger.debug("Entering sendUsePersistent method");
		}
		try {
			session.setDebug(debugSession);
			msg.setSentDate(new Date());
			// use a Transport instance to send the message via a specified smtp
			// server
			msg.saveChanges();
			// send the thing off
			transport.sendMessage(msg, msg.getRecipients(Message.RecipientType.TO));
			if (isDebugEnabled) {
				logger.debug("Mail TO " + msg.getRecipients(Message.RecipientType.TO)[0] + " - " + msg.getSubject()
						+ " was sent successfully.");
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught, retry...", e);
			if (transport != null && !transport.isConnected()) {
				initTransport();
				sendUsePersistent(msg);
			}
		}
	}

	private void sendUsePersistent(MessageBean m) throws NumberFormatException, MessagingException {
		if (isDebugEnabled) {
			logger.debug("Entering sendUsePersistent method");
		}
		try {
			session.setDebug(debugSession);
			// construct the message
			Message msg = new MimeMessage(session);
			if (m.getFrom() != null && m.getFrom().length > 0) {
				msg.setFrom(m.getFrom()[0]);
			}
			else {
				msg.setFrom();
			}
			msg.setRecipients(RecipientType.TO, m.getTo());
			if (m.getCc() != null) {
				msg.setRecipients(RecipientType.CC, m.getCc());
			}
			if (m.getBcc() != null) {
				msg.setRecipients(RecipientType.BCC, m.getBcc());
			}
			msg.setSubject(m.getSubject());
			msg.setText(m.getBody());
			if (m.getXmailer() != null && m.getXmailer().length > 0) {
				msg.setHeader(XHeaderName.MAILER.value(), m.getXmailer()[0]);
			}
			msg.setSentDate(new Date());
			// use a Transport instance to send the message via a specified smtp
			// server
			msg.saveChanges();
			// send the thing off
			transport.sendMessage(msg, obtainAddrArray(m));
			if (isDebugEnabled) {
				logger.debug("Mail TO " + msg.getRecipients(Message.RecipientType.TO)[0] + " - " + m.getSubject()
						+ " was sent successfully.");
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught, retry...", e);
			if (transport != null && !transport.isConnected()) {
				initTransport();
				sendUsePersistent(m);
			}
		}
	}

	private Address[] obtainAddrArray(MessageBean msgBean) {
		List<Address> addrList = new ArrayList<>();;
		if (msgBean.getTo() != null && msgBean.getTo().length > 0) {
			addrList.addAll(Arrays.asList(msgBean.getTo()));
		}
		if (msgBean.getCc() != null && msgBean.getCc().length > 0) {
			addrList.addAll(Arrays.asList(msgBean.getCc()));
		}
		if (msgBean.getBcc() != null && msgBean.getBcc().length > 0) {
			addrList.addAll(Arrays.asList(msgBean.getBcc()));
		}
		Address[] address = addrList.toArray(new Address[addrList.size()]);
		return address;
	}

	protected void finalize() {
		try {
			if (transport != null && transport.isConnected()) {
				transport.close();
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
	
}
