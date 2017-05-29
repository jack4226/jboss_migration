package jpa.service.msgout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.constant.CarrierCode;
import jpa.constant.CodeType;
import jpa.constant.Constants;
import jpa.constant.EmailIdToken;
import jpa.constant.MsgStatusCode;
import jpa.constant.XHeaderName;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageBeanBuilder;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageContext;
import jpa.message.MsgHeader;
import jpa.message.util.MsgIdCipher;
import jpa.model.SenderData;
import jpa.model.msg.MessageInbox;
import jpa.model.msg.MessageStream;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.msgdata.MessageDeliveryStatusService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgdata.MessageStreamService;
import jpa.service.msgin.MessageInboxBo;
import jpa.spring.util.SpringUtil;
import jpa.util.EmailAddrUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Base class for sending messages off through SMTP server(s).
 * 
 * @author Jack Wang
 */
public abstract class MailSenderBase implements java.io.Serializable {
	private static final long serialVersionUID = 327892556529764255L;
	protected static final Logger logger = Logger.getLogger(MailSenderBase.class);
	protected static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	protected boolean debugSession = false;
	protected SenderData senderVo = null;

	@Autowired
	protected MessageInboxBo msgInboxBo;
	@Autowired
	protected MessageInboxService msgInboxDao;
	@Autowired
	protected MsgOutboxBo msgOutboxBo;
	@Autowired
	protected MessageDeliveryStatusService deliveryStatusDao;
	@Autowired
	protected EmailAddressService emailAddrDao;
	@Autowired
	protected MessageStreamService msgStreamDao;
	@Autowired
	protected SenderDataService senderService;

	//private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	protected static final String LF = System.getProperty("line.separator", "\n");

	public MailSenderBase() {
		if (isDebugEnabled)
			logger.debug("Entering constructor...");
	}
	
	/**
	 * Process the request. The message context should contain an email
	 * message represented by either a MessageBean or a ByteStream.
	 * 
	 * @param messageContext -
	 *            contains a MessageBean or a MessageStream object
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SmtpException
	 * @throws DataValidationException 
	 */
	protected void processMessage(MessageContext ctx)
			throws MessagingException, IOException, SmtpException, DataValidationException {

		if (ctx == null) {
			logger.error("a null request was received.");
			return;
		}
		if (ctx.getMessageBean() == null && ctx.getMessageStream() == null) {
			throw new IllegalArgumentException("Request did not contain a MessageBean nor a MessageStream.");
		}
		
		MessageBean msgBean = ctx.getMessageBean();
		if (msgBean == null) {
			logger.info("MessageBean is null, continue processing with a MessageStream...");
			javax.mail.Message mimeMsg = MessageBeanUtil.createMimeMessage(ctx.getMessageStream());
			/*
			 * In order to save the message to database, a MessageBean is required
			 * by saveMessage method. So first we convert the JavaMail message to a
			 * MessageBean and save it. Second we convert the MessageBean back to
			 * JavaMail message and send it off (as an Email_Id may have been added
			 * to the message body and X-header).
			 */
			// convert the JavaMail message to a MessageBean
			msgBean = MessageBeanBuilder.processPart(mimeMsg, null);
			// convert extra mimeMessage headers
			addXHeadersToBean(msgBean, mimeMsg);
		}
		// was the outgoing message rendered?
		if (msgBean.getRenderId() == null) {
			logger.warn("process() - Render Id is null, the message was not rendered");
		}
		
		/* 
		 * Save addresses first to resolve database error - Lock wait timeout exceeded;
		 * Only do this if it's not already in a transaction. 
		 */
		if (SpringUtil.isInTransaction() == false) {
			SpringUtil.beginTransaction();
			try {
				/* insert email addresses */
				saveEmailAddr(msgBean.getFrom());
				saveEmailAddr(msgBean.getTo());
				saveEmailAddr(msgBean.getReplyto());
				/* end of email addresses */
				SpringUtil.commitTransaction();
			}
			catch (Exception e) {
				logger.error("Exception during saving email addrs: " + e.getMessage());
				SpringUtil.rollbackTransaction();
			}
		}
		
		SpringUtil.beginTransaction();
		try {
			// set rule name to SEND_MAIL
			msgBean.setRuleName(RuleNameEnum.SEND_MAIL.getValue());
			if (StringUtils.isBlank(msgBean.getSenderId())) {
				msgBean.setSenderId(Constants.DEFAULT_SENDER_ID);
			}
			senderVo = senderService.getBySenderId(msgBean.getSenderId());
			if (senderVo == null) {
				throw new DataValidationException("SenderId (" + msgBean.getSenderId() + ") not found.");
			}
			msgBean.setIsReceived(false); // out going message
			if (msgBean.getEmBedEmailId() == null) { // not provided by calling program
				// use system default
				msgBean.setEmBedEmailId(Boolean.valueOf(senderVo.isEmbedEmailId()));
			}
			// save the message to the database
			int rowId = msgInboxBo.saveMessage(msgBean);
			ctx.getRowIds().add(rowId);
			MessageInbox minbox = msgInboxDao.getByRowId(rowId);
			// check if VERP is enabled
			if (senderVo.isVerpEnabled()) {
				// set return path with VERP, msgBean.msgId must be valued.
				String emailId = EmailIdToken.XHDR_BEGIN + MsgIdCipher.encode(msgBean.getMsgId())
						+ EmailIdToken.XHDR_END;
				Address[] addrs = msgBean.getTo();
				if (addrs == null || addrs.length == 0 || addrs[0] == null) {
					throw new DataValidationException("TO address is not provided.");
				}
				String recipient = EmailAddrUtil.removeDisplayName(addrs[0].toString());
				if (StringUtils.isBlank(senderVo.getVerpInboxName())) {
					throw new DataValidationException("VERP inbox name is blank in SenderData table.");
				}
				String left = senderVo.getVerpInboxName() + "-" + emailId + "-"
						+ recipient.replaceAll("@", "=");
				String verpDomain = senderVo.getDomainName();
				if (StringUtils.isNotBlank(senderVo.getVerpSubDomain())) {
					verpDomain = senderVo.getVerpSubDomain() + "." + verpDomain;
				}
				msgBean.setReturnPath("<" + left + "@" + verpDomain + ">");
				// set List-Unsubscribe VERP header
				if (StringUtils.isNotBlank(msgBean.getMailingListId())) {
					if (StringUtils.isBlank(senderVo.getVerpRemoveInbox())) {
						throw new DataValidationException("VERP remove inbox is blank in SenderData table.");
					}
					left = senderVo.getVerpRemoveInbox() + "-" + msgBean.getMailingListId() + "-"
							+ recipient.replaceAll("@", "=");
					MsgHeader header = new MsgHeader();
					header.setName("List-Unsubscribe");
					header.setValue("<mailto:" + left + "@" + verpDomain + ">");
					msgBean.getHeaders().add(header);
				}
			}
			// build a MimeMessage from the MessageBean
			javax.mail.Message mimeMsg = MessageBeanUtil.createMimeMessage(msgBean);
			// retrieve SMTP Message-Id
			if (mimeMsg.getHeader("Message-Id")!=null) {
				String[] smtpMsgId = mimeMsg.getHeader("Message-Id");
				if (smtpMsgId.length>0) {
					msgBean.setSmtpMessageId(smtpMsgId[0]);
				}
			}
			// override mimeMessage.TO with test address if this is a test run
			rebuildAddresses(mimeMsg, msgBean.getOverrideTestAddr());
			// send the message off
			Map<String, Address[]> errors = new HashMap<String, Address[]>();
			try {
				sendMail(mimeMsg, msgBean.isUseSecureServer(), errors);
				/* Update message delivery status */
				updateMsgStatus(msgBean, minbox);
			}
			catch (SendFailedException sfex) {
				// failed to send the message to certain recipients
				logger.error("SendFailedException caught: ", sfex);
				updtDlvrStatAndLoopback(msgBean, sfex, errors);
				if (errors.containsKey("validSent")) {
					updateDeliveryReport(msgBean, minbox);
				}
			}
			// save message raw stream to database
			if (msgBean.getSaveMsgStream()) {
				saveMsgStream(mimeMsg, minbox);
			}
			SpringUtil.commitTransaction();
		}
		catch (MessagingException | IOException | SmtpException | DataValidationException e) {
			SpringUtil.rollbackTransaction();
			throw e;
		}
	}
	
	private void saveEmailAddr(Address[] addrs) {
		for (int i = 0; addrs != null && i < addrs.length; i++) {
			Address addr = addrs[i];
			if (addr != null && StringUtils.isNotBlank(addr.toString())) {
				emailAddrDao.findSertAddress(addr.toString().trim());
			}
		}
	}

	private void addXHeadersToBean(MessageBean msgBean, javax.mail.Message mimeMsg)
			throws MessagingException {
		String[] renderId = mimeMsg.getHeader(XHeaderName.RENDER_ID.value());
		if (renderId != null && renderId.length > 0) {
			String renderIdStr = renderId[0];
			try {
				msgBean.setRenderId(Integer.valueOf(renderIdStr));
			}
			catch (NumberFormatException e) {
				logger.error("addXHeadersToBean() - NumberFormatException caught from converting "
						+ XHeaderName.RENDER_ID.value() + ": " + renderIdStr);
			}
		}
		
		String[] msgRefId = mimeMsg.getHeader(XHeaderName.MSG_REF_ID.value());
		if (msgRefId != null && msgRefId.length > 0) {
			String msgRefIdStr = msgRefId[0];
			try {
				msgBean.setMsgRefId(Integer.valueOf(msgRefIdStr));
			}
			catch (NumberFormatException e) {
				logger.error("addXHeadersToBean() - NumberFormatException caught from converting "
						+ XHeaderName.MSG_REF_ID.value() + ": " + msgRefIdStr);
			}
		}
		
		boolean isSecure = false;
		// retrieve secure transport flag from X-Header
		String[] st = mimeMsg.getHeader(XHeaderName.USE_SECURE_SMTP.value());
		if (st != null && st.length > 0) {
			if (CodeType.YES.getValue().equals(st[0])) {
				isSecure = true;
			}
		}
		msgBean.setUseSecureServer(isSecure);
		
		//String[] ruleName = mimeMsg.getHeader(XHeaderName.RULE_NAME.value());
		//if (ruleName != null && ruleName.length > 0) {
		//	msgBean.setRuleName(ruleName[0]);
		//}
		
		String[] overrideTestAddr = mimeMsg.getHeader(XHeaderName.OVERRIDE_TEST_ADDR.value());
		if (overrideTestAddr != null && overrideTestAddr.length > 0) {
			if (CodeType.YES.getValue().equalsIgnoreCase(overrideTestAddr[0]))
				msgBean.setOverrideTestAddr(true);
		}
		
		String[] saveRawStream = mimeMsg.getHeader(XHeaderName.SAVE_RAW_STREAM.value());
		if (saveRawStream != null && saveRawStream.length > 0) {
			if (CodeType.NO.getValue().equalsIgnoreCase(saveRawStream[0]))
				msgBean.setSaveMsgStream(false);
		}
		
		String[] embedEmailId = mimeMsg.getHeader(XHeaderName.EMBED_EMAILID.value());
		if (embedEmailId != null && embedEmailId.length > 0) {
			if (CodeType.NO.getValue().equalsIgnoreCase(embedEmailId[0]))
				msgBean.setEmBedEmailId(Boolean.valueOf(false));
			else if (CodeType.YES.getValue().equalsIgnoreCase(embedEmailId[0]))
				msgBean.setEmBedEmailId(Boolean.valueOf(true));
		}
	}
	
	/**
	 * Convert FROM and TO addresses to testing addresses if needed. When
	 * original addresses are converted to testing addresses, the original
	 * addresses are not completed lost, they are shown as "Display Name".
	 * 
	 * Exceptions: if the TO address is a local address (xxxxxxxxx@localhost),
	 * it is not converted to testing TO address regardless.
	 * 
	 * @param m -
	 *            a javax.mail.Message object
	 * @param isOverrideTestAddr -
	 *            if true, do not convert original addresses.
	 * @throws MessagingException
	 */
	protected void rebuildAddresses(javax.mail.Message m, boolean isOverrideTestAddr)
			throws MessagingException {
		if (isDebugEnabled) {
			logger.debug("Entering rebuildAddresses method...");
		}
		// set TO address to Test Address if it's a test run
		if (senderVo.isUseTestAddr() && !isOverrideTestAddr) {
			boolean toAddrIsLocal = false;
			String displayName = null;
			// use the original address as Display Name 
			Address[] to_addrs = m.getRecipients(javax.mail.Message.RecipientType.TO);
			if (to_addrs != null && to_addrs.length > 0) {
				Address to_addr = to_addrs[0];
				if (to_addr != null) {
					String addr = to_addr.toString();
					if (StringUtils.isNotBlank(addr)) {
						displayName = EmailAddrUtil.removeDisplayName(addr);
						//displayName = StringUtil.replaceAll(displayName, "@", ".at.");
						toAddrIsLocal = addr.toLowerCase().endsWith("@localhost");
					}
				}
			}
			if (!toAddrIsLocal) { // DO NOT override if TO address is local
				if (isDebugEnabled) {
					logger.debug("rebuildAddresses() - Replace original TO: "
							+ EmailAddrUtil.addressToString(m.getRecipients(javax.mail.Message.RecipientType.TO))
							+ ", with testing address: " + senderVo.getTestToAddr());
				}
				if (displayName == null) {
					m.setRecipients(RecipientType.TO, InternetAddress.parse(senderVo.getTestToAddr()));
				}
				else {
					m.setRecipients(RecipientType.TO, InternetAddress.parse("\""
								+ displayName + "\" <"
								+ EmailAddrUtil.removeDisplayName(senderVo.getTestToAddr()) + ">"));
				}
			}
		}
		// validate TO address
		if (m.getRecipients(RecipientType.TO) == null
				|| m.getRecipients(RecipientType.TO).length == 0) {
			throw new AddressException("TO address is blank!");
		}
		// Set From address to Test Address if it's a test run and not provided
		if (senderVo.isUseTestAddr() && !isOverrideTestAddr
				&& (m.getFrom() == null || m.getFrom().length == 0)) {
			if (isDebugEnabled) {
				logger.debug("rebuildAddresses() - Original From is missing, use testing address: "
						+  senderVo.getTestFromAddr());
			}
			if (EmailAddrUtil.hasDisplayName(senderVo.getTestFromAddr())) {
				m.setFrom(InternetAddress.parse(senderVo.getTestFromAddr())[0]);
			}
			else {
				m.setFrom(InternetAddress.parse("\"MailSender\" <" + senderVo.getTestFromAddr()
						+ ">")[0]);
			}
		}
		// validate FROM address
		if (m.getFrom() == null || m.getFrom().length == 0) { // just for safety
			throw new AddressException("FROM address is blank!");
		}
		// set ReplyTo address to Test Address if it's a test run and not provided
		if (senderVo.isUseTestAddr() && !isOverrideTestAddr
				&& (m.getReplyTo() == null || m.getReplyTo().length == 0)) {
			if (StringUtils.isNotBlank(senderVo.getTestReplytoAddr())) {
				if (EmailAddrUtil.hasDisplayName(senderVo.getTestReplytoAddr())) {
					m.setReplyTo(InternetAddress.parse(senderVo.getTestReplytoAddr()));
				}
				else {
					m.setReplyTo(InternetAddress.parse("\"MailSender Reply\" " + "<"
							+ senderVo.getTestReplytoAddr() + ">"));
				}
			}
		}
	}

	/**
	 * Save a JavaMail message in a raw stream format into database.
	 * 
	 * @param msg -
	 *            a JavaMail message object
	 * @param msgId -
	 *            primary key of the database table.
	 * @throws MessagingException
	 * @throws IOException
	 */
	protected void saveMsgStream(javax.mail.Message msg, MessageInbox msgInboxVo) throws MessagingException,
			IOException {
		if (isDebugEnabled)
			logger.debug("saveMsgStream() - msgId: " + msgInboxVo.getRowId());
		MessageStream msgStreamVo = new MessageStream();
		msgStreamVo.setMessageInbox(msgInboxVo);
		Address[] fromAddrs = msg.getFrom();
		if (fromAddrs != null && fromAddrs.length > 0) {
			EmailAddress emailAddrVo = emailAddrDao.findSertAddress(fromAddrs[0].toString());
			msgStreamVo.setFromAddrRowId(Integer.valueOf(emailAddrVo.getRowId()));
		}
		Address[] toAddrs = msg.getRecipients(RecipientType.TO);
		if (toAddrs != null && toAddrs.length > 0) {
			EmailAddress emailAddrVo = emailAddrDao.findSertAddress(toAddrs[0].toString());
			msgStreamVo.setToAddrRowId(Integer.valueOf(emailAddrVo.getRowId()));
		}
		msgStreamVo.setMsgSubject(msg.getSubject());
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			msg.writeTo(baos);
		}
		catch (java.io.UnsupportedEncodingException e) {
			// The added jutf7 in Maven dependency should have fixed the problem
			logger.error("UnsupportedEncodingException caught: ", e);
			byte[] errorMsg = new String("\n\n" + e.getMessage()).getBytes();
			baos.write(errorMsg, baos.size(), errorMsg.length);
		}
		msgStreamVo.setMsgStream(baos.toByteArray());
		//msgInboxVo.setMessageStream(msgStreamVo);
		//msgInboxDao.update(msgInboxVo); // may trigger OptimisticLockingException
		msgStreamDao.insert(msgStreamVo);
	}
	
	/**
	 * update delivery status with delivery error, send an error message back to
	 * system via email
	 * 
	 * @param msgBean -
	 *            message bean
	 * @param exp -
	 *            exception
	 * @param errors -
	 *            error map
	 * @throws SmtpException
	 * @throws IOException
	 * @throws MessagingException
	 */
	public void updtDlvrStatAndLoopback(MessageBean msgBean, SendFailedException exp,
			Map<String, ?> errors) throws MessagingException, IOException, SmtpException {
		if (errors.get("validUnsent") != null) {
			Address[] validUnsent = (Address[]) errors.get("validUnsent");
			validUnsent(msgBean, exp, validUnsent);
		}
		if (errors.get("invalid") != null) {
			Address[] invalid = (Address[]) errors.get("invalid");
			invalid(msgBean, exp, invalid);
		}
	}

	protected void validUnsent(MessageBean msgBean, SendFailedException exp, Address[] validUnsent)
			throws MessagingException, IOException, SmtpException {
		
		MessageInbox minbox = msgInboxDao.getByRowId(msgBean.getMsgId());
		if (minbox == null) {
			logger.error("validUnsent() - MsgInbox record not found for MsgId: "
					+ msgBean.getMsgId());
			return;
		}
		String reason = "4.1.0 Mail unsent to the address due to following error: "
				+ exp.toString();
		for (int i = 0; i < validUnsent.length; i++) {
			logger.info("validUnsent() - Addr [" + i + "]: " + validUnsent[i]
					+ ", insert into DeliveryStatus msgId=" + msgBean.getMsgId());
			Address failedAddr = validUnsent[i];
			if (failedAddr == null || StringUtils.isBlank(failedAddr.toString())) {
				continue;
			}
			try {
				// loop back unsent messages as soft bounce
				loopbackMail(msgBean, exp.getMessage(), failedAddr, reason);
			}
			catch (DataValidationException e) {
				logger.error("DataValidationException caught, ignore.", e);
			}
		}
	}
	
	protected void invalid(MessageBean msgBean, SendFailedException exp, Address[] invalid)
			throws MessagingException, IOException, SmtpException {
		
		MessageInbox minbox =msgInboxDao.getByRowId(msgBean.getMsgId());
		if (minbox == null) {
			logger.error("invalid() - MsgInbox record not found for MsgId: " + msgBean.getMsgId());
			return;
		}
		String reason = "5.1.1 Invalid Destination Mailbox Address: " + exp.toString();
		for (int i = 0; i < invalid.length; i++) {
			logger.info("invalid() -  Addr [" + i + "]: " + invalid[i]
					+ ", insert into DeliveryStatus msgId=" + msgBean.getMsgId());
			Address failedAddr = invalid[i];
			if (failedAddr == null || StringUtils.isBlank(failedAddr.toString())) {
				continue;
			}
			try {
				// loop back invalid messages as hard bounce
				loopbackMail(msgBean, exp.getMessage(), failedAddr, reason);
			}
			catch (DataValidationException e) {
				logger.error("DataValidationException caught, ignore.", e);
			}
		}
	}
	
	/**
	 * send delivery report to caller
	 * 
	 * @param m -
	 *            MessageBean
	 * @return number of email's that were sent
	 * @throws MessagingException
	 */
	public int updateDeliveryReport(MessageBean m, MessageInbox minbox) throws MessagingException {
		int rspCount = 0;
		if (CarrierCode.SMTPMAIL.equals(m.getCarrierCode())) {
			if (m.isInternalOnly()) {
				rspCount = updateMsgStatus(m, minbox);
			}
			else {
				rspCount = updateMsgStatus(m, minbox);
			}
		}
		else {
			throw new MessagingException("Invalid Carrier Code: " + m.getCarrierCode());
		}
		return rspCount;
	}

	/**
	 * update delivery status to MsgInbox.
	 * 
	 * @param msgId -
	 *            MessageId
	 * @return number records updated
	 * @throws MessagingException 
	 */
	protected int updateMsgStatus(MessageBean msgBean, MessageInbox msgInboxVo) throws MessagingException {
		// update MsgInbox status (to delivered)
		Timestamp ts = new Timestamp(System.currentTimeMillis());
		msgInboxVo.setStatusId(MsgStatusCode.DELIVERED.getValue());
		msgInboxVo.setDeliveryTime(ts);
		msgInboxVo.setUpdtTime(ts);
		msgInboxVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		if (StringUtils.isNotBlank(msgBean.getSmtpMessageId())) {
			msgInboxVo.setSmtpMessageId(msgBean.getSmtpMessageId());
		}
		msgInboxDao.update(msgInboxVo);
		return 1;
	}

	/**
	 * loop error report back to rule engine for further processing.
	 * @param msgBean -
	 *            message bean
	 * @param errmsg -
	 *            error text
	 * @param reason -
	 *            DSN reason code
	 * @throws IOException
	 * @throws MessagingException
	 * @throws DataValidationException 
	 * @throws SmtpException 
	 */
	protected void loopbackMail(MessageBean msgBean, String errmsg, Address failedAddr,
			String reason) throws MessagingException, IOException, SmtpException {
		
		logger.info("Entering LoopbackMail method, error message: " + errmsg);
		// generate delivery failure report
		// attach the original header lines to the message body
		String reportLine1 = "The delivery of following message failed due to: " + LF;
		String reportLine2 = reason + LF;
		String reportLine3 = errmsg + ": " + failedAddr.toString() + LF;
		
		String loopbackText = reportLine1 + reportLine2 + reportLine3 + LF + LF;
		
		// assign loop-back address, for reference only, not used to deliver the message.
		msgBean.setTo(InternetAddress.parse("loopback@localhost"));
		logger.info("loopbackMail() - Undelivered message has been routed to "
				+ "loopback@localhost");
		Message msg = MessageBeanUtil.createMimeMessage(msgBean, failedAddr, loopbackText);
//		MessageBean loopBackBean = MessageBeanBuilder.processPart(msg, null);
//		loopBackBean.setMsgRefId(msgBean.getMsgId());
//		loopBackBean.setIsReceived(true);
//		if (isDebugEnabled) {
//			logger.debug("loopbackMail() - The loopback MessageBean:" + LF + "<----" + LF
//					+ loopBackBean + LF + "---->");
//		}
//		// use MessageParserBo to invoke rule engine
//		parser.parse(loopBackBean);
		// send the loop back mail off
		sendMail(msg, new HashMap<String, Address[]>());
	}

	/**
	 * send a message via a SMTP server. to be implemented by sub-class.
	 * 
	 * @param msg -
	 *            a JavaMail Message object
	 * @param isSecure -
	 *            true to use secure SMTP server
	 * @param errors -
	 *            any errors from the SMTP server
	 * @throws MessagingException
	 * @throws IOException
	 * @throws SmtpException
	 */
	public abstract void sendMail(Message msg, boolean isSecure, Map<String, Address[]> errors)
		throws MessagingException, IOException, SmtpException;

	/**
	 * send the email off via unsecured SMTP server. to be implemented by
	 * sub-class.
	 * 
	 * @param msg -
	 *            a JavaMail message object
	 * @throws SmtpException
	 * @throws MessagingException
	 */
	public abstract void sendMail(Message msg, Map<String, Address[]> errors)
			throws MessagingException, SmtpException;

}