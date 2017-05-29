package jpa.util;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author Jack Wang
 *
 * This is a common email sender that is used to send out email notifications.
 * 
 * This class requires an email.properties file in META-INF folder of your class path.
 * 
 * Sample contents of email.properties:
#
# %%%CHANGE ME%%%: Change the SenderId to your project name
#
SenderId=MyApp
#
HostName=emailsphere.com
HostIP=localhost
EmailDomain=espheredemo.com
#
# Default recipients for unchecked error.
#
RecipientsForUnchecked.DEV=developers
RecipientsForUnchecked.TEST=quality.control
RecipientsForUnchecked.UAT=user.acceptance
RecipientsForUnchecked.PROD=prod.support
#
# Recipients for fatal error.
#
RecipientsForFatalError.DEV=developers
RecipientsForFatalError.TEST=developers,quality.control
RecipientsForFatalError.UAT=developers
RecipientsForFatalError.PROD=developers,prod.support
#
# Recipients for developers.
#
RecipientsForDevelopers.DEV=developers
RecipientsForDevelopers.TEST=developers
RecipientsForDevelopers.UAT=developers
RecipientsForDevelopers.PROD=developers
#
# disable email notification: yes/no
disable=no
#
 */
public class EmailSender {
	private static Logger logger = Logger.getLogger(EmailSender.class);
	static boolean isDebugEnabled = logger.isDebugEnabled();

	private static String fileName = "META-INF/email.properties";
	private static Properties emailProps = null;
	private static String hostName = null;
	
	private static List<String> hostNames = new ArrayList<String>();
	private static int currHostIdx = 0;
	
	public enum EmailList {
		ToUnchecked,
		ToFatalError,
		ToDevelopers
	}
	
	private EmailSender() {
	}

	private static void getEmailProperties() {
		if (emailProps == null) {
			logger.info("email properties file name: " + fileName);
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			URL url = loader.getResource(fileName);
			if (url == null) {
				throw new RuntimeException("Could not find " + fileName + " file.");
			}
			logger.info("loading email properties file from: " + url.getPath());
			emailProps = new Properties();
			try {
				InputStream is = url.openStream();
				emailProps.load(is);
			}
			catch (IOException e) {
				throw new RuntimeException("IOException caught, failed to load " + fileName, e);
			}
			logger.info(fileName + ": " + emailProps);
			hostNames.add(emailProps.getProperty("HostName"));
			String hostIP = emailProps.getProperty("HostIP");
			if (StringUtils.isNotBlank(hostIP)) {
				hostNames.add(hostIP);
			}
			hostName = hostNames.get(0);
		}
	}

	private static Session getMailSession() {
		String mailJndi = "mail/Mailer"; //"java:comp/env/mail/Mailer";
		Session mailSession = null;
		//get JNDI configuration mail session from the was server
		try {
			InitialContext ctx = new InitialContext();
   			mailSession = (Session) ctx.lookup(mailJndi);
		}
		catch (NamingException ne) {
			logger.warn("Could not find JNDI entry: " + mailJndi + ". " + ne.getMessage());
			//If there is no JNDI configuration, 
			//then get mail session through convention way
			Properties props = System.getProperties();
			props.put("mail.smtp.host", hostName);
			mailSession = Session.getInstance(props, null);
		}
		return mailSession;
	}

	/**
	 * Send an email notification when unchecked error was raised.
	 * Email subject line is constructed as following: 
	 * <SenderId> Application - Error, <region>
	 * for example: Emailsphere Application - Error, TEST
	 * 
	 * @param body
	 *            - message body
	 * @param attachment
	 *            - attachment, optional.
	 * @return true if email is sent successfully
	 */
	public static boolean sendToUnchecked(String body, String attachment) {
		return sendEmail(null, body, attachment, EmailList.ToUnchecked);
	}

	/**
	 * A send mail method that sends email notifications when an unchecked error
	 * or a fatal error was raised from application.
	 * 
	 * @param subject
	 *            - message subject, ignored if recipient is
	 *            EmailList.ToUnchecked.
	 * @param body
	 *            - message body
	 * @param attachment
	 *            - message attachment, optional.
	 * @param emailList
	 *            - message recipients.
	 * @return true if email is sent successfully
	 */
	public static boolean sendEmail(String subject, String body,
			String attachment, EmailList emailList) {
		// read email.properties
		getEmailProperties();
		String region = EnvUtil.getEnv().toUpperCase();
		// get recipients from properties file
		String recipients = emailProps.getProperty("RecipientsForUnchecked." + region);
		if (EmailList.ToFatalError.equals(emailList)) {
			recipients = emailProps.getProperty("RecipientsForFatalError." + region);
		}
		else if (EmailList.ToDevelopers.equals(emailList)) {
			recipients = emailProps.getProperty("RecipientsForDevelopers." + region);
		}
		return sendEmail(subject, body, attachment, recipients); 
	}

	/**
	 * A send mail method that sends email notifications when an unchecked error
	 * or a fatal error was raised from application.
	 * 
	 * @param subject
	 *            - message subject
	 * @param body
	 *            - message body
	 * @param attachment
	 *            - message attachment, optional.
	 * @param recipients
	 *            - message recipients.
	 * @return true if email is sent successfully
	 */
	public static boolean sendEmail(String subject, String body,
			String attachment, String recipients) {
		// read email.properties
		getEmailProperties();
		if ("yes".equalsIgnoreCase(emailProps.getProperty("disable"))) {
			logger.info("sendEmail() - Email notification disabled in " + fileName);
			return false;
		}
		try {
			send_mail(subject, body, attachment, recipients);
			return true;
		}
		catch (AddressException e) {
			logger.error("Invalid Email address found: ", e);
			return false;
		}
		catch (MessagingException e1) {
			if ((e1.toString().indexOf("Could not connect to SMTP host") >= 0 
					|| e1.toString().indexOf("Unknown SMTP host") >= 0)
					&& (++currHostIdx < hostNames.size())) {
				logger.error("Failed to send email via " + hostName, e1);
				hostName = (String) hostNames.get(currHostIdx);
				logger.error("Try next SMTP server " + hostName + " ...");
				return sendEmail(subject, body, attachment, recipients);
			}
			else {
				throw new RuntimeException("Failed to send email via " + hostName, e1);
			}
		}
	}
	
	private static void send_mail(String _subject, String body, String attachment,
			String recipients) throws AddressException, MessagingException {
		// get "region" from system properties
		String region = EnvUtil.getEnv().toUpperCase();
		String senderId = emailProps.getProperty("SenderId");
		if (StringUtils.isBlank(recipients)) {
			logger.warn("sendEmail() - Email recipients is not provided, quit.");
			return;
		}
		
		String emailDomain = emailProps.getProperty("EmailDomain","emailsphere.com");
		
		logger.info("EMail host name: " + hostName + ", Region: " + region);

		Session session = getMailSession();

		// create a message
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(appendDomain(senderId, emailDomain)));
		String recipientAddrs = appendDomain(recipients, emailDomain);
		InternetAddress[] address = InternetAddress.parse(recipientAddrs, false);
		msg.setRecipients(Message.RecipientType.TO, address);
		if (StringUtils.isNotBlank(_subject)) {
			String subj = StringUtils.replaceOnce(_subject, "{0}", region);
			msg.setSubject(subj);
		}
		else {
			msg.setSubject(senderId + " Application - Error, " + region);
		}
		// create and fill the first message part
		MimeBodyPart mbp1 = new MimeBodyPart();
		String bodyStr = StringUtils.replaceOnce(body, "{0}", region);
		mbp1.setText(bodyStr);

		// create the Multipart and add its parts to it
		Multipart mp = new MimeMultipart();
		mp.addBodyPart(mbp1);

		if (StringUtils.isNotBlank(attachment)) {
			// create the second message part
			MimeBodyPart mbp2 = new MimeBodyPart();
			mbp2.setText(attachment, "us-ascii");
			mp.addBodyPart(mbp2);
		}

		// add the Multipart to the message
		msg.setContent(mp);

		// set the Date: header
		msg.setSentDate(new Date());

		// send the message
		Transport.send(msg);

		logger.info("Email notification sent to: " + recipientAddrs);
	}

	/**
	 * A generic send mail method.
	 * @param from - from address
	 * @param to - to address
	 * @param subject - message subject
	 * @param body - message body
	 * @throws MessagingException
	 */
	public static void send(String from, String to, String subject, String body)
			throws MessagingException {
		if (StringUtils.isBlank(to)) {
			throw new MessagingException("TO address is blank.");
		}
		if (StringUtils.isBlank(subject)) {
			throw new MessagingException("Subject is blank.");
		}
		getEmailProperties();
		// Get a Session object
		Session session = getMailSession();
		// construct a MimeMessage
		Message msg = new MimeMessage(session);
		Address[] addrs = InternetAddress.parse(from, false);
		if (addrs != null && addrs.length > 0) {
			msg.setFrom(addrs[0]);
		}
		else {
			msg.setFrom();
		}
		msg.setRecipients(RecipientType.TO, InternetAddress.parse(to, false));
		msg.setSubject(subject);
		msg.setText(body);
		msg.setSentDate(new Date());
		// could also use Session.getTransport() and Transport.connect()
		// send the thing off
		Transport.send(msg);
		if (isDebugEnabled) {
			logger.debug("Mail from " + from + " - " + subject
					+ " was sent to: " + to);
		}
	}

	private static String appendDomain(String addrs, String emailDomain) {
		StringTokenizer st = new StringTokenizer(addrs, ",");
		StringBuffer sb = new StringBuffer();
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.indexOf("@") < 0) {
				token += "@" + emailDomain;
			}
			if (sb.length() > 0) sb.append(",");
			sb.append(token);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		//fileName = "META-INF/email_sample.properties";
		try {
			EmailSender.sendEmail("Test from EmailSender",
					"EmailSender...\ntest message", "attachment text",
					EmailList.ToUnchecked);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
