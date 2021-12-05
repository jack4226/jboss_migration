package jpa.message.util;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpa.constant.Constants;
import jpa.constant.EmailIdToken;
import jpa.message.MsgHeader;
import jpa.model.SenderData;
import jpa.model.IdTokens;
import jpa.service.common.SenderDataService;
import jpa.spring.util.SpringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * search the input string for possible embedded message id.
 */
public final class EmailIdParser implements Serializable {
	private static final long serialVersionUID = 8659745554700580366L;
	static final Logger logger = LogManager.getLogger(EmailIdParser.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private final IdTokens id_tokens;
	
	// all patterns are defined with groups like: (A(B)C)
	private final Pattern bodyPattern;
	private final Pattern bodyHeaderPattern;
	private final Pattern headerPattern;
	
	private static EmailIdParser emailIdParser = null;

	private SenderData senderData = null;

	private EmailIdParser(IdTokens _tokens) {
		SenderDataService senderService = SpringUtil.getAppContext().getBean(SenderDataService.class);
		senderData = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		if (_tokens != null) { // not used
			this.id_tokens = _tokens;
		}
		else { // use default values from Constants class
			this.id_tokens = new IdTokens();
			this.id_tokens.setSenderData(senderData);
			this.id_tokens.setBodyBeginToken(EmailIdToken.BODY_BEGIN);
			this.id_tokens.setBodyEndToken(EmailIdToken.BODY_END);
			this.id_tokens.setXhdrBeginToken(EmailIdToken.XHDR_BEGIN);
			this.id_tokens.setXhdrEndToken(EmailIdToken.XHDR_END);
			this.id_tokens.setXheaderName(EmailIdToken.XHEADER_NAME);
			this.id_tokens.setMaxLength(EmailIdToken.MAXIMUM_LENGTH);
		}
		/*
		 * Some Email servers may insert CR/LF and tabs into Email_Id section
		 * (MS exchange server for one). Further research revealed that MS
		 * exchange server inserted "\r\n\t" into the Email_ID string, and it
		 * caused the parser to fail. The work around is to match spaces with
		 * one or more white spaces(\\s+).
		 */
		// body regular expression
		String bodyBegin = StringUtils.replace(getSenderId() + " " + getBodyBegin(), " ", "\\s+");
		bodyBegin = StringUtils.replace(bodyBegin, ".", "\\.");
		String bodyEnd = StringUtils.replace(getBodyEnd(), " ", "\\s+");
		bodyEnd = StringUtils.replace(bodyEnd, ".", "\\.");
		String bodyRegex = "(" + bodyBegin + "(\\d{1," + id_tokens.getMaxLength() + "})"
				+ bodyEnd + ")";
		bodyPattern = Pattern.compile(bodyRegex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		// end of body
		// body regular expression for x-header
		String bodyHdrBegin = StringUtils.replace(getBodyXhdrBegin(), " ", "\\s+");
		bodyHdrBegin = StringUtils.replace(bodyHdrBegin, ".", "\\.");
		String bodyHdrEnd = StringUtils.replace(getXhdrEnd(), " ", "\\s+");
		bodyHdrEnd = StringUtils.replace(bodyHdrEnd, ".", "\\.");
		String bodyHdrRegex = "(" + bodyHdrBegin + "(\\d{1," + id_tokens.getMaxLength()
				+ "})" + bodyHdrEnd + ")";
		bodyHeaderPattern = Pattern.compile(bodyHdrRegex, Pattern.DOTALL);
		// end of body for x-header
		// x-header regular expression
		String headerBegin = StringUtils.replace(getXhdrBegin(), " ", "\\s+");
		headerBegin = StringUtils.replace(headerBegin, ".", "\\.");
		String headerEnd = StringUtils.replace(getXhdrEnd(), " ", "\\s+");
		headerEnd = StringUtils.replace(headerEnd, ".", "\\.");
		String headerRegex = "\\s*(" + headerBegin + "(\\d{1," + id_tokens.getMaxLength() + "})"
				+ headerEnd + ")\\s*";
		headerPattern = Pattern.compile(headerRegex, Pattern.DOTALL);
		// end of x-header
		if (isDebugEnabled) {
			logger.debug("Regex for body 1: " + bodyRegex);
			logger.debug("Regex for body 2: " + bodyHdrRegex);
			logger.debug("Regex for header: " + headerRegex);
		}
	}
	
	public static EmailIdParser getDefaultParser() {
		if (emailIdParser == null) {
			emailIdParser = new EmailIdParser(null);
		}
		return emailIdParser;
	}

/*
	public static EmailIdParser getParser(IdTokensVo tokensVo) {
		EmailIdParser parser = new EmailIdParser(tokensVo);
		return parser;
	}
*/
	/**
	 * Search message text for embedded email id section, and return the
	 * Email_Id code.
	 * 
	 * @param msgStr -
	 *            message text
	 * @return Email_Id code or null if not found
	 * @throws NumberFormatException
	 *             if the Email_Id format is invalid
	 */
	public String parseMsg(String msgStr) throws NumberFormatException {
		if (msgStr == null || msgStr.length() == 0) {
			return null;
		}
		// Search Email Id from body. example: Email Id: 10.1234567890.0
		String keyStr = getKey(msgStr, bodyPattern);
		if (keyStr == null) {
			// Search Email Id from X-Header string: X-Email_Id: 10.300262286930.0
			// as X-Headers could be included in message body from a bounced email.
			keyStr = getKey(msgStr, bodyHeaderPattern);			
		}
		if (keyStr != null) {
			if (isDebugEnabled)
				logger.debug("parseMsg() - Encoded Email_Id found from body: " + keyStr);
			return MsgIdCipher.decode(keyStr) + "";
		}
		return null;
	}
	
	/**
	 * find Email_Id section from X-Headers and extract the embedded email id
	 * code.
	 * 
	 * @param headers -
	 *            MsgHeader list
	 * @return email id code
	 * @throws NumberFormatException
	 *             if the Email_Id format is invalid
	 */
	public String parseHeaders(List<MsgHeader> headers) throws NumberFormatException {
		for (int i = 0; headers != null && i < headers.size(); i++) {
			MsgHeader header = headers.get(i);
			String keyStr = null;
			if (Constants.VERP_BOUNCE_EMAILID_XHEADER.equals(header.getName())) {
				keyStr = parseXHeader(header.getValue());
			}
			else if (getEmailIdXHdrName().equals(header.getName())) {
				keyStr = parseXHeader(header.getValue());
			}
			if (keyStr != null) {
				if (isDebugEnabled) {
					logger.debug("parseHeaders() - Encoded Email_Id found from X-Header: "
							+ keyStr);
				}
				return MsgIdCipher.decode(keyStr) + "";
			}
		}
		return null;
	}

	/**
	 * extract the embedded email id from X-Header
	 * 
	 * @param xhdrStr
	 *            X-Header text
	 * @return Email_Id
	 * @throws NumberFormatException
	 *             if the Email_Id format is invalid
	 */
	public String parseXHeader(String xhdrStr) throws NumberFormatException {
		if (xhdrStr == null || xhdrStr.length() == 0) {
			return null;
		}
		// look for Email_Id from X-Header. example: 10.1234567890.0
		String keyStr = getKey(xhdrStr, headerPattern);
		return keyStr;
	}

	/**
	 * search message body for an embedded EmailId section
	 * 
	 * @param body -
	 *            message body
	 * @return true if an EmailId is found
	 */
	public boolean isEmailIdExist(String body) {
		if (body == null || body.trim().length() == 0) {
			return false;
		}
		// search embedded EmailId. example: Email Id: 10.1234567890.0
		Matcher matcher = bodyPattern.matcher(body);
		return matcher.find();
	}
	
	/**
	 * Replace the embedded email id with a new one. If a X-Header Email_Id
	 * section also found in the message text, remove it first.
	 * 
	 * @param msgStr
	 *            message text
	 * @param msgId
	 *            new message id
	 * @return message text with a new email_id
	 */
	public String replaceEmailId(String msgStr, int msgId) {
		if (msgStr == null || msgStr.trim().length() == 0) {
			return msgStr;
		}
		// first remove body X-Header: X-Email_Id: 10.3002622869.0
		String msgStr1 = removeKey(msgStr, bodyHeaderPattern);
		
		// second replace embedded: Email Id: 10.1234567890.0
		String msgStr2 = replaceKey(msgStr1, bodyPattern, msgId);
		return msgStr2;
	}

	/*
	 * retrieve email id code (still encoded) from message text using provided
	 * Pattern.
	 */
	private String getKey(String msg, Pattern pattern) {
		String key = null;
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {
			key = matcher.group(matcher.groupCount());
		}
		return key;
	}

	String displayAllKeys(String msg, Pattern pattern) {
		String key = null;
		Matcher matcher = pattern.matcher(msg);
		int index = 0;
		while (matcher.find(index)) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				logger.info(i + " - " + matcher.group(i));
			}
			key = matcher.group(matcher.groupCount());
			index = matcher.end();
		}
		return key;
	}
	
	/*
	 * remove Email_Id section, for example: X-Email_Id: 10.3002622869.0
	 */
	public String removeKey(String msg, Pattern pattern) {
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {
			int group = matcher.groupCount() >= 1 ? 1 : matcher.groupCount();
			msg = msg.substring(0, matcher.start(group)) + msg.substring(matcher.end(group));
		}
		return msg;
	}

	/*
	 * Find an Email_Id section in the message text, and replace the Email_Id
	 * code with a new one.
	 */
	public String replaceKey(String msg, Pattern pattern, int msgId) {
		Matcher matcher = pattern.matcher(msg);
		if (matcher.find()) {
			//msg = matcher.replaceFirst(MsgIdCipher.encode(msgId)); // not working with group
			String newEmailId = MsgIdCipher.encode(msgId);
			msg = msg.substring(0, matcher.start(matcher.groupCount())) + newEmailId
					+ msg.substring(matcher.end(matcher.groupCount()));
			if (isDebugEnabled) {
				logger.debug("in replaceKey() - Email_Id ("
						+ matcher.group(matcher.groupCount())
						+ ") was found in message body and is replaced by ("
						+ newEmailId + ").");
			}
		}
		return msg;
	}

	/**
	 * create an email id section for message body, using the provided msgId as
	 * the source of Email_Id code.
	 * 
	 * @param msgId
	 *            email_id to be wrapped
	 * @return wrapped email_id
	 */
	public String createEmailId(Integer msgId) throws NumberFormatException {
		if (msgId == null) {
			return null;
		}
		else {
			return getSenderId() + " " + getBodyBegin() + MsgIdCipher.encode(msgId) + getBodyEnd();
		}
	}
	
	/**
	 * create an email id section for X-Header, using the provided msgId as the
	 * source of Email_Id code.
	 * 
	 * @param msgId
	 *            email_id to be wrapped
	 * @return wrapped email_id
	 */
	public String createEmailId4XHdr(Integer msgId) throws NumberFormatException {
		if (msgId == null) {
			return null;
		}
		else {
			return getXhdrBegin() + MsgIdCipher.encode(msgId) + getXhdrEnd();
		}
	}

	/**
	 * return the X-Header name for Message_Id
	 * 
	 * @return X-Header name
	 */
	public String getEmailIdXHdrName() {
		return id_tokens.getXheaderName();
	}

	private String getSenderId() {
		if (senderData != null) {
			return senderData.getSenderId();
		}
		else {
			return Constants.DEFAULT_SENDER_ID;
		}
	}
	
	private String getBodyBegin() {
		return id_tokens.getBodyBeginToken();
	}
	
	private String getBodyEnd() {
		return id_tokens.getBodyEndToken();
	}
	
	private String getBodyXhdrBegin() {
		return id_tokens.getXheaderName() + ": " + id_tokens.getXhdrBeginToken();
	}
	
	private String getXhdrBegin() {
		return id_tokens.getXhdrBeginToken();
	}
	
	private String getXhdrEnd() {
		return id_tokens.getXhdrEndToken();
	}
	
	public Pattern getBodyPattern() {
		return bodyPattern;
	}

	public static void main(String[] args) {
		SpringUtil.beginTransaction();
		try {
			int msgId = 12345;
			EmailIdParser parser = EmailIdParser.getDefaultParser();
			String msgStr = "this is my message text.\n" + parser.createEmailId(msgId) + "\n...the rest";
			logger.info("Original Msg Text: " + msgStr);
			String id = parser.parseMsg(msgStr);
			logger.info("Email Id restored: " + id);
			String msgStr2 = parser.replaceEmailId(msgStr, msgId);
			logger.info("Msg Text after replace: " + msgStr2);
			logger.info("Email Id restored: " + parser.parseMsg(msgStr2));
			logger.info("Msg: " + parser.replaceKey(msgStr, parser.bodyPattern, 9876543));
			logger.info("Msg: " + parser.removeKey(msgStr, parser.bodyPattern));
			
			Matcher matcher = parser.bodyPattern.matcher("aaaaab \nSystem Email \r\tId: \n 10.123456.0 ... the rest");
			if (matcher.find()) {
				String matched = matcher.group(matcher.groupCount());
				logger.info("Matched String: " + matched + ", groups: " + matcher.groupCount());
			}
			else {
				logger.info("Pattern not matched.");
			}
		}
		finally {
			SpringUtil.commitTransaction();
		}
	}
}
