package jpa.message;

import java.util.List;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.message.util.EmailIdParser;
import jpa.util.EmailAddrUtil;
import jpa.util.SenderUtil;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Construct out-going message body
 * 
 * @author Administrator
 */
public final class MessageBodyBuilder {
	static final Logger logger = Logger.getLogger(MessageBodyBuilder.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator", "\n");

	private static final String OPEN_BODY_TAG = "<BODY";
	private static final String OPEN_HTML_TAG = "<HTML>";
	private static final String CLOSE_BODY_TAG = "</BODY>";
	private static final String CLOSE_HTML_TAG = "</HTML>";
	private static final String RIGHT_CHEVRON = ">";
	static final String MSG_DELIMITER = "----- Original Message -----";
	
	static final int MAX_OUTBOUND_BODY_SIZE = 256 * 1024;
	static final int MAX_OUTBOUND_CMPT_SIZE = 512 * 1024;

	private MessageBodyBuilder() {
		// make it static only
	}

	/**
	 * if either the reply text or the original text is HTML, the result is
	 * HTML.
	 * 
	 * @return message body
	 */
	public static String getBodyWithEmailId(MessageBean msgBean) {
		String msgBody = msgBean.getBody();
		if (msgBody != null) {
			if (msgBody.length() > MAX_OUTBOUND_BODY_SIZE) {
				msgBody = msgBody.substring(0, MAX_OUTBOUND_BODY_SIZE) + LF
						+ Constants.MESSAGE_TRUNCATED;
			}
		}
		else {
			msgBody = "";
		}
		
		MessageBean orig = msgBean.getOriginalMail();
		String origContentType = "text/plain";
		if (orig != null && orig.getBodyContentType() != null) {
			origContentType = orig.getBodyContentType();
		}
		if (msgBean.getBodyContentType().indexOf("html") >= 0
				|| origContentType.indexOf("html") >= 0) {
			// either the reply or the original message is HTML
			if (msgBean.getBodyContentType().indexOf("html") < 0) {
				// reply message is plain text
				msgBody = StringUtil.getHtmlDisplayText(msgBody);
			}
			msgBody = constructHtmlBody(msgBean, msgBody);
		}
		else {
			// plain text email
			msgBody = constructTextBody(msgBean, msgBody);
		}
		msgBody = StringUtil.trimRight(msgBody);
		return msgBody;
	}

	/**
	 * insert original message text.
	 * 
	 * @return message with original email text
	 */
	private static String constructHtmlBody(MessageBean msgBean, String msgBody) {
		if (msgBean.getEmBedEmailId() != null && msgBean.getEmBedEmailId().booleanValue()) {
			// embed Message_Id into the body (before the original message)
			msgBody = embedEmailId2Body(msgBean, msgBody, true);
			String emailId = addEmailIdToHeader(msgBean);
			if (isDebugEnabled) {
				logger.debug("in constructHtmlBody() - Email_Id (" + emailId + ") is embeded to header.");
			}
		}
		// if Email_Id not present in header, embed it anyway.
		if (msgBean.getHeader(EmailIdParser.getDefaultParser().getEmailIdXHdrName()).isEmpty()
				|| msgBean.getIsReceived()==false) {
			addEmailIdToHeader(msgBean);
		}

		String origBody = getOriginal(msgBean, true);
		String newBody = null;
		if (msgBean.getBodyContentType().indexOf("html") >= 0) {
			origBody = removeHtmlBodyTags(origBody);
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				if (Constants.EmbedPoweredByToFreeVersion) {
					newBody = appendTextToHtml(msgBody, LF + Constants.POWERED_BY_HTML_TAG + LF);
				}
			}
			newBody = appendTextToHtml(msgBody, origBody);
		}
		else { // only original is HTML
			msgBody = removeHtmlBodyTags(msgBody);
			if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
				if (Constants.EmbedPoweredByToFreeVersion) {
					msgBody += LF + Constants.POWERED_BY_HTML_TAG + LF;
				}
			}
			newBody = prependTextToHtml(origBody, msgBody);
		}
		return newBody;
	}
	
	/**
	 * insert original message text.
	 * 
	 * @return message with original email text
	 */
	private static String constructTextBody(MessageBean msgBean, String msgBody) {
		if (msgBean.getEmBedEmailId() != null && msgBean.getEmBedEmailId().booleanValue()) {
			// embed Message_Id into the body (before the original message)
			msgBody = embedEmailId2Body(msgBean, msgBody, false);
			String emailId = addEmailIdToHeader(msgBean);
			if (isDebugEnabled) {
				logger.debug("in constructTextBody() - Email_Id (" + emailId + ") is embeded to header.");
			}
		}
		// if Email_Id not present in header, embed it anyway.
		if (msgBean.getHeader(EmailIdParser.getDefaultParser().getEmailIdXHdrName()).isEmpty()
				|| msgBean.getIsReceived()==false) {
			addEmailIdToHeader(msgBean);
		}

		if (!SenderUtil.isProductKeyValid() && SenderUtil.isTrialPeriodEnded()) {
			if (Constants.EmbedPoweredByToFreeVersion) {
				msgBody += Constants.CRLF + Constants.CRLF + Constants.POWERED_BY_TEXT + Constants.CRLF;
			}
		}
		if (msgBean.getOriginalMail() != null) { // is a reply
			msgBody += getOriginal(msgBean, false);
		}
		return msgBody;
	}

	/*
	 * Embed EmailId into the message body and X-Header. If an EmailId already
	 * exist in the message body, replace to with the EmailId.
	 * 
	 * @param msgBean -
	 *            MessageBean object
	 * @param body - body text
	 * @return body text with Email_Id embedded.
	 */
	private static String embedEmailId2Body(MessageBean msgBean, String body, boolean isHtml) {
		if (msgBean.getMsgId() == null) return body;
		if (EmailIdParser.getDefaultParser().isEmailIdExist(body)) {
			// Replace existing EmailId with a new one
			body = EmailIdParser.getDefaultParser().replaceEmailId(body, msgBean.getMsgId());
		}
		else { // Embed Email_Id into message body
			String emailIdSec = getEmailIdSection(msgBean, isHtml);
			int pos1 = body.indexOf(Constants.MSG_DELIMITER_BEGIN);
			int pos2 = body.indexOf(Constants.MSG_DELIMITER_END, pos1 + 1);
			if (pos1 >=0 && pos2 > pos1 && (pos2 - pos1) <= 255) {
				// a reply email, insert Email_Id before the original message
				body = body.substring(0, pos1) + emailIdSec + (isHtml ? "<br>" : Constants.CRLF)
						+ body.substring(pos1);
			}
			else {
				// a new email, append Email_Id to the end
				if (isHtml) {
					body = appendTextToHtml(body, emailIdSec);
				}
				else {
					body += emailIdSec;
				}
			}
			if (isDebugEnabled) {
				logger.debug("in embedEmailId2Body() - Email_Id (" + StringUtils.trim(emailIdSec) + ") is embeded to body.");
			}
		}
		
		return body;
	}
	
	public static String addEmailIdToHeader(MessageBean msgBean) {
		if (msgBean.getMsgId() == null) return null;
		// Embed Email_Id to X-Header
		MsgHeader header = new MsgHeader();
		String headerName = EmailIdParser.getDefaultParser().getEmailIdXHdrName();
		header.setName(headerName);
		String emailId = EmailIdParser.getDefaultParser().createEmailId4XHdr(msgBean.getMsgId());
		header.setValue(emailId);
		List<MsgHeader> list = msgBean.getHeaders();
		// first remove old Email_Id X-Header from the header list
		removeEmailIdFromHeader(msgBean);
		// now add the new header to the list
		list.add(header);
		return emailId;
	}

	public static void removeEmailIdFromHeader(MessageBean msgBean) {
		List<MsgHeader> list = msgBean.getHeaders();
		String headerName = EmailIdParser.getDefaultParser().getEmailIdXHdrName();
		for (int i = 0; i < list.size(); i++) {
			MsgHeader hdr = list.get(i);
			if (headerName.equals(hdr.getName())) {
				list.remove(hdr);
			}
		}
	}
	
	/**
	 * construct original message.
	 * 
	 * @param isHtml -
	 *            true if HTML message, false plain text message
	 * @return original message
	 */
	private static String getOriginal(MessageBean msgBean, boolean isHtml) {
		if (msgBean.getOriginalMail() == null) {
			return "";
		}
		if (CarrierCode.WEBMAIL.equals(msgBean.getOriginalMail().getCarrierCode())) {
			// drop the original message if it's a WebMail
			return "";
		}
		else {
			String header = constructOriginalHeader(msgBean, isHtml);
			String origBody = msgBean.getOriginalMail().getBody();
			if (isHtml) {
				String origType = msgBean.getOriginalMail().getBodyContentType();
				if (origType != null && origType.indexOf("html") < 0) {
					// original message is plain text, add PRE tags
					origBody = StringUtil.getHtmlDisplayText(origBody);
				}
				// insert headers after the <BODY> or <HTML> tag
				return prependTextToHtml(origBody, header);
			}
			else {
				return header + origBody;
			}
		}
	}

	/**
	 * construct headers of the original message.
	 * 
	 * @param isHtml -
	 *            true if HTML message, false plain text message
	 * @return original message headers
	 */
	public static String constructOriginalHeader(MessageBean msgBean, boolean isHtml) {
		String headers = "";
		MessageBean origMsg = msgBean.getOriginalMail();
		if (origMsg == null) { // not a reply
			return headers;
		}
		if (isHtml) { // HTML text
			headers = LF + "<p><br>" + Constants.MSG_DELIMITER_BEGIN + origMsg.getFromAsString()
					+ Constants.MSG_DELIMITER_END + "<br><br>" + LF;
			headers += "&gt; From: " + origMsg.getFromAsString() + "<br>" + LF;
			headers += "&gt; To: " + origMsg.getToAsString() + "<br>" + LF;
			headers += "&gt; Date: " + origMsg.getSendDate() + "<br>" + LF;
			headers += "&gt; Subject: " + origMsg.getSubject() + "<br>" + LF;
			headers += "<br>" + Constants.DASHES_OF_33 + "<br></p>" + LF;
		}
		else { // plain text
			headers = Constants.CRLF + Constants.CRLF + Constants.MSG_DELIMITER_BEGIN
					+ origMsg.getFromAsString() + Constants.MSG_DELIMITER_END + Constants.CRLF
					+ Constants.CRLF;
			headers += "> From: " + origMsg.getFromAsString() + Constants.CRLF;
			headers += "> To: " + origMsg.getToAsString() + Constants.CRLF;
			headers += "> Date: " + origMsg.getSendDate() + Constants.CRLF;
			headers += "> Subject: " + origMsg.getSubject() + Constants.CRLF;
			headers += Constants.CRLF + Constants.DASHES_OF_33 + Constants.CRLF;
		}
		return headers;
	}

	/**
	 * generate Email_Id section.
	 * 
	 * @param isHtml -
	 *            true if HTML message, false plain text message
	 * @return String with Email_Id
	 */
	private static String getEmailIdSection(MessageBean msgBean, boolean isHtml) {
		String section = "";
		if (!CarrierCode.WEBMAIL.equals(msgBean.getCarrierCode())
				&& msgBean.getMsgId() != null) {
			if (isHtml) {
				section += LF + "<div style='color: darkgray;'><p>";
				section += EmailIdParser.getDefaultParser().createEmailId(msgBean.getMsgId())
						+ "</p></div>" + LF;
			}
			else {
				section += Constants.CRLF + Constants.CRLF
						+ EmailIdParser.getDefaultParser().createEmailId(msgBean.getMsgId());
			}
			if (isDebugEnabled) {
				logger.debug("getEmailIdSection() - MsgId: " + msgBean.getMsgId()
						+ ", EmailId: " + EmailAddrUtil.removeCRLFTabs(section));
			}
		}
		return section;
	}

	/**
	 * Add the newText to the top of the origText, after the <BODY> or <HTML> tag.
	 * 
	 * @param origText -
	 *            original text
	 * @param newText -
	 *            new message text
	 * @return message with new text added to the top
	 */
	private static String prependTextToHtml(String origText, String newText) {
		// make sure the HTML tag is there
		String origBody = checkHtmlTag(origText); 
		int pos1 = 0, pos2 = 0;

		String origBodyUpperCase = origBody.toUpperCase();

		// first locate the body tag
		// locate "<BODY"
		pos1 = origBodyUpperCase.indexOf(OPEN_BODY_TAG);
		if (pos1 >= 0) {
			// locate ">"
			pos2 = origBodyUpperCase.indexOf(RIGHT_CHEVRON, pos1 + 1);
		}
		if (pos1 < 0 || pos2 <= 0) { 
			// the BODY tag is missing, locate HTML tag	
			// locate <HTML>
			pos2 = origBodyUpperCase.indexOf(OPEN_HTML_TAG);
			// shift right to the end of the tag
			pos2 = pos2 + OPEN_HTML_TAG.length() - 1;
		}

		StringBuffer sb = new StringBuffer();
		// + from beginning to the Tag location
		sb.append(origBody.substring(0, pos2 + 1));
		// + reply Text
		sb.append(newText);
		// + the rest of the original text, from the Tag location to the end
		sb.append(origBody.substring(pos2 + 1));

		return sb.toString();
	}

	/**
	 * Append the newText to the bottom of the origText, before the </BODY> or
	 * </HTML> tag.
	 * 
	 * @param origText -
	 *            original text
	 * @param newText -
	 *            new message text
	 * @return message with new text added to the bottom
	 */
	private static String appendTextToHtml(String origText, String newText) {
		// make sure the HTML tag is there
		String origBody = checkHtmlTag(origText);
		int pos1 = 0;

		String origBodyUpperCase = origBody.toUpperCase();

		// first locate the closing body tag
		// locate "</BODY>"
		pos1 = origBodyUpperCase.indexOf(CLOSE_BODY_TAG);
		if (pos1 < 0) { 
			// the </BODY> tag is missing, locate closing HTML tag	
			// locate </HTML>
			pos1 = origBodyUpperCase.indexOf(CLOSE_HTML_TAG);
		}

		StringBuffer sb = new StringBuffer();
		// + from beginning to the Tag location
		sb.append(origBody.substring(0, pos1));
		// + reply text
		sb.append(newText);
		// + the rest of the original text, from the Tag location to the end
		sb.append(origBody.substring(pos1));

		return sb.toString();
	}

	/**
	 * remove opening and closing HTML/BODY tags from bodyStr.
	 * 
	 * @param bodyStr -
	 *            message text
	 * @return string w/o HTML and body tags
	 */
	public static String removeHtmlBodyTags(String body) {
		body = removeString(body, "<BODY", RIGHT_CHEVRON);
		body = removeString(body, "</BODY", RIGHT_CHEVRON);
		body = removeString(body, "<HTML", RIGHT_CHEVRON);
		body = removeString(body, "</HTML", RIGHT_CHEVRON);
		return body;
	}

	/**
	 * check the opening HTML tag, and add it if missing.
	 * 
	 * @param bodyStr -
	 *            message text
	 * @return string with HTML tag
	 */
	private static String checkHtmlTag(String body) {
		if (body==null) body = "";
		
		if (body.toUpperCase().indexOf(OPEN_HTML_TAG) < 0
				|| body.toUpperCase().indexOf(CLOSE_HTML_TAG) < 0) {
			// remove HTML and body tags, just for safety
			body = removeHtmlBodyTags(body);
			// add missing HTML tags
			body = "<HTML><BODY>" + body + "</BODY></HTML>";
		}
		return body;
	}

	/**
	 * remove string that starts with startStr and ends with endStr from
	 * bodyStr.
	 * 
	 * @param bodyStr -
	 *            message text
	 * @param startStr -
	 *            start with
	 * @param endStr -
	 *            end with
	 * @return new string
	 */
	private static String removeString(String body, String startStr, String endStr) {
		if (body==null) return "";
		
		String bodyUpperCase = body.toUpperCase();
		int start_pos = 0, end_pos = 0;

		try {
			start_pos = bodyUpperCase.indexOf(startStr.toUpperCase());
			end_pos = bodyUpperCase.indexOf(endStr.toUpperCase(), start_pos
					+ startStr.length());
			if (start_pos >= 0 && end_pos > start_pos) {
				String body1 = body.substring(0, start_pos);

				if ((end_pos + 1) <= body.length())
					body = body1 + body.substring(end_pos + 1);
				else
					body = body1;
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during RemoveString()", e);
		}
		return body;
	}
	
	public static void main(String[] args) {
		String origText = "<HTML>This is the original message.</HTML>";
		String newText = "This is the new Text.";
		String str = appendTextToHtml(origText, newText);
		logger.info("Append: "+ str);
		str = prependTextToHtml(origText, newText);
		logger.info("Prepend: "+ str);
		
		EmailIdParser parser = EmailIdParser.getDefaultParser();
		String emailIdStr = parser.createEmailId(123456);

		// embed email_id for HTML email
		MessageBean msgBean = new MessageBean();
		msgBean.setContentType("text/html");
		msgBean.setMsgId(Integer.valueOf(999999));
		msgBean.setSubject("Test Embedding Email_Id 1");
		msgBean.setBody("<HTML>This is the test message with no Email_Id in the body.</HTML>");
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		msgBean.setBody(getBodyWithEmailId(msgBean));
		logger.info(">>>>>>>>>>>>>>>>HTML Message:" + LF + msgBean);

		// embed email_id for plain text email
		msgBean = new MessageBean();
		msgBean.setContentType("text/plain");
		msgBean.setMsgId(Integer.valueOf(999999));
		msgBean.setSubject("Test Embedding Email_Id 2");
		msgBean.setBody("This is the test message that has an existing Email_Id\n"
				+ Constants.MSG_DELIMITER_BEGIN
				+ emailIdStr
				+ Constants.MSG_DELIMITER_END
				+ "\nand is replaced by a new one.");
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		msgBean.setEmBedEmailId(Boolean.TRUE);
		msgBean.setBody(getBodyWithEmailId(msgBean));
		logger.info(">>>>>>>>>>>>>>>>TEXT Message:" + LF +msgBean);

		// parse email_id
		String msgId = parser.parseMsg(msgBean.getBody());
		logger.info("Email_Id from Body: " + msgId);
		msgId = parser.parseHeaders(msgBean.getHeaders());
		logger.info("Email_Id from X-Header: " + msgId);
		
		msgBean = new MessageBean();
		msgBean.setContentType("text/plain");
		msgBean.setMsgId(Integer.valueOf(999999));
		msgBean.setSubject("Test Embedding Email_Id 3");
		msgBean.setBody("This is the test message with embedded Email_Id.");
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL); 
		msgBean.setEmBedEmailId(Boolean.TRUE);
		msgBean.setBody(getBodyWithEmailId(msgBean));
		logger.info(">>>>>>>>>>>>>>>>TEXT Message:" + LF +msgBean);
	}
}
