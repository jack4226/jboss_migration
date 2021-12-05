package com.es.jaxrs.service;

import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Path("/email")
public class EmailSession {
	protected final static Logger logger = LogManager.getLogger(EmailSession.class);

	@POST
	@Produces({ MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_XML })
	public String lowerCase(final EmailDto emailDto) {
		try {
			// Create some properties and get the default Session
			final Properties props = new Properties();
			props.put("mail.smtp.host", "localhost");
			props.put("mail.debug", "true");

			final Session session = Session.getInstance(props, new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication("email", "email");
				}
			});

			// Set this just to see some internal logging
			session.setDebug(true);

			// Create a message
			final MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(emailDto.fromAddress));
			final InternetAddress[] address = { new InternetAddress(emailDto.toAddress) };
			msg.setRecipients(Message.RecipientType.TO, address);
			msg.setSubject(emailDto.subject);
			msg.setSentDate(new Date());
			msg.setText(emailDto.message, "UTF-8");

			Transport.send(msg);
			logger.info("Message has been set to: " + emailDto.fromAddress + ", with subject: " + emailDto.subject);
		} catch (MessagingException e) {
			logger.error("MessagingException caught: " + e.getMessage());
			return "Failed to send message: " + e.getMessage();
		}

		return "Sent";
	}

	@XmlRootElement
	public static class EmailDto {
		public String fromAddress = "support@localhost";
		public String toAddress = "jwang@localhost";
		public String subject = "JavaMail API test";
		public String message;
	}
}
