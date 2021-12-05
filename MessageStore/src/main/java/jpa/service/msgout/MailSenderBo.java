package jpa.service.msgout;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.msg.MessageRendered;
import jpa.service.msgdata.MessageRenderedService;
import jpa.spring.util.SpringUtil;
import jpa.util.EmailSender;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Class to send the email off.
 * 
 * @author Jack Wang
 */
@Component("mailSenderBo")
//@org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRED)
public class MailSenderBo extends MailSenderBase {
	private static final long serialVersionUID = -851117269960155984L;
	static final Logger logger = LogManager.getLogger(MailSenderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	// send mail can be disabled through system property
	private boolean disableSendMail = false;

	/**
	 * must be a no-argument constructor.
	 */
	public MailSenderBo() {
		super();
		String disable_send_mail = System.getProperty("disable_send_mail");
		if ("yes".equals(disable_send_mail) || "true".equals(disable_send_mail)) {
			logger.warn("MAIL SENDER HAS BEEN DISABLED !!!!!");
			disableSendMail = true;
		}
	}

	public static void main(String[] args) {
		MailSenderBo sender = SpringUtil.getAppContext().getBean(MailSenderBo.class);
		MsgOutboxBo msgOutboxBo = SpringUtil.getAppContext().getBean(MsgOutboxBo.class);
		MessageRenderedService msgRenderedService = SpringUtil.getAppContext().getBean(MessageRenderedService.class);
		//SpringUtil.beginTransaction();
		try {
			MessageRendered mr = msgRenderedService.getFirstRecord();
			MessageBean bean = msgOutboxBo.getMessageByPK(mr.getRowId());
			if (bean.getTo()==null || bean.getTo().length==0) {
				bean.setTo(InternetAddress.parse("testto@localhost"));
			}
			logger.info("MessageBean retrieved:\n" + bean);
			sender.process(new MessageContext(bean));
			//SpringUtil.commitTransaction();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
		}
	}

	/**
	 * Process the request. The message context should contain an email
	 * message represented by either a MessageBean or a ByteStream.
	 * 
	 * @param req - a message bean or a message stream
	 * @throws IOException 
	 * @throws SmtpException 
	 */
	public void process(MessageContext req) throws SmtpException, IOException {
		try {
			if (disableSendMail == false) {
				processMessage(req);
			}
		}
		catch (DataValidationException dex) {
			// failed to send the message
			logger.error("DataValidationException caught", dex);
		}
		catch (MessagingException mex) {
			logger.error("MessagingException caught", mex);
		}
		catch (NullPointerException en) {
			logger.error("NullPointerException caught", en);
		}
		catch (IndexOutOfBoundsException eb) {
			// AddressException from InternetAddress.parse() caused this
			// Exception to be thrown
			// write the original message to error queue
			logger.error("IndexOutOfBoundsException caught", eb);
		}
		catch (NumberFormatException ef) {
			logger.error("NumberFormatException caught", ef);
			// send error notification
			EmailSender.sendEmail(null, ef.getMessage(),
					ExceptionUtils.getStackTrace(ef),
					EmailSender.EmailList.ToDevelopers);
		}
		catch (SmtpException se) {
			logger.error("SmtpException caught", se);
			// SMTP error, roll back and exit
			throw se;
		} 
		finally {
		}
	}

	/**
	 * Send the email off. <p>
	 * SMTP server properties are retrieved from database. 
	 * 
	 * @param msg -
	 *            message
	 * @param isSecure -
	 *            send via secure SMTP server when true
	 * @param errors -
	 *            contains delivery errors if any
	 * @throws SmtpException
	 * @throws MessagingException
	 */
	public void sendMail(javax.mail.Message msg, boolean isSecure, Map<String, Address[]> errors)
			throws MessagingException, IOException, SmtpException {
		NamedPool smtp = SmtpWrapperUtil.getSmtpNamedPool();
		NamedPool secu = SmtpWrapperUtil.getSecuNamedPool();
		/* Send Message */
		SmtpConnection smtp_conn = null;
		if (isSecure && !secu.isEmpty() || smtp.isEmpty()) {
			try {
				smtp_conn = (SmtpConnection) secu.getConnection();
				smtp_conn.sendMail(msg, errors);
			}
			finally {
				if (smtp_conn != null) {
					secu.returnConnection(smtp_conn);
				}
			}
		}
		else {
			try {
				smtp_conn = (SmtpConnection) smtp.getConnection();
				smtp_conn.sendMail(msg, errors);
			}
			finally {
				if (smtp_conn != null) {
					smtp.returnConnection(smtp_conn);
				}
			}
		}
	}

	/**
	 * Send the email off via unsecured SMTP server. <p>
	 * SMTP server properties are retrieved from database. 
	 * 
	 * @param msg -
	 *            message
	 * @throws SmtpException 
	 * @throws MessagingException 
	 */
	public void sendMail(javax.mail.Message msg, Map<String, Address[]> errors)
			throws MessagingException, SmtpException {
		NamedPool smtp = SmtpWrapperUtil.getSmtpNamedPool();
		if (smtp.isEmpty()) {
			smtp = SmtpWrapperUtil.getSecuNamedPool();
		}
		SmtpConnection smtp_conn = null;
		try {
			smtp_conn = (SmtpConnection) smtp.getConnection();
			smtp_conn.sendMail(msg, errors);
		}
		finally {
			if (smtp_conn != null) {
				smtp.returnConnection(smtp_conn);
			}
		}
	}
}