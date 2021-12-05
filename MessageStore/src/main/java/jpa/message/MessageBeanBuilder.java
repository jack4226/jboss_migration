package jpa.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import jpa.constant.Constants;
import jpa.constant.XHeaderName;
import jpa.util.EmailAddrUtil;
import jpa.util.EmailSender;
import jpa.util.HostUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class MessageBeanBuilder {
	static final Logger logger = LogManager.getLogger(MessageBeanBuilder.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	public static final String MSG_RAW_STREAM = "msg_raw_stream";
	final static String LF = System.getProperty("line.separator", "\n");
	
	private static String hostName = null;
	
	private MessageBeanBuilder() {
		// make it static only
	}
	
	/**
	 * process message part
	 * 
	 * @param p -
	 *            part
	 * @param toAddrDomain -
	 *            a list of domain names separated by comma. when present, the
	 *            code will try to find a TO address that its domain name
	 *            matches one of the names from the list.
	 * @throws MessagingException
	 * @throws IOException
	 *             if any error
	 */
	public static MessageBean processPart(Part p, String toAddrDomain)
			throws MessagingException {
		// make sure it's a message
		if (!(p instanceof Message) && !(p instanceof MimeMessage)) {
			// not a known message type
			try {
				logger.error("Unknown Part: " + p.getContentType());
				logger.error("---------------------------");
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}
			throw new MessagingException("Part was not a MimeMessage as expected");
		}
		
		if (hostName == null) {
			try {
				hostName = java.net.InetAddress.getLocalHost().getHostName();
				if (isDebugEnabled)
					logger.debug("Host Name: " + hostName);
			}
			catch (UnknownHostException e) {
				logger.warn("UnknownHostException caught, use localhost as host name", e);
				hostName = "localhost";
			}
		}
		
		MessageBean msgBean = new MessageBean();
		msgBean.clearParameters();
		
		// populate message header
		processEnvelope((Message) p, msgBean, toAddrDomain);

		addXHeadersToBean(msgBean, (Message) p);
		
		processAttachment((BodypartBean) msgBean, p, msgBean, 0);
		
		addMsgStreamToBean(p, msgBean);
		
		if (isDebugEnabled) {
			logger.debug("Number of attachments: " + msgBean.getAttachCount());
			logger.debug("ContentType ************************:" + LF + msgBean.getBodyContentType(true));
			logger.debug("Body *******************************:" + LF + msgBean.getBody(true));
		}
		return msgBean;
	}

	/**
	 * process message envelope and headers
	 * 
	 * @param msg -
	 *            message
	 * @return - SMTP message id, or null if not found
	 * @throws AddressException
	 *             if any error
	 */
	private static String processEnvelope(Message msg, MessageBean msgBean, String toAddrDomain)
			throws AddressException {
		Address[] from = null,
			received_to = null,
			envelope_to = null,
			cc = null,
			bcc = null,
			replyto = null;
		String[] xmailer = null;
		String subject = null;
		String messageId= null;
		java.util.Date receivedTime = null;

		// Received Date
		try {
			receivedTime = msg.getReceivedDate();
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught during getReceivedDate()", e);
		}
		Calendar rightNow = Calendar.getInstance();
		if (receivedTime != null) {
			Calendar receivedTimePlusHours = Calendar.getInstance();
			receivedTimePlusHours.setTime(receivedTime);
			receivedTimePlusHours.add(Calendar.HOUR, 48);
			// The Received Date from Spam E-mails are usually way off.
			// Allow system down time up to 48 hours.
			if (rightNow.getTimeInMillis() > receivedTimePlusHours.getTimeInMillis()) {
				msgBean.setSendDate(rightNow.getTime());
			}
			else {
				msgBean.setSendDate(receivedTime);
			}
			//msgBean.setSendDate(receivedTime);
		}

		// retrieve Message-Id and Return-Path from headers
		try {
			Enumeration<?> enu = ((MimeMessage) msg).getAllHeaders();
			while (enu.hasMoreElements()) {
				Header hdr = (Header) enu.nextElement();
				String name = hdr.getName();
				if (isDebugEnabled)
					logger.debug("header line: " + name + ": " + hdr.getValue());
				if ("Message-ID".equalsIgnoreCase(name)) {
					messageId= hdr.getValue();
					logger.info(">>>>>Message-ID retrieved: " + messageId);
					msgBean.setSmtpMessageId(messageId);
				}
				if (XHeaderName.RETURN_PATH.value().equalsIgnoreCase(name)) {
					msgBean.setReturnPath(hdr.getValue());
				}
				if ("Date".equals(name) && receivedTime == null) {
					receivedTime = getHeaderDate(hdr.getValue()); // SMTP Date
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught from getAllHeaderLines()", e);
		}

		// If Received Date not found from envelope, use current time
		if (receivedTime == null) {
			msgBean.setSendDate(rightNow.getTime());
		}
		// display Received Date
		if (isDebugEnabled) {
			logger.info("Email Received Time: "
					+ (receivedTime != null ? receivedTime.toString() : "UNKNOWN")
					+ ", SERVER-TIME: " + rightNow.getTime().toString());
		}

		String[] received = null;
		try {
			received = ((MimeMessage) msg).getHeader("Received");
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught from getHeader(Received)", e);
		}

		// retrieve TO address (real_to) from "Received" Headers.
		String real_to = "";
		if (received != null) { // sanity check, should never be null
			// scan received headers for "for" address, starting from the last
			// "received" header (the oldest).
			int i;
			String tmp_to = null;
			for (i = received.length - 1; i >= 0; i--) {
				if (isDebugEnabled)
					logger.debug("Received: " + received[i]);
				if ((tmp_to = analyzeReceived(received[i])) != null) {
					// rely on InternetAddress.parse() to perform syntax check.
					real_to = tmp_to;
					logger.info("found \"for\" in Received Headers: " + real_to);
					break; // exit loop
				}
			}
			if (tmp_to != null && toAddrDomain != null
					&& toAddrDomain.indexOf(getDomain(real_to)) < 0) {
				// domain matching string is present in MailBoxes record, scan
				// Received headers for address with a matching domain.
				for (int j = i - 1; j >= 0; j--) {
					if ((tmp_to = analyzeReceived(received[j])) != null) {
						if (toAddrDomain.indexOf(getDomain(tmp_to)) >= 0) {
							real_to = tmp_to;
							logger.info("found \"for\" with matching domain in Received Headers: "
									+ real_to);
							break; // exit loop
						}
					}
				}
			}
		}

		Address[] addr;
		// FROM from envelope or Return-Path
		try {
			String[] _froms = null;
			if ((addr = msg.getFrom()) != null && addr.length > 0) {
				String addrStr = checkAddr(addr[0].toString());
				for (int j = 1; j < addr.length; j++) {
					addrStr += "," + checkAddr(addr[j].toString());
				}
				from = InternetAddress.parse(addrStr);
			}
			else if ((_froms = msg.getHeader(XHeaderName.RETURN_PATH.value())) != null && _froms.length > 0) {
				logger.warn("FROM is missing from envelope, use Return-Path instead.");
				String addrStr = checkAddr(_froms[0]);
				for (int j = 1; j < _froms.length; j++) {
					addrStr += "," + checkAddr(_froms[j]);
				}
				from = InternetAddress.parse(addrStr);
			}
			else {
				// FROM is empty from envelope and Return-Path
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught from getFrom()", e);
		}
		msgBean.setFrom(from);
		if (isDebugEnabled)
			logger.debug("Email From Address: " + msgBean.getFromAsString());

		// TO from Received Headers
		if (real_to != null && real_to.trim().length() > 0) {
			// found TO address from header
			try {
				received_to = InternetAddress.parse(real_to);
			}
			catch (javax.mail.internet.AddressException e) {
				logger.error("Warning!!! AddressException caught from parsing " + real_to, e);
			}
		}

		// TO from envelope
		try {
			if ((addr = msg.getRecipients(RecipientType.TO)) != null && addr.length > 0) {
				String addrStr = checkAddr(addr[0].toString());
				for (int j = 1; j < addr.length; j++) {
					addrStr += "," + checkAddr(addr[j].toString());
				}
				envelope_to = InternetAddress.parse(addrStr);
			}
			else {
				// TO is empty from envelope
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught from getRecipients(TO)", e);
		}
		msgBean.setToEnvelope(envelope_to);

		// TO from "Delivered-To" header
		Address[] delivered_to = null;
		try {
			String[] dlvrTo = msg.getHeader("Delivered-To");
			if (dlvrTo != null && dlvrTo.length > 0) {
				String addrStr = checkAddr(dlvrTo[0]);
				for (int j=1; j<dlvrTo.length; j++) {
					addrStr += "," + checkAddr(dlvrTo[j]);
				}
				logger.info("\"Delivered-To\" found from header: " + addrStr);
				delivered_to = InternetAddress.parse(addrStr);
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught from msg.getHeader(\"Delivered-To\")", e);
		}
		
		// TO: Received (non-VERP) > Delivered-To > Received (VERP) > Envelope 
		if (received_to != null && received_to.length > 0) {
			String dest = received_to[0] == null ? null : received_to[0].toString();
			if (StringUtils.isNotBlank(dest) && !EmailAddrUtil.isVERPAddress(dest)) {
				msgBean.setTo(received_to);
			}
		}
		if (msgBean.getTo() == null) {
			if (delivered_to != null && delivered_to.length > 0) {
				// The real mailbox address the email is delivered to. If the email
				// address is a VERP address, the original address is restored from
				// the VERP address and is assigned to "Delivered-To" header.
				msgBean.setTo(delivered_to);
			}
			else if (received_to != null && received_to.length > 0) {
				// Email address extracted from "Received" header is the real email
				// address. But when VERP is enabled, since the Email Id is embedded
				// in the VERP address, every email received will have its own VERP
				// address. This will cause a disaster to EmailAddress table since all
				// TO addresses are saved to that table.
				String dest = received_to[0] == null ? null : received_to[0].toString();
				if (StringUtils.isNotBlank(dest) && EmailAddrUtil.isVERPAddress(dest)) {
					String verpDest = EmailAddrUtil.getDestAddrFromVERP(dest);
					try {
						Address[] destAddr = InternetAddress.parse(verpDest);
						msgBean.setTo(destAddr);
					}
					catch (AddressException e) {
						logger.error("AddressException from Received_To: " + dest);
					}
				}
			}
			if (msgBean.getTo() == null) {
				msgBean.setTo(envelope_to);
			}
		}
		logger.info("Email To from Delivered-To: " + EmailAddrUtil.addressToString(delivered_to, false)
				+ ", from Received Header: " + EmailAddrUtil.addressToString(received_to, false)
				+ ", from Envelope: " + EmailAddrUtil.addressToString(envelope_to, false));
		
		// CC
		try {
			if ((addr = msg.getRecipients(RecipientType.CC)) != null && addr.length > 0) {
				cc = addr;
				msgBean.setCc(cc);
				if (isDebugEnabled)
					logger.debug("Email CC Address: " + msgBean.getCcAsString());
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught during getRecipients(CC)", e);
		}

		// BCC
		try {
			if ((addr = msg.getRecipients(RecipientType.BCC)) != null && addr.length > 0) {
				bcc = addr;
				msgBean.setBcc(bcc);
				if (isDebugEnabled)
					logger.debug("Email BCC Address: " + msgBean.getBccAsString());
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught during getRecipients(BCC)", e);
		}

		// REPLYTO
		try {
			if ((addr = msg.getReplyTo()) != null && addr.length > 0) {
				String addrStr = checkAddr(addr[0].toString());
				for (int j = 1; j < addr.length; j++) {
					addrStr += "," + checkAddr(addr[j].toString());
				}
				replyto = InternetAddress.parse(addrStr);
				msgBean.setReplyto(replyto);
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught during getReplyTo()", e);
		}

		// SUBJECT
		try {
			subject = msg.getSubject();
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught during getSubject()", e);
		}
		msgBean.setSubject(subject);
		if (isDebugEnabled)
			logger.debug("Email Subject: [" + subject + "]");

		// X-MAILER
		try {
			String[] hdrs = msg.getHeader(XHeaderName.MAILER.value());
			if (hdrs != null) {
				xmailer = hdrs;
				msgBean.setXmailer(xmailer);
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught during getHeader(X-Mailer)", e);
		}

		// X-Priority: 1 (High), 2 (Normal), 3 (Low)
		try {
			String[] priority = ((MimeMessage) msg).getHeader(XHeaderName.PRIORITY.value());
			if (priority != null) {
				msgBean.setPriority(priority);
			}
		}
		catch (MessagingException e) {
			logger.error("MessagingException caught during getHeader(X-Priority)", e);
		}

		return messageId;
	} // end of processEnvelope

	/**
	 * Add certain message headers to MessageBean.
	 * 
	 * @param msgBean -
	 *            MessageBean
	 * @param mimeMsg -
	 *            JavaMail message
	 * @throws MessagingException
	 */
	private static void addXHeadersToBean(MessageBean msgBean, Message mimeMsg)
			throws MessagingException {
		// check X Headers and populate MessageBean properties
		String[] senderId = mimeMsg.getHeader(XHeaderName.SENDER_ID.value());
		if (senderId != null && senderId.length > 0) {
			msgBean.setSenderId(senderId[0]);
		}
		String[] subrId = mimeMsg.getHeader(XHeaderName.SUBSCRIBER_ID.value());
		if (subrId != null && subrId.length > 0) {
			msgBean.setSubrId(subrId[0]);
		}
		// DO NOT SET MessageBean's MsgRefId property here
	}

	/**
	 * recursively build up an attachment tree structure from a MultiPart
	 * message.
	 * 
	 * @param aNode -
	 *            current BodypartBean
	 * @param p -
	 *            JavaMail part
	 * @param msgBean -
	 *            root message bean
	 * @param level -
	 *            attachment level
	 */
	private static void processAttachment(BodypartBean aNode, Part p, MessageBean msgBean,
			int level) {
		/**
		 * "javax.mail.MessagingException: Missing start boundary" was caught by
		 * mp.getCount() during the MultiPart read of an email bounced from
		 * Linux sendmail smtp server to Lotus notes.
		 * 
		 * Here is the header line that might have caused the problem:
		 * Content-Type: multipart/report; report-type=delivery-status;
		 * 		boundary="==IFJRGLKFGIR45076UHRUHIHD"
		 * 
		 * The actual boundary reported from smtp log file is: f6IFMHf15667
		 * 
		 * It is considered that Lotus Notes altered the header contents or
		 * message body boundaries in an incorrect way and that caused the
		 * getCount() method to fail. The same email saved in a Sun server
		 * was read successfully by this program.
		 * 
		 * "java.io.UnsupportedEncodingException: unicode-1-1-utf-7" was caught
		 * by p.getCount() during the process of some text/* type of messages.
		 * 
		 * A try/catch block is added to get around these problems.
		 */
		String disp = null, desc = null, contentType = "text/plain";
		String dispOrig = null, descOrig = null;
		String fileName = null;
		// initialize part size
		int partSize = 0;
		// get content type
		try {
			contentType = p.getContentType();
		}
		catch (Exception e) {
			contentType = "text/plain"; // failed to get content type, use default
			logger.error("Exception caught during getContentType()", e);
		}
		// get disposition
		try {
			dispOrig = p.getDisposition();
			/*
			 * disposition may look like:
		 	 * 1) inline 
		 	 * 2) attachment 
		 	 * 3) attachment|inline; filename=xxxxx
			 * I believe JavaMail is taking care of this. However the code
			 * stays here just for safety.
			 */ 
			if (dispOrig != null && dispOrig.indexOf(";") > 0) {
				disp = dispOrig.substring(0, dispOrig.indexOf(";"));
			}
			else {
				disp = dispOrig;
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during getDisposition()", e);
		}
		// get description
		try {
			descOrig = p.getDescription();
			// to make use of "desc" field by saving attachment file name on it.
			if (descOrig == null) {
				// if null, get attachment filename from content type field
				desc = getFileName(contentType);
				// JavaMail appends file name to content type field if one is
				// found from disposition field
			}
			else {
				desc = descOrig;
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during getDescription()", e);
		}
		// get file name
		try {
			fileName = p.getFileName();
		}
		catch (Exception e) {
			logger.error("Exception caught during getFileName()", e);
		}
		// get part size
		try {
			partSize = p.getSize();
		}
		catch (Exception e) {
			logger.error("Exception caught during getSize()", e);
		}
		// display some key information
		if (isDebugEnabled) {
			logger.info("getDisposition(): " + dispOrig);
			logger.info("getDescription(): " + descOrig);
			logger.info("getContentType(): " + contentType + ", level:" + level + ", size:"
					+ partSize);
		}
		if (fileName != null && isDebugEnabled) {
			logger.debug("getFileName() = " + fileName);
		}
		// build mime tree
		try {
			aNode.setDisposition(disp);
			aNode.setDescription(desc);
			aNode.setFileName(fileName);
			// update attachment count
			if (Part.ATTACHMENT.equalsIgnoreCase(disp) 
					|| (Part.INLINE.equalsIgnoreCase(disp) && desc != null)
					|| getFileName(contentType) != null) {
				
				msgBean.updateAttachCount(1);
			}
			// set content type
			aNode.setContentType(contentType);
			//if (!p.isMimeType("multipart/*")) {
				aNode.setHeaders(p);
			//}
			aNode.setSize(partSize);
			/*
			 * Using isMimeType to determine the content type to avoid fetching
			 * the actual content data until it is needed.
			 */
			if (p.isMimeType("text/plain") || p.isMimeType("text/html")) {
				logger.info("processAttc: level " + level + ", text message: " + contentType);
				aNode.setValue((String) p.getContent());
				msgBean.getComponentsSize().add(aNode.getSize());
			}
			else if (p.isMimeType("multipart/*")) {
				// multipart attachment(s) detected
				logger.info("processAttc: level " + level + ", Recursive Multipart: "
								+ contentType);
				if (p.getContent() instanceof Multipart) {
					Multipart mp = (Multipart) p.getContent();
					int count = mp.getCount();
					for (int i = 0; i < count; i++) {
						Part p1 = mp.getBodyPart(i);
						// call itself to build up a child attachment tree
						if (p1 != null) {
							BodypartBean subNode = new BodypartBean();
							processAttachment(subNode, p1, msgBean, level+1);
							aNode.put(subNode);
						}
					}
				}
				else if (p.getContent() instanceof MimeMultipart) {
					MimeMultipart mp = (MimeMultipart) p.getContent();
					int count = mp.getCount();
					for (int i = 0; i < count; i++) {
						Part p1 = mp.getBodyPart(i);
						// call itself to build up a child attachment tree
						if (p1 != null) {
							BodypartBean subNode = new BodypartBean();
							processAttachment(subNode, p1, msgBean, level+1);
							aNode.put(subNode);
						}
					}
				}
				else {
					logger.error("Unknown class for multipart received: " + p.getContent().getClass().getName());
				}
			}
			else if (p.isMimeType("message/rfc822"))
			// || p.isMimeType("text/rfc822-headers"))
			{
				// nested message type
				logger.info("processAttc: level " + level + ", RFC822 Message: " + contentType);
				Part p1 = (Part) p.getContent();
				if (p1 != null) {
					BodypartBean subNode = new BodypartBean();
					processAttachment(subNode, p1, msgBean, level+1);
					aNode.put(subNode);
				}
			}
			else {
				/*
				 * other mime type. could be application, image, audio, video,
				 * message, etc.
				 */
				//logger.info("processAttc: level " + level + ", unknown mime type: " + contentType);
				Object o = p.getContent();
				/*
				 * unknown mine type section. check its java type.
				 */
				if (o instanceof String) {
					// text type of section
					logger.info("processAttc: level " + level + ", Java String Content type "
							+ contentType);
					aNode.setValue((String) o);
					if (aNode.getValue() != null) {
						msgBean.getComponentsSize().add(aNode.getValue().length);
					}
				}
				else if (o instanceof InputStream) {
					// stream type of section
					logger.info("processAttc: level " + level + ", Java InputStream Content type "
							+ contentType);
					aNode.setValue((InputStream) o); // it converts input stream to byte array
					if (aNode.getValue() != null) {
						msgBean.getComponentsSize().add(aNode.getValue().length);
					}
				}
				else if (o != null) {
					// unknown Java type, write it out as a string anyway.
					logger.error("processAttc: level " + level + ", Unknown Object type: "
							+ o.toString());
					aNode.setValue(o.toString());
					if (aNode.getValue() != null) {
						msgBean.getComponentsSize().add(aNode.getValue().length);
					}
				}
				else {
					// no content
					logger.error("processAttc: level " + level + ", Content is null");
					aNode.setValue((Object)null);
				}
			}
		} // end of the try block
		catch (IndexOutOfBoundsException e) {
			/* thrown from mp.getBodyPart(i), should never happen */
			logger.error("processAttc(): IndexOutOfBoundsException caught: " + contentType);
			logger.error("IndexOutOfBoundsException caught", e);
			aNode.setValue("reader001: IndexOutOfBoundsException caught during process.");
			BodypartBean subNode = new BodypartBean("text/plain");
			aNode.put(subNode);
			setAnodeValue(subNode, p,
					"reader002: IndexOutOfBoundsException thrown from mp.getBodyPart(i), Body Part does not exist.");
			if (subNode.getValue() != null) {
				msgBean.getComponentsSize().add(subNode.getValue().length);
			}
			subNode.setDisposition(aNode.getDisposition());
			subNode.setDescription(aNode.getDescription());
		}
		catch (MessagingException e) {
			/*
			 * JavaMail failed to read the message body, use its raw data
			 * instead
			 */
			logger.error("processAttc(): MessagingException caught: " + contentType);
			logger.error("MessagingException caught", e);
			if (contentType.trim().toLowerCase().startsWith("multipart/")
					|| contentType.trim().toLowerCase().startsWith("message/rfc822")) {
				aNode.setValue("reader003: MessagingException caught during process.");
				BodypartBean subNode = new BodypartBean("text/plain");
				aNode.put(subNode);
				setAnodeValue(subNode, p);
				if (subNode.getValue() != null) {
					msgBean.getComponentsSize().add(subNode.getValue().length);
				}
				subNode.setDisposition(aNode.getDisposition());
				subNode.setDescription(aNode.getDescription());
			}
			else {
				setAnodeValue(aNode, p);
				if (aNode.getValue() != null) {
					msgBean.getComponentsSize().add(aNode.getValue().length);
				}
			}
		}
		catch (UnsupportedEncodingException e) {
			/* unsupported encoding found, use its raw data instead */
			logger.error("processAttc(): UnsupportedEncodingException caught: " + contentType);
			logger.error("UnsupportedEncodingException caught", e);
			if (contentType.trim().toLowerCase().startsWith("multipart/")
					|| contentType.trim().toLowerCase().startsWith("message/rfc822")) {
				aNode.setValue("reader004: UnsupportedEncodingException caught during process.");
				BodypartBean subNode = new BodypartBean("text/plain");
				aNode.put(subNode);
				setAnodeValue(subNode, p);
				if (subNode.getValue() != null) {
					msgBean.getComponentsSize().add(subNode.getValue().length);
				}
				subNode.setDisposition(aNode.getDisposition());
				subNode.setDescription(aNode.getDescription());
			}
			else {
				setAnodeValue(aNode, p);
				if (aNode.getValue() != null) {
					msgBean.getComponentsSize().add(aNode.getValue().length);
				}
			}
		}
		catch (IOException e) {
			/*
			 * IOException caught during decoding, couldn't read the message
			 * body
			 * use "-- Message body has been omitted --" as body text
			 */
			logger.error("processAttc(): IOException caught: " + contentType);
			logger.error("IOException caught", e);
			if (contentType.trim().toLowerCase().startsWith("multipart/")
					|| contentType.trim().toLowerCase().startsWith("message/rfc822")) {
				aNode.setValue("reader005: IOException caught during process.");
				BodypartBean subNode = new BodypartBean("text/plain");
				aNode.put(subNode);
				subNode.setValue("-- Message body has been omitted --");
				subNode.setDisposition(aNode.getDisposition());
				subNode.setDescription(aNode.getDescription());
			}
			else {
				aNode.setValue("-- Message body has been omitted --");
			}
		}
		catch (Exception e) {
			/* all other unchecked exceptions */
			logger.error("processAttc(): Exception caught: " + contentType);
			logger.error("Exception caught", e);
			if (contentType.trim().toLowerCase().startsWith("multipart/")
					|| contentType.trim().toLowerCase().startsWith("message/rfc822")) {
				aNode.setValue("reader006: Exception caught during process.");
				BodypartBean subNode = new BodypartBean("text/plain");
				aNode.put(subNode);
				setAnodeValue(subNode, p, "Unchecked Exception caught: " + e.toString());
				subNode.setDisposition(aNode.getDisposition());
				subNode.setDescription(aNode.getDescription());
			}
			else {
				setAnodeValue(aNode, p, "Unchecked Exception caught: " + e.toString());
			}
			// notify programming with an Email (requires an SMTP server on local host)
			String hostIp = HostUtil.getHostIpAddress();
			String subj = "MessageBeanBuilder running on " + hostIp + " caught Unchecked Exception";
			String fromAddr = "MessageBeanBuilder@" + HostUtil.getHostName();
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos);
				e.printStackTrace(ps);
				try {
					baos.close();
				}
				catch (IOException e1) {
					logger.warn("IOException caught: " + e1.getMessage());
				}
				ps.close();
				if (baos.size() > 0) {
					String body = new String(baos.toByteArray());
					EmailSender.send(fromAddr, Constants.VENDER_SUPPORT_EMAIL, subj, body);
				}
			}
			catch (Throwable e2) {
				logger.error("Exception caught", e2);
			}
		}
	} // end of processAttachment

	private static void setAnodeValue(BodypartBean anode, Part p) {
		setAnodeValue(anode, p,
				"-- Message body has been omitted. Exception thrown from p.getInputStream() --");
	}

	private static void setAnodeValue(BodypartBean anode, Part p, String errmsg) {
		try {
			anode.setValue((InputStream) p.getInputStream());
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
			anode.setValue(errmsg);
		}
	}

	final static MailDateFormat mailDateFormat = new MailDateFormat();
	static java.util.Date getHeaderDate(String text) {
		if (StringUtils.isBlank(text)) return null;
		try {
			java.util.Date date = mailDateFormat.parse(text);
			return date;
		}
		catch (ParseException e) {
			logger.warn("ParseException caught parsing: " + text);
		}
		return null;
	}

	final static String rfc822Regex = "^(?:\\w{3},\\s+)??(\\d{1,2}\\s+\\w{3}\\s+\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\s+[+-]\\d{4})";
	// RFC822 Date Header samples:
	// Date: Thu, 02 Oct 2008 08:12:21 -0400 (EDT)
	// Date: Wed, 29 Oct 2008 16:24:40 +0200
	// Date: 9 Oct 2008 15:20:20 -0400
	final static Pattern rfc822Pattern = Pattern.compile(rfc822Regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	final static SimpleDateFormat rfc822Formatter = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z");
	/*
	 * @deprecated: replaced by above method that uses MailDateFormat class.
	 */
	static java.util.Date getHeaderDate_v0(String text) {
		if (StringUtils.isBlank(text)) return null;
		Matcher m = rfc822Pattern.matcher(text);
		if (m.find() && m.groupCount() >= 1) {
			String tsStr = m.group(1);
			try {
				java.util.Date date = rfc822Formatter.parse(tsStr);
				return date;
			}
			catch (ParseException e) {
				logger.warn("ParseException caught parsing: " + tsStr);
			}
		}
		return null;
	}

	/**
	 * check and reformat email addresses.
	 * 
	 * @param addr -
	 *            email addresses
	 * @return reformatted addresses
	 */
	static Address[] checkAddr(Address[] addr) {
		if (addr != null && addr.length > 0) {
			InternetAddress[] newAddr = new InternetAddress[addr.length];
			for (int j = 0; j < addr.length; j++) {
				newAddr[j].setAddress(checkAddr(addr[j].toString()));
			}
			return (Address[]) newAddr;
		}
		return addr;
	}

	/**
	 * check and reformat email address
	 * 
	 * @param s -
	 *            email address
	 * @return reformatted address
	 */
	private static String checkAddr(String s) {
		// do not append domain name by default
		return checkAddr(s, false);
	}

	/**
	 * check and reformat email address
	 * 
	 * @param s -
	 *            email address
	 * @param needDomain -
	 *            true requires domain
	 * @return reformatted address
	 */
	private static String checkAddr(String s, boolean needDomain) {
		if (s == null || s.trim().length() == 0) {
			return s;
		}
		try {
			InternetAddress.parse(s);
		}
		catch (javax.mail.internet.AddressException e) {
			logger.error("AddressException caught during parsing", e);
			return null;
		}

		String addr = s;
		// is this a name only address?
		if (needDomain && addr.indexOf("@") < 0) {
			int pos;
			if ((pos = addr.indexOf(">")) < 0) {
				// does it look like <user>? no - append default domain name
				addr += "@" + hostName;
			}
			else {
				addr = addr.substring(0, pos) + "@" + hostName + ">";
			}
		}
		return addr;
	}

	/**
	 * retrieve an email address's domain
	 * 
	 * @param addr -
	 *            address
	 * @return domain
	 */
	private static String getDomain(String addr) {
		if (isDebugEnabled)
			logger.debug("Real_TO Address: ->" + addr + "<-");
		addr = EmailAddrUtil.removeDisplayName(addr);

		int at_pos = addr.lastIndexOf("@");
		if (at_pos > 0 && addr.length() > at_pos + 1) {
			String domain = addr.substring(at_pos + 1);

			int last_dot = domain.lastIndexOf(".");
			if (last_dot > 0) {
				String addr1 = domain.substring(last_dot);
				domain = domain.substring(0, last_dot);
				last_dot = domain.lastIndexOf(".");
				String addr2 = null;
				if (last_dot > 0 && domain.length() >= last_dot + 1) {
					addr2 = domain.substring(last_dot + 1);
				}
				else {
					addr2 = domain;
				}
				addr = addr2 + addr1;
			}
			else {
				addr = domain;
			}
		}
		if (isDebugEnabled)
			logger.debug("Real_TO's domain name: ->" + addr + "<-");
		return addr.toLowerCase();
	}

	/**
	 * analyze "Received" header and retrieve address from the header
	 * 
	 * @param received -
	 *            header
	 * @return "for" address if found, null otherwise.
	 */
	private static String analyzeReceived(String received) {
		int semicolon_pos = -1;
		if (received != null && (semicolon_pos = received.indexOf(";")) > 0) {
			received = received.substring(0, semicolon_pos);
			received = received.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');

			// required fields
			int from_pos = received.indexOf("from ");
			int by_pos = received.indexOf(" by ", from_pos + 1);
			int low_pos = Math.min(from_pos, by_pos);
			int high_pos = Math.max(from_pos, by_pos);
			int max_pos = high_pos;

			// check optional fields
			int via_pos = received.indexOf(" via ", max_pos + 1);
			max_pos = Math.max(max_pos, via_pos);
			int with_pos = received.indexOf(" with ", max_pos + 1);
			max_pos = Math.max(max_pos, with_pos);
			int id_pos = received.indexOf(" id ", max_pos + 1);
			max_pos = Math.max(max_pos, id_pos);

			int for_pos = received.indexOf(" for ", max_pos + 1);
			if (low_pos >= 0 && for_pos > high_pos) {
				// rely on InternetAddress.parse() to perform syntax check.
				return received.substring(for_pos + 4);
			}
			else if (by_pos >= 0 && with_pos > by_pos && for_pos > with_pos) {
				// AOL or Google - "received" could only contain "by" and
				// "with", but no "from"
				return received.substring(for_pos + 4);
			}
			else if (low_pos >= 0 && max_pos > high_pos) {
				// found optional field
				// address may have display name and the display name may
				// contain one of the search keys used by the optional fields
				// (indexOf())
				for_pos = received.lastIndexOf(" for ");
				if (for_pos > high_pos) {
					return received.substring(for_pos + 4);
				}
			}
		}

		return null;
	}

	/**
	 * extract message information and write it to file
	 * 
	 * @param p -
	 *            part
	 * @param msgBean -
	 *            MessageBean object
	 * @throws MessagingException
	 *             if any error
	 */
	private static void addMsgStreamToBean(Part p, MessageBean msgBean)
			throws MessagingException {
		/* save the message in its raw format to the HashMap */
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			p.writeTo(baos);
			msgBean.getHashMap().put(MSG_RAW_STREAM, baos.toByteArray());
		}
		catch (IOException e) {
			logger.error("Failed to write message raw stream to HashMap", e);
		}
		finally {
			try {
				baos.close();
			}
			catch (IOException e) {
				logger.warn("IOException caught: " + e.getMessage());
			}
		}
	}

	/**
	 * locate the file name from content-type.
	 * 
	 * @param ctype -
	 *            content type
	 * @return file name extracted from the content type
	 */
	public static String getFileName(String ctype) {
		String desc = null;
		if (ctype != null && ctype.indexOf("name=") >= 0) {
			desc = ctype.substring(ctype.indexOf("name=") + 5);
			if (desc != null && desc.indexOf(";") > 0)
				desc = desc.substring(0, desc.indexOf(";"));
		}
		return desc;
	}

}
