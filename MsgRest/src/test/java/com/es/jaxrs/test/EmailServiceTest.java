package com.es.jaxrs.test;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.Properties;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class EmailServiceTest {
	final static Logger logger = Logger.getLogger(EmailServiceTest.class);

	static EJBContainer ejbContainer;

	@Resource(name = "smtpSession")
	public Session smtpSession;

	@BeforeClass
	public static void beforeClass() {
		Properties p = new Properties();
		// SMTP properties
		p.put("smtpSession", "new://Resource?type=javax.mail.Session");
		p.put("smtpSession.mail.transport.protocol", "smtp");
		p.put("smtpSession.mail.smtp.host", "localhost");
		p.put("smtpSession.mail.smtp.port", "25");
		p.put("smtpSession.mail.smtp.auth", "false");

		ejbContainer = EJBContainer.createEJBContainer(p);
	}

	@AfterClass
	public static void afterClass() {
		ejbContainer.close();
	}

	@Before
	public void before() throws NamingException {
		ejbContainer.getContext().bind("inject", this);

	}

	@Test
	public void testSendSimpleEmail() throws AddressException, MessagingException {
		assertNotNull(smtpSession);
		
		smtpSession.setDebug(true);

		// Create a message
		final MimeMessage msg = new MimeMessage(smtpSession);
		msg.setFrom(new InternetAddress("jwang@localhost"));
		final InternetAddress[] address = { new InternetAddress("support@localhost") };
		msg.setRecipients(Message.RecipientType.TO, address);
		msg.setSubject("JavaMail SMTP Session test");
		msg.setSentDate(new Date());
		msg.setText("this is a test email", "UTF-8");
		
		//smtpSession.getTransport().send(msg);
		Transport.send(msg);
	}

}
