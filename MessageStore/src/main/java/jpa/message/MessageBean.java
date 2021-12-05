package jpa.message;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.mail.Address;

import jpa.constant.CarrierCode;
import jpa.util.HtmlConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.htmlparser.util.ParserException;

/**
 * this class holds all the properties and data of an email message, including
 * attachments if there are any. It is designed to work with both Internet email
 * and other mail types.
 */
public final class MessageBean extends BodypartBean implements java.io.Serializable {
	private static final long serialVersionUID = -7651754840464120630L;
	static final Logger logger = LogManager.getLogger(MessageBean.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	private Address[] from, to, cc, bcc, replyto, forward;
	private Address[] toEnvelope;
	private String returnPath;

	private String[] xmailer, priority;

	private String smtpMessageId; // SMTP message id

	private String subject;

	// 'S' = 'SMTP Email'
	// 'W' = 'Web Mail'
	private CarrierCode carrierCode;
	
	private boolean internalOnly = false;
	private boolean useSecureServer = false;
	private boolean isReceived = false;
	private Integer purgeAfter = null;

	private java.util.Date date;

	private int attachCount = 0;

	// TODO add messageFolder field
	
	// to be set by BodypartUtil.retrieveAttachments()
	private MessageNode rfc822, report;
	private List<MessageNode> attachments; // list of MessageNode
	// end
	
	// to be set by MessageParserBo.parse()
	private String origRcpt, finalRcpt, dsnAction, dsnStatus;
	private String origSubject, diagnosticCode, rfcMessageId;
	private String dsnRfc822, dsnText, dsnDlvrStat;
	//end
	
	// properties about the mailbox
	private String mailboxHost, mailboxUser, mailboxName, folderName;

	// MsgId of a MsgInbox record, set by MailSender. Not used by incoming
	// e-mails
	private Integer msgId;
	
	// RenderId of MsgRendered record, set by renderer engine.
	private Integer renderId;

	// Embed email IdToken into out-going email message, set by MailSender. Not
	// used by incoming e-mails
	private Boolean emBedEmailId = null; // set by application to override system default

	// Save email raw stream to database if it is true.
	private boolean saveMsgStream = true; // default is true
	
	// EmailId obtained from scanning incoming email, or set by rendering
	// or mail delivery programs. A reference to a MsgInbox record.
	private Integer msgRefId;

	// email type, set by RuleEngine
	private String ruleName = null;

	// holds the information about templates, sender, etc.
	// for incoming: derived from msgRefId -> msgId of a MsgInbox record
	private String msgSourceId;

	// incoming: derived from msgRefId
	private String senderId, subrId;

	// the original email if this is a reply
	private MessageBean origMail;

	// convert HTML to plain text, and tells the getBody() method to return
	// plain text version of the email
	private boolean toPlainText = false;

	// TO address is a testing address so no masking is needed, out-going only.
	private boolean overrideTestAddr = false;

	// Mailing list id used for broadcasting
	private String mailingListId = null;
	private boolean toSubscribersOnly = false;
	private boolean toProspectsOnly = false;

	// can be used to store extra string properties w/o code change
	private final Properties properties;

	// can be used to store extra objects w/o code change
	private final Map<String, Object> hashMap;

	// for incoming only, stores sizes of each attachment.
	private final List<Integer> componentsSize;

	// name used to store message body to the hashMap
	public static final String MSG_BODY_TEXT = "msg_body_text";

	final static String LF = System.getProperty("line.separator", "\n");

	/**
	 * default constructor
	 */
	public MessageBean() {
		properties = new Properties();
		hashMap = new HashMap<String, Object>();
		componentsSize = new ArrayList<Integer>();
	}

	private String addrToString(Address[] addr) {
		if (addr == null || addr.length == 0)
			return null;

		String str = addr[0].toString();
		for (int i = 1; i < addr.length; i++) {
			str = str + "," + addr[i].toString();
		}
		return str;
	}

	/* all getters start from here */
	/**
	 * @return from address as an Address arrayNode
	 */
	public Address[] getFrom() {
		return this.from;
	}

	/**
	 * @return from address as a string
	 */
	public String getFromAsString() {
		return addrToString(this.from);
	}

	/**
	 * @return to address as an Address arrayNode
	 */
	public Address[] getTo() {
		return this.to;
	}

	/**
	 * @return to address as a string
	 */
	public String getToAsString() {
		return addrToString(this.to);
	}

	/**
	 * @return cc address as an Address arrayNode
	 */
	public Address[] getCc() {
		return this.cc;
	}

	/**
	 * @return cc address as a string
	 */
	public String getCcAsString() {
		return addrToString(this.cc);
	}

	/**
	 * @return bcc address as an Address arrayNode
	 */
	public Address[] getBcc() {
		return this.bcc;
	}

	/**
	 * @return bcc address as a string
	 */
	public String getBccAsString() {
		return addrToString(this.bcc);
	}

	/**
	 * @return replyto address as an Address arrayNode
	 */
	public Address[] getReplyto() {
		return this.replyto;
	}

	/**
	 * @return replyto address as a string
	 */
	public String getReplytoAsString() {
		return addrToString(this.replyto);
	}

	/**
	 * @return forward address as an Address arrayNode
	 */
	public Address[] getForward() {
		return this.forward;
	}

	/**
	 * @return forward address as a string
	 */
	public String getForwardAsString() {
		return addrToString(this.forward);
	}

	public String getReturnPath() {
		return returnPath;
	}

	public Address[] getToEnvelope() {
		return this.toEnvelope;
	}

	String getToEnvelopeAsString() {
		return addrToString(this.toEnvelope);
	}

	/**
	 * @return x_mailer as a string arrayNode
	 */
	public String[] getXmailer() {
		return this.xmailer;
	}

	/**
	 * @return x-priority as a string arrayNode
	 */
	public String[] getPriority() {
		return this.priority;
	}

	/**
	 * @return SMTP message id as a string
	 */
	public String getSmtpMessageId() {
		return this.smtpMessageId;
	}

	/**
	 * @return subject as a string
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @return send DateField
	 */
	public java.util.Date getSendDate() {
		return date;
	}

	/**
	 * Get Body Content Type
	 * 
	 * @return content type. "text/plain" is returned if getToPlainText()
	 *         returns true.
	 */
	public String getBodyContentType() {
		return getBodyContentType(false);
	}

	/**
	 * @param original -
	 *            When true, this method returns the original content type. When
	 *            false, "text/plain" is returned if getToPlainText() returns
	 *            true.
	 * @return content type of the body
	 */
	public String getBodyContentType(boolean original) {
		String type = getBodyContentType(0);
		if (type == null || type.trim().length() == 0) {
			type = DEFAULT_CONTENT_TYPE;
		}
		else if (!original && getToPlainText() && type.toLowerCase().indexOf("text/html") >= 0) {
			type = replace(type, "text/html", "text/plain");
			//type.replaceFirst("text/html", "text/plain");
		}
		return type;
	}

	private String replace(String text, String from, String to) {
		StringBuffer sb = new StringBuffer(text);
		int pos = text.toLowerCase().indexOf(from);
		if (pos < 0)
			return text;
		else {
			StringBuffer sb2 = sb.replace(pos, pos + from.length(), to);
			return sb2.toString();
		}
	}

	/**
	 * This method will return a plain text version of HTML email if
	 * getToPlainText() method returns true.
	 * 
	 * @return message body
	 */
	public String getBody() {
		return getBody(false);
	}

	/**
	 * This method will return a plain text version of HTML email if the input
	 * is false and getToPlainText() method returns true.
	 * 
	 * @param original -
	 *            When false, return as plain text, otherwise no conversion is
	 *            performed.
	 * @return the email body
	 */
	public String getBody(boolean original) {
		String msgBody = getBody(0, hashMap);
		
		if (!original && getToPlainText()
				&& getBodyContentType(0).toLowerCase().indexOf("text/html") >= 0) {
			// convert HTML to plain text if "to_plain_text" is "yes"
			if (!hashMap.containsKey(MSG_BODY_TEXT)) { // use a cache
				try {
					HtmlConverter html2text = HtmlConverter.getInstance();
					String textBody = html2text.convertToText(msgBody).trim();
					hashMap.put(MSG_BODY_TEXT, textBody);
					if (isDebugEnabled) {
						logger.debug("Converted to plain text:" + LF + textBody);
					}
				}
				catch (ParserException e) {
					logger.error("Failed to convert html to plain text, use original content.");
					logger.error("Original message body: ----->" + LF + msgBody + LF + "<-----");
					logger.error("ParserException caught", e);
					return msgBody;
				}
				catch (Throwable e) { // just for safety
					logger.error("Throwable caught during Html->Text conversion, use original.");
					logger.error("Throwable caught", e);
					return msgBody;
				}
			}
			return (String) hashMap.get(MSG_BODY_TEXT);
		}

		return msgBody;
	}

	/**
	 * get a BodypartBean that holds message body data
	 * @return a BodypartBean
	 */
	public BodypartBean getBodyNode() {
		return getBodyNode(0);
	}
	
	/**
	 * @return the number of attachments.
	 */
	public int getAttachCount() {
		return attachCount;
	}

	/**
	 * @return the mailbox host name
	 */
	public String getMailboxHost() {
		return mailboxHost;
	}

	/**
	 * @return the mailbox user id
	 */
	public String getMailboxUser() {
		return mailboxUser;
	}

	/**
	 * @return the mailbox box name
	 */
	public String getMailboxName() {
		return mailboxName;
	}

	/**
	 * @return the mailbox folder name
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * @return carrierCode(delivery method) as a string
	 */
	public CarrierCode getCarrierCode() {
		return carrierCode;
	}

	/**
	 * @return message id
	 */
	public Integer getMsgId() {
		return msgId;
	}

	public Integer getRenderId() {
		return renderId;
	}

	/**
	 * @return emBedEmailid
	 */
	public Boolean getEmBedEmailId() {
		return emBedEmailId;
	}

	/**
	 * @return saveMsgStream
	 */
	public boolean getSaveMsgStream() {
		return saveMsgStream;
	}

	/**
	 * @return reference message id
	 */
	public Integer getMsgRefId() {
		return msgRefId;
	}

	/**
	 * @return message type id
	 */
	public String getRuleName() {
		return ruleName;
	}

	/**
	 * @return message source id
	 */
	public String getMsgSourceId() {
		return msgSourceId;
	}

	/**
	 * @return sender id
	 */
	public String getSenderId() {
		return senderId;
	}

	/**
	 * @return subscriber id
	 */
	public String getSubrId() {
		return subrId;
	}

	/**
	 * @return a MessageBean
	 */
	public MessageBean getOriginalMail() {
		return origMail;
	}

	/**
	 * @return toPlainText
	 */
	public boolean getToPlainText() {
		return toPlainText;
	}

	/**
	 * @return overrideTestAddr
	 */
	public boolean getOverrideTestAddr() {
		return overrideTestAddr;
	}

	public String getMailingListId() {
		return mailingListId;
	}
	
	public boolean getToSubscribersOnly() {
		return toSubscribersOnly;
	}
	
	public boolean getToProspectsOnly() {
		return toProspectsOnly;
	}

	/**
	 * @return rfc822
	 */
	public MessageNode getRfc822() {
		return rfc822;
	}

	/**
	 * @return report
	 */
	public MessageNode getReport() {
		return report;
	}

	/**
	 * @return attachments
	 */
	public List<MessageNode> getAttachments() {
		return attachments;
	}

	public String getRfcMessageId() {
		return rfcMessageId;
	}

	/**
	 * @return Properties object
	 */
	public Properties getProperties() {
		return properties;
	}

	/**
	 * @return Hash-table object
	 */
	public Map<String, Object> getHashMap() {
		return hashMap;
	}

	public List<Integer> getComponentsSize() {
		return componentsSize;
	}
	
	/* all setters start from here */

	/**
	 * set from address from an Address arrayNode
	 * 
	 * @param from
	 */
	public void setFrom(Address[] from) {
		this.from = from;
	}

	/**
	 * set to address from an Address arrayNode
	 * 
	 * @param to
	 */
	public void setTo(Address[] to) {
		this.to = to;
	}

	/**
	 * set cc address from an Address arrayNode
	 * 
	 * @param cc
	 */
	public void setCc(Address[] cc) {
		this.cc = cc;
	}

	/**
	 * set bcc address from an Address arrayNode
	 * 
	 * @param bcc
	 */
	public void setBcc(Address[] bcc) {
		this.bcc = bcc;
	}

	/**
	 * set replyto address from an Address arrayNode
	 * 
	 * @param replyto
	 */
	public void setReplyto(Address[] replyto) {
		this.replyto = replyto;
	}

	/**
	 * set forward address from an Address arrayNode
	 * 
	 * @param forward
	 */
	public void setForward(Address[] forward) {
		this.forward = forward;
	}

	public void setReturnPath(String returnPath) {
		this.returnPath = returnPath;
	}
	
	/**
	 * set x-mailer from a string arrayNode
	 * 
	 * @param xmailer
	 */
	public void setXmailer(String[] xmailer) {
		this.xmailer = xmailer;
	}

	/**
	 * set x-priority from a string arrayNode
	 * 
	 * @param priority
	 */
	public void setPriority(String[] priority) {
		this.priority = priority;
	}

	/**
	 * set SMTP message id
	 * 
	 * @param smtpMessageId
	 */
	public void setSmtpMessageId(String smtpMessageId) {
		this.smtpMessageId = smtpMessageId;
	}

	/**
	 * set subject from a string
	 * 
	 * @param subject
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * set send DateField
	 * 
	 * @param DateField
	 */
	public void setSendDate(java.util.Date date) {
		this.date = date;
	}

	/**
	 * set mailbox host
	 * 
	 * @param mailboxHost
	 */
	public void setMailboxHost(String mailboxHost) {
		this.mailboxHost = mailboxHost;
	}

	/**
	 * set mailbox user id
	 * 
	 * @param mailboxUser
	 */
	public void setMailboxUser(String mailboxUser) {
		this.mailboxUser = mailboxUser;
	}

	/**
	 * set mailbox box name
	 * 
	 * @param mailboxName
	 */
	public void setMailboxName(String mailboxName) {
		this.mailboxName = mailboxName;
	}

	/**
	 * set mailbox folder name
	 * 
	 * @param folderName
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	/**
	 * set carrierCode from a string
	 * 
	 * @param carrierCode
	 */
	public void setCarrierCode(CarrierCode carrierCode) {
		this.carrierCode = carrierCode;
	}

	/**
	 * set message id
	 * 
	 * @param msgId
	 */
	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}

	public void setRenderId(Integer renderId) {
		this.renderId = renderId;
	}

	/**
	 * set emBedEmailId
	 * 
	 * @param emBedEmailId
	 */
	public void setEmBedEmailId(Boolean emBedEmailId) {
		this.emBedEmailId = emBedEmailId;
	}

	/**
	 * set saveMsgStream
	 * 
	 * @param saveMsgStream
	 */
	public void setSaveMsgStream(boolean saveMsgStream) {
		this.saveMsgStream = saveMsgStream;
	}

	/**
	 * set reference message id
	 * 
	 * @param outRefMsgId
	 */
	public void setMsgRefId(Integer msgRefId) {
		this.msgRefId = msgRefId;
	}

	/**
	 * set message rule name
	 * 
	 * @param ruleName
	 */
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	/**
	 * set message source id
	 * 
	 * @param msgSourceId
	 */
	public void setMsgSourceId(String msgSourceId) {
		this.msgSourceId = msgSourceId;
	}

	/**
	 * set sender id
	 * 
	 * @param senderId
	 */
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	/**
	 * set subscriber id
	 * 
	 * @param subrId
	 */
	public void setSubrId(String subrId) {
		this.subrId = subrId;
	}

	/**
	 * set original MessageBean
	 * 
	 * @param origMail
	 */
	public void setOriginalMail(MessageBean origMail) {
		this.origMail = origMail;
	}

	/**
	 * set toPlainText
	 * 
	 * @param toPlainText
	 */
	public void setToPlainText(boolean toPlainText) {
		this.toPlainText = toPlainText;
	}

	/**
	 * set overrideTestAddr
	 * 
	 * @param overrideTestAddr
	 */
	public void setOverrideTestAddr(boolean overrideTestAddr) {
		this.overrideTestAddr = overrideTestAddr;
	}

	public void setMailingListId(String mailingListId) {
		this.mailingListId = mailingListId;
	}
	
	public void setToSubscribersOnly(boolean toSubscribersOnly) {
		this.toSubscribersOnly = toSubscribersOnly;
	}
	
	public void setToProspectsOnly(boolean toProspectsOnly) {
		this.toProspectsOnly = toProspectsOnly;
	}
	
	/**
	 * set rfc822
	 * 
	 * @param rfc822
	 */
	public void setRfc822(MessageNode rfc822) {
		this.rfc822 = rfc822;
	}

	/**
	 * set report
	 * 
	 * @param report
	 */
	public void setReport(MessageNode report) {
		this.report = report;
	}

	/**
	 * set attachments
	 * 
	 * @param attachments
	 */
	public void setAttachments(List<MessageNode> attachments) {
		this.attachments = attachments;
	}

	/**
	 * set Message Body, a convenient method, not recommended in production code
	 * 
	 * @param body
	 */
	public void setBody(String body) {
		setValue(body);
	}

	public String getDiagnosticCode() {
		return diagnosticCode;
	}

	public void setDiagnosticCode(String diagnosticCode) {
		this.diagnosticCode = diagnosticCode;
	}

	public String getDsnAction() {
		return dsnAction;
	}

	public void setDsnAction(String dsnAction) {
		this.dsnAction = dsnAction;
	}

	public String getDsnDlvrStat() {
		return dsnDlvrStat;
	}

	public void setDsnDlvrStat(String dsnDlvrStat) {
		this.dsnDlvrStat = dsnDlvrStat;
	}

	public String getDsnRfc822() {
		return dsnRfc822;
	}

	public void setDsnRfc822(String dsnRfc822) {
		this.dsnRfc822 = dsnRfc822;
	}

	public String getDsnStatus() {
		return dsnStatus;
	}

	public void setDsnStatus(String dsnStatus) {
		this.dsnStatus = dsnStatus;
	}

	public String getDsnText() {
		return dsnText;
	}

	public void setDsnText(String dsnText) {
		this.dsnText = dsnText;
	}

	public String getFinalRcpt() {
		return finalRcpt;
	}

	public void setFinalRcpt(String finalRcpt) {
		this.finalRcpt = finalRcpt;
	}

	public String getOrigRcpt() {
		return origRcpt;
	}

	public void setOrigRcpt(String origRcpt) {
		this.origRcpt = origRcpt;
	}

	public String getOrigSubject() {
		return origSubject;
	}

	public void setOrigSubject(String origSubject) {
		this.origSubject = origSubject;
	}

	public void setToEnvelope(Address[] toEnvelope) {
		this.toEnvelope = toEnvelope;
	}

	public void setRfcMessageId(String dsnMessageId) {
		this.rfcMessageId = dsnMessageId;
	}
	
	public boolean isUseSecureServer() {
		return useSecureServer;
	}

	public void setUseSecureServer(boolean useSecureSmtpServer) {
		this.useSecureServer = useSecureSmtpServer;
	}

	public boolean isInternalOnly() {
		return internalOnly;
	}

	public void setInternalOnly(boolean internalOnly) {
		this.internalOnly = internalOnly;
	}
	
	public boolean getIsReceived() {
		return isReceived;
	}

	public void setIsReceived(boolean isReceived) {
		this.isReceived = isReceived;
	}
	
	public Integer getPurgeAfter() {
		return purgeAfter;
	}

	public void setPurgeAfter(Integer purgeAfter) {
		this.purgeAfter = purgeAfter;
	}
	
	/**
	 * update the counter of attachments
	 * 
	 * @param attachCount
	 */
	public synchronized void updateAttachCount(int attachCount) {
		this.attachCount = this.attachCount + attachCount;
	}
	
	/**
	 * clear up all parameters
	 */
	public void clearParameters() {
		super.clearParameters();
		from = null;
		to = null;
		cc = null;
		bcc = null;
		replyto = null;
		forward = null;
		returnPath = null;
		toEnvelope = null;
		xmailer = null;
		priority = null;
		smtpMessageId = null;
		subject = null;

		date = null;
		synchronized (this) {
			attachCount = 0;
		}
		mailboxHost = null;
		mailboxUser = null;
		mailboxName = null;
		folderName = null;
		carrierCode = null;
		internalOnly = false;
		useSecureServer = false;
		isReceived = false;
		purgeAfter = null;

		msgId = null;
		renderId = null;
		emBedEmailId = null;
		saveMsgStream = true;
		msgRefId = null;
		ruleName = null;
		msgSourceId = null;
		senderId = null;
		subrId = null;
		origMail = null;
		toPlainText = false;
		overrideTestAddr = false;
		mailingListId = null;
		toSubscribersOnly = false;
		rfc822 = null;
		report = null;
		attachments = null;
		properties.clear();
		hashMap.clear();
		componentsSize.clear();

		origRcpt=null;
		finalRcpt=null;
		dsnAction=null;
		dsnStatus=null;
		origSubject=null;
		diagnosticCode=null;
		dsnText=null;
		dsnRfc822=null;
		dsnDlvrStat=null;
		rfcMessageId=null;
	}

	/**
	 * release all references to the object
	 */
	public void destroy() {
		super.destroy();
		this.clearParameters();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (from != null)
			sb.append("From: " + getFromAsString() + LF);
		if (to != null)
			sb.append("To: " + getToAsString() + LF);
		if (toEnvelope != null)
			sb.append("To from Envelope: " + getToEnvelopeAsString() + LF);
		if (cc != null)
			sb.append("Cc: " + getCcAsString() + LF);
		if (bcc != null)
			sb.append("Bcc: " + getBccAsString() + LF);
		if (replyto != null)
			sb.append("Replyto: " + getReplytoAsString() + LF);
		if (forward != null)
			sb.append("Forward: " + getForwardAsString() + LF);
		if (returnPath != null)
			sb.append("Return-Path: " + returnPath + LF);

		if (xmailer != null && xmailer.length > 0) {
			for (int i = 0; i < xmailer.length; i++)
				sb.append("X-Mailer: " + xmailer[i] + LF);
		}
		if (priority != null && priority.length > 0) {
			for (int i = 0; i < priority.length; i++)
				sb.append("Priority: " + priority[i] + LF);
		}
		if (smtpMessageId != null)
			sb.append("SMTP Message Id: " + smtpMessageId + LF);

		if (carrierCode != null)
			sb.append("CarrierCode: " + carrierCode + LF);
		sb.append("Subject: " + subject + LF);
		if (date != null)
			sb.append("Send Date: " + date + LF);
		if (emBedEmailId != null) {
			sb.append("Embed EmailId: " + emBedEmailId.booleanValue() + LF);
		}
		if (msgId != null) {
			sb.append("MsgId: " + msgId.toString() + LF);
		}
		if (msgRefId != null)
			sb.append("MsgRefId: " + msgRefId + LF);
		if (ruleName != null)
			sb.append("RuleName: " + ruleName + LF);
		if (mailingListId != null)  {
			sb.append("Mailing List Id: " + mailingListId + LF);
			sb.append("ToSubscribersOnly: " + toSubscribersOnly + LF);
		}
		if (renderId != null) {
			sb.append("RenderId: " + renderId.toString() + LF);
		}
		if (msgSourceId != null)
			sb.append("MsgSourceId: " + msgSourceId + LF);
		if (senderId != null)
			sb.append("Sender Id: " + senderId + LF);
		if (subrId != null)
			sb.append("Subr Id: " + subrId + LF);
		sb.append("To PlainText: " + toPlainText + LF);
		sb.append("Override TestAddr: " + overrideTestAddr + LF);
		if (mailboxHost != null)
			sb.append("MailBox Host: " + mailboxHost + LF);
		if (mailboxUser != null)
			sb.append("Mailbox User: " + mailboxUser + LF);
		if (mailboxName != null)
			sb.append("Mailbox Name: " + mailboxName + LF);
		if (folderName != null)
			sb.append("Folder Name: " + folderName + LF);
		if (finalRcpt!=null)
			sb.append("Final Recipient: "+finalRcpt+LF);
		if (origRcpt!=null)
			sb.append("Original Recipient: "+origRcpt+LF);
		if (dsnAction!=null)
			sb.append("DSN Action: "+dsnAction+LF);
		if (dsnStatus!=null)
			sb.append("DSN Status: "+dsnStatus+LF);
 		if (diagnosticCode!=null)
			sb.append("Diagnostic Code: "+diagnosticCode+LF);
		if (origSubject!=null)
			sb.append("Original Subject: "+origSubject+LF);
		if (rfcMessageId!=null)
			sb.append("RFC MessageId: "+rfcMessageId+LF);
		if (dsnText!=null)
			sb.append(LF + "List DSN Text: "+ LF + dsnText+LF);
		if (dsnRfc822!=null)
			sb.append(LF + "List DSN RFC822: "+ LF + dsnRfc822+LF);
		if (dsnDlvrStat!=null)
			sb.append(LF + "List DSN Delivery Status: "+ LF + dsnDlvrStat+LF);
		if (headers.size() > 0)
			sb.append(LF + "List Header Lines:" + LF);
		for (int i = 0; i < headers.size(); i++) {
			MsgHeader hdr = (MsgHeader) headers.get(i);
			sb.append("Header Line - " + hdr.getName() + ": " + hdr.getValue() + LF);
		}
		sb.append(LF + "List Body Parts:" + LF);
		sb.append(super.toString(0));
		if (properties != null && !properties.isEmpty()) {
			sb.append(LF + "List Properties:" + LF);
			for (Enumeration<?> enu = properties.keys(); enu.hasMoreElements();) {
				String key = (String) enu.nextElement();
				sb.append(key + ": " + properties.getProperty(key) + LF);
			}
		}
		if (hashMap != null && !hashMap.isEmpty()) {
			sb.append(LF + "List HashMap:" + LF);
			Set<?> set = hashMap.keySet();
			for (Iterator<?> it = set.iterator(); it.hasNext();) {
				String key = (String) it.next();
				Object value = hashMap.get(key);
				if (value != null && (value instanceof String)) {
					// display the first 70 characters if it is a string
					String dispStr = (String) value;
					StringTokenizer st = new StringTokenizer(dispStr, "\n");
					if (st.hasMoreTokens()) {
						dispStr = st.nextToken(); // display only the first line
					}
					if (dispStr.length() < 70)
						value = dispStr;
					else
						value = dispStr.substring(0, 70) + "...";
				}
				sb.append(key + ": " + value + LF);
			}
		}
		if (attachments != null && attachments.size() > 0) {
			sb.append(LF + "List Attachments:" + LF);
			for (int i = 0; i < attachments.size(); i++) {
				MessageNode node = (MessageNode)attachments.get(i);
				BodypartBean anode = node.getBodypartNode();
				sb.append(anode.toString(node.getLevel()));
			}
		}
		/*
		if (report != null && report instanceof MessageNode) {
			sb.append(LF + "List Delivery Status Report:" + LF);
			BodypartBean anode = report.getBodypartNode();
			sb.append(anode.toString(report.getLevel()));
		}
		if (rfc822 != null && rfc822 instanceof MessageNode) {
			sb.append(LF + "List Rfc822:" + LF);
			BodypartBean anode = rfc822.getBodypartNode();
			sb.append(anode.toString(rfc822.getLevel()));
		}
		*/
		return sb.toString();
	}
}
