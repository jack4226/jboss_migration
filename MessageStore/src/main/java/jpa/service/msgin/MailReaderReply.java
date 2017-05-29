package jpa.service.msgin;

import java.io.IOException;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import jpa.constant.Constants;
import jpa.constant.XHeaderName;
import jpa.message.MessageBodyBuilder;

import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;

/**
 * provide method for constructing reply email message.
 * 
 * @author Jack Wang
 */
public class MailReaderReply implements java.io.Serializable {
	private static final long serialVersionUID = -4717336100946179241L;
	static final Logger logger = Logger.getLogger(MailReaderReply.class);
	/**
	 * compose a reply message.
	 * 
	 * @param msg -
	 *            message
	 * @param body -
	 *            body
	 * @return reply message
	 * @throws MessagingException
	 * @throws IOException
	 * @throws ParserException 
	 */
	public Message composeReply(Message msg, String body, String contentType)
			throws MessagingException, IOException {
		String LF = System.getProperty("line.separator","\n");
		// return the mail
		MimeMessage reply = (MimeMessage)msg.reply(false);	// reply to sender only
		reply.setSubject("Delivery Failure: Message exceeds local size limit.");
		/*
		 * construct reply body part
		 */
		MimeBodyPart mbp1 = new MimeBodyPart();
		Address addr[] = null;
		// retrieving TO address
		String to_addrStr="";
		if ((addr = msg.getRecipients(Message.RecipientType.TO)) != null && addr.length > 0) {
			to_addrStr = addr[0].toString();
			for (int j = 1; j < addr.length; j++) {
				to_addrStr += "," + addr[j].toString();
			}
		}
		// reply message
		String replyText =
		"We're sorry, but the size of the email body you sent is too large"+LF+
		"and cannot be processed due to system restrictions. Please delete"+LF+
		"any unnecessary text and/or attachments and resend the email."+LF+
		"You message "+LF+LF+
		"  Subject: "+msg.getSubject()+LF+LF+
		"was not delivered to:"+LF+LF+
		"  "+to_addrStr+LF+LF+
		"because:"+LF+LF+
		"  552 5.2.3 Message exceeds local size limit."+LF+LF;
		
		// set body part text
		mbp1.setText(replyText, "us-ascii");
		
		/*
		 * construct message/rfc822 body part
		 */
		MimeBodyPart mbp2 = new MimeBodyPart();
		// retrieving FROM address
		String from_addrStr = "";
		if ((addr = msg.getFrom()) != null && addr.length > 0) {
			from_addrStr = addr[0].toString();
			for (int j = 1; j < addr.length; j++) {
				from_addrStr += "," + addr[j].toString();
			}
		}
		// retrieve other headers
		String x_mailer = ((MimeMessage)msg).getHeader(XHeaderName.MAILER.value(),LF);
		String mime_ver = ((MimeMessage)msg).getHeader("MIME-Version",LF);
		// construct rfc822 headers
		String rfc822Str = LF + // don't remove this
			"Message-ID: "+((MimeMessage)msg).getMessageID()+LF+
			"From: "+from_addrStr+LF+
			"To: "+to_addrStr+LF+
			"Subject: "+msg.getSubject()+LF+
			"Date: "+msg.getSentDate()+" -0400"+LF+
			"MIME-Version: "+mime_ver+LF+
			"X-Mailer: "+x_mailer+LF+
			"Content-Type: text/plain; charset=iso-8859-1"+LF+LF;
		// remove HTML/BODY tags from original message
		if (contentType != null && contentType.toLowerCase().startsWith("text/html")) {
			body = MessageBodyBuilder.removeHtmlBodyTags(body);
		}
		// limit reply message size
		if (body.length() > 10 * 1024) { // allow up to 10k to be attached
			body = body.substring(0, 10 * 1024) + LF + Constants.MESSAGE_TRUNCATED;
		}
		ByteArrayDataSource bads = new ByteArrayDataSource(rfc822Str + body, "message/rfc822");
		mbp2.setDataHandler(new DataHandler(bads));
		//mbp2.setContent(rfc822Str + body, "message/rfc822");
		
		Multipart mp = new MimeMultipart("mixed");
		mp.addBodyPart(mbp1);
		mp.addBodyPart(mbp2);
		
		reply.setContent(mp);
		return reply;
	}
}

