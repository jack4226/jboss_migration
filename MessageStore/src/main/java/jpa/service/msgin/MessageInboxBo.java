package jpa.service.msgin;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

import javax.mail.Address;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.CarrierCode;
import jpa.constant.Constants;
import jpa.constant.EmailAddrType;
import jpa.constant.MsgDirectionCode;
import jpa.constant.MsgStatusCode;
import jpa.data.preload.FolderEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.message.BodypartBean;
import jpa.message.BodypartUtil;
import jpa.message.MessageBean;
import jpa.message.MessageBeanBuilder;
import jpa.message.MessageBeanUtil;
import jpa.message.MessageBodyBuilder;
import jpa.message.MessageNode;
import jpa.message.MsgHeader;
import jpa.model.EmailAddress;
import jpa.model.SenderData;
import jpa.model.SubscriberData;
import jpa.model.msg.MessageActionLog;
import jpa.model.msg.MessageActionLogPK;
import jpa.model.msg.MessageAddress;
import jpa.model.msg.MessageAttachment;
import jpa.model.msg.MessageAttachmentPK;
import jpa.model.msg.MessageDeliveryStatus;
import jpa.model.msg.MessageDeliveryStatusPK;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageHeader;
import jpa.model.msg.MessageHeaderPK;
import jpa.model.msg.MessageInbox;
import jpa.model.msg.MessageRendered;
import jpa.model.msg.MessageRfcField;
import jpa.model.msg.MessageRfcFieldPK;
import jpa.model.msg.MessageStream;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailAddressService;
import jpa.service.common.SenderDataService;
import jpa.service.common.SubscriberDataService;
import jpa.service.msgdata.MessageActionLogService;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgdata.MessageRenderedService;
import jpa.service.rule.RuleLogicService;
import jpa.util.PrintUtil;

/**
 * save email data and properties into database.
 */
@Component("messageInboxBo")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageInboxBo implements java.io.Serializable {
	private static final long serialVersionUID = -4615089647296218955L;
	static final Logger logger = Logger.getLogger(MessageInboxBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageInboxService msgInboxDao;
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private MessageRenderedService msgRenderedDao;
	@Autowired
	private MessageActionLogService msgActionLogsDao;
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private SubscriberDataService subrService;
	@Autowired
	private RuleLogicService logicService;
	@Autowired
	private MessageFolderService folderService;

	static final String LF = System.getProperty("line.separator", "\n");

	/**
	 * save an email to database.
	 * 
	 * @param msgBean
	 *            MessageBean instance containing email properties
	 * @return primary key (=msgId) of the record inserted
	 * @throws DataValidationException 
	 * @throws SQLException
	 *             if SQL error occurred
	 */
	public int saveMessage(MessageBean msgBean) throws DataValidationException {
		if (isDebugEnabled) {
			logger.debug("Entering saveMessage() method..." + LF + msgBean);
		}
		if (msgBean == null) {
			throw new DataValidationException("Input MessageBean is null");
		}
		if (msgBean.getRuleName() == null) {
			throw new DataValidationException("MessageBean.getRuleName() returns a null");
		}
		Timestamp updtTime = new Timestamp(System.currentTimeMillis());

		MessageInbox msgVo = new MessageInbox();
		msgVo.setReferringMessageRowId(msgBean.getMsgRefId());
		if (msgBean.getCarrierCode() == null) {
			logger.warn("saveMessage() - carrierCode field is null, set to S (SMTP)");
			msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		}
		msgVo.setCarrierCode(msgBean.getCarrierCode().getValue());
		msgVo.setMsgSubject(StringUtils.left(msgBean.getSubject(),255));
		msgVo.setMsgPriority(MessageBeanUtil.getMsgPriority(msgBean.getPriority()));
		Timestamp ts = msgBean.getSendDate() == null ? updtTime
				: new Timestamp(msgBean.getSendDate().getTime());
		msgVo.setReceivedTime(ts);
		
		Integer fromAddrId = getEmailAddrId(msgBean.getFrom());
		if (fromAddrId != null) {
			msgVo.setFromAddress(emailAddrDao.getByRowId(fromAddrId));
		}
		Integer replyToAddrId = getEmailAddrId(msgBean.getReplyto());
		if (replyToAddrId != null) {
			msgVo.setReplytoAddress(emailAddrDao.getByRowId(replyToAddrId));
		}
		else {
			msgVo.setReplytoAddress(null);
		}
		Integer toAddrId = getEmailAddrId(msgBean.getTo());
		if (toAddrId != null) {
			msgVo.setToAddress(emailAddrDao.getByRowId(toAddrId));
		}
		
		Calendar cal = Calendar.getInstance();
		if (msgBean.getPurgeAfter() != null) {
			cal.add(Calendar.MONTH, msgBean.getPurgeAfter());
		}
		else {
			cal.add(Calendar.MONTH, 12); // purge in 12 months - default
		}
		// set purge Date
		msgVo.setPurgeDate(new java.sql.Date(cal.getTimeInMillis()));

		// find LeadMsgId. Also find sender id if it's from MailReader
		if (msgBean.getMsgRefId() != null) {
			MessageInbox origVo = msgInboxDao.getByRowId(msgBean.getMsgRefId());
			if (origVo == null) {
				// could be deleted by User or purged by Purge routine
				logger.warn("saveMessage() - MsgInbox record not found by MsgRefId: "
						+ msgBean.getMsgRefId());
			}
			else {
				msgVo.setLeadMessageRowId(origVo.getLeadMessageRowId());
				if (msgBean.getIsReceived()) { // from MailReader
					// code has been moved to MessageParserBo.parse()
				}
			}
		}
		// end LeadMsgId
		
		msgVo.setMsgContentType(StringUtils.left(msgBean.getContentType(),100));
		String contentType = msgBean.getBodyContentType();
		msgVo.setBodyContentType(StringUtils.left(contentType,50));
		if (msgBean.getIsReceived()) {
			/* from MailReader */
			MessageFolder folder = folderService.getOneByFolderName(FolderEnum.Inbox.name());
			msgVo.setMessageFolder(folder);
			
			msgVo.setMsgDirection(MsgDirectionCode.RECEIVED.getValue());
			msgVo.setStatusId(MsgStatusCode.OPENED.getValue());
			msgVo.setSmtpMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			String msgBody = msgBean.getBody();
			msgVo.setMsgBody(msgBody);
			msgVo.setMsgBodySize(msgBody == null ? 0 : msgBody.length());
			// update "Last Received" time
			if (msgVo.getFromAddress() != null) {
				// TODO revisit - was causing "Lock wait timeout exceeded" error
				EmailAddress lastRcptVo = msgVo.getFromAddress();
				long minutes = 10 * 60 * 1000; // 10 minutes
				// to reduce the dead-lock occurrences, update database only if it hasn't been updated in last 10 minutes 
				if (lastRcptVo.getLastRcptTime() == null
						|| lastRcptVo.getLastRcptTime().getTime() < (updtTime.getTime() - minutes)) {
					//emailAddrDao.updateLastRcptTime(msgVo.getFromAddress().getRowId());
				}
			}
		}
		else {
			/* from MailSender */
			MessageFolder folder = folderService.getOneByFolderName(FolderEnum.Sent.name());
			msgVo.setMessageFolder(folder);
			
			msgVo.setMsgDirection(MsgDirectionCode.SENT.getValue());
			msgVo.setSmtpMessageId(null);
			msgVo.setDeliveryTime(null); // delivery time
			msgVo.setStatusId(MsgStatusCode.PENDING.getValue());
			if (msgBean.getRenderId() != null) {
				MessageRendered mr = msgRenderedDao.getByPrimaryKey(msgBean.getRenderId());
				msgVo.setMessageRendered(mr);
			}
			msgVo.setOverrideTestAddr(msgBean.getOverrideTestAddr());
			// check original message body's content type
			if (msgBean.getOriginalMail() != null) {
				String origContentType = msgBean.getOriginalMail().getBodyContentType();
				if (contentType.indexOf("html") < 0 && origContentType.indexOf("html") >= 0) {
					// reset content type to original's
					msgVo.setBodyContentType(StringUtils.left(origContentType,50));
				}
			}
			// update "Last Sent" time
			if (msgVo.getToAddress() != null) {
				// TODO revisit - was causing "Lock wait timeout exceeded" error
				EmailAddress lastSentVo = msgVo.getToAddress();
				long minutes = 10 * 60 * 1000; // 10 minutes
				if (lastSentVo.getLastSentTime() == null
						|| lastSentVo.getLastSentTime().getTime() < (updtTime.getTime() - minutes)) {
					//emailAddrDao.updateLastSentTime(msgVo.getToAddress().getRowId());
				}

			}
		}
		
		if (StringUtils.isNotBlank(msgBean.getSenderId())) {
			SenderData sender = senderService.getBySenderId(msgBean.getSenderId());
			if (sender != null) {
				msgVo.setSenderData(sender);
			}
		}
		if (StringUtils.isNotBlank(msgBean.getSubrId())) {
			SubscriberData subscriber = subrService.getBySubscriberId(msgBean.getSubrId());
			if (subscriber != null) {
				msgVo.setSubscriberData(subscriber);
			}
		}
		if (StringUtils.isNotBlank(msgBean.getRuleName())) {
			RuleLogic logic = logicService.getByRuleName(msgBean.getRuleName());
			msgVo.setRuleLogic(logic);
		}
		
		msgVo.setUpdtTime(updtTime);
		msgVo.setUpdtUserId(StringUtils.left(Constants.DEFAULT_USER_ID,10));
		
		// retrieve attachment count and size, and gathers delivery reports
		BodypartUtil.retrieveAttachments(msgBean);
		
		List<MessageNode> aNodes = msgBean.getAttachments();
		msgVo.setAttachmentCount(aNodes == null ? 0 : aNodes.size());
		int attachmentSize = 0;
		if (aNodes != null) {
			for (MessageNode mNode : aNodes) {
				BodypartBean aNode = mNode.getBodypartNode();
				int _size = aNode.getValue() == null ? 0 : aNode.getValue().length;
				attachmentSize += _size;
			}
		}
		msgVo.setAttachmentSize(attachmentSize);
		if (isDebugEnabled) {
			logger.debug("Message to insert" + LF + PrintUtil.prettyPrint(msgVo));
		}
		msgInboxDao.insert(msgVo);
		logger.info("saveMessage() - MsgId saved as: " + msgVo.getRowId() + ", From MailReader: "
				+ msgBean.getIsReceived());
		
		msgBean.setMsgId(msgVo.getRowId());
		if (!msgBean.getIsReceived()) { /* from MailSender */
			/* Rebuild the Message Body, generate Email_Id from MsgId */
			String msgBody = MessageBodyBuilder.getBodyWithEmailId(msgBean);
			/* end of rebuild */
			msgVo.setMsgBody(msgBody);
			msgVo.setMsgBodySize(msgBody == null ? 0 : msgBody.length());
			BodypartBean bodyNode = msgBean.getBodyNode();
			// update MessageBean.body with Email_Id
			if (bodyNode == null) {
				logger.fatal("saveMessage() - Programming error: bodyNode is null");
				msgBean.setContentType(msgVo.getMsgContentType());
				msgBean.setBody(msgVo.getMsgBody());
			}
			else {
				bodyNode.setContentType(msgVo.getBodyContentType());
				bodyNode.setValue(msgVo.getMsgBody().getBytes());
			}
			msgInboxDao.update(msgVo);
			if (msgBean.getEmBedEmailId()!=null && msgBean.getEmBedEmailId()) {
				logger.info("saveMessage() - Message Body with Email_Id is saved.");
			}
			else {
				logger.info("saveMessage() - Message Body is saved.");
			}
		}

		// save message headers
		List<MsgHeader> headers = msgBean.getHeaders();
		if (headers != null) {
			for (int i = 0; i < headers.size(); i++) {
				MsgHeader header = headers.get(i);
				MessageHeader msgHeadersVo= new MessageHeader();
				MessageHeaderPK pk = new MessageHeaderPK(msgVo,(i+1));
				msgHeadersVo.setMessageHeaderPK(pk);
				msgHeadersVo.setHeaderName(StringUtils.left(header.getName(),100));
				msgHeadersVo.setHeaderValue(header.getValue());
				msgVo.getMessageHeaderList().add(msgHeadersVo);
			}
		}

		// save attachments
		if (aNodes!=null && aNodes.size()>0) {
			for (int i=0; i<aNodes.size(); i++) {
				MessageNode mNode = aNodes.get(i);
				BodypartBean aNode = mNode.getBodypartNode();
				MessageAttachment attchVo = new MessageAttachment();
				MessageAttachmentPK pk = new MessageAttachmentPK(msgVo, mNode.getLevel(), (i+1));
				attchVo.setMessageAttachmentPK(pk);
				attchVo.setAttachmentName(StringUtils.left(aNode.getDescription(),100));
				attchVo.setAttachmentType(StringUtils.left(aNode.getContentType(),100));
				attchVo.setAttachmentDisp(StringUtils.left(aNode.getDisposition(),100));
				attchVo.setAttachmentValue(aNode.getValue());
				msgVo.getMessageAttachmentList().add(attchVo);
			}
		}
		
		// save Delivery Status
		if (msgBean.getReport() != null) {
			MessageNode mNode = msgBean.getReport();
			BodypartBean aNode = mNode.getBodypartNode();
			BodypartBean dlvrStatBean = BodypartUtil.retrieveDlvrStatus(aNode, 0);
			if (dlvrStatBean == null) {
				dlvrStatBean = BodypartUtil.retrieveMDNReceipt(aNode, 0);
			}
			if (dlvrStatBean == null) {
				List<BodypartBean> bpBeans = BodypartUtil.retrieveReportText(aNode, 0);
				if (bpBeans!=null && bpBeans.size()>0) {
					dlvrStatBean = bpBeans.get(0); // TODO revisit
				}
			}
			if (dlvrStatBean != null && StringUtils.isNotBlank(msgBean.getFinalRcpt())) {
				EmailAddress emailAddrVo = emailAddrDao.findSertAddress(msgBean.getFinalRcpt());
				MessageDeliveryStatus deliveryStatusVo = new MessageDeliveryStatus();
				MessageDeliveryStatusPK pk = new MessageDeliveryStatusPK(msgVo, emailAddrVo.getRowId());
				deliveryStatusVo.setMessageDeliveryStatusPK(pk);
				deliveryStatusVo.setSmtpMessageId(StringUtils.left(msgBean.getRfcMessageId(),255));
				if (StringUtils.isNotBlank(msgBean.getDsnDlvrStat())) {
					deliveryStatusVo.setDeliveryStatus(msgBean.getDsnDlvrStat());
				}
				else if (StringUtils.isNotBlank(msgBean.getDsnText())) {
					deliveryStatusVo.setDsnText(msgBean.getDsnText());
				}
				deliveryStatusVo.setDsnReason(StringUtils.left(msgBean.getDiagnosticCode(),255));
				deliveryStatusVo.setDsnStatus(StringUtils.left(msgBean.getDsnStatus(),50));
				deliveryStatusVo.setFinalRecipientAddress(StringUtils.left(msgBean.getFinalRcpt(),255));
				if (StringUtils.isNotBlank(msgBean.getOrigRcpt())) {
					EmailAddress vo = emailAddrDao.findSertAddress(msgBean.getOrigRcpt());
					deliveryStatusVo.setOriginalRcptAddrRowId(vo.getRowId());
				}
				msgVo.getMessageDeliveryStatusList().add(deliveryStatusVo);
			}
		}

		// save RFC fields
		if (msgBean.getRfc822() != null) {
			MessageNode mNode = msgBean.getRfc822();
			BodypartBean aNode = mNode.getBodypartNode();
			MessageRfcField rfcFieldsVo = new MessageRfcField();
			MessageRfcFieldPK pk = new MessageRfcFieldPK(msgVo,StringUtils.left(aNode.getContentType(),50));
			rfcFieldsVo.setMessageRfcFieldPK(pk);
			if (StringUtils.isNotBlank(msgBean.getFinalRcpt())) {
				EmailAddress frcpt = emailAddrDao.findSertAddress(msgBean.getFinalRcpt());
				rfcFieldsVo.setFinalRcptAddrRowId(frcpt.getRowId());
			}
			//rfcFieldsVo.setOrigRcpt(StringUtil.cut(msgBean.getOrigRcpt(),255));
			rfcFieldsVo.setOriginalMsgSubject(StringUtils.left(msgBean.getOrigSubject(),255));
			rfcFieldsVo.setMessageId(StringUtils.left(msgBean.getSmtpMessageId(),255));
			rfcFieldsVo.setDsnText(msgBean.getDsnText());
			rfcFieldsVo.setDsnRfc822(msgBean.getDsnRfc822());
			msgVo.getMessageRfcFieldList().add(rfcFieldsVo);
		}
		
		// we could have found a final recipient without delivery reports
		if (msgBean.getReport() == null && msgBean.getRfc822() == null) {
			if (StringUtils.isNotBlank(msgBean.getFinalRcpt())
					|| StringUtils.isNotBlank(msgBean.getOrigRcpt())) {
				MessageRfcField rfcFieldsVo = new MessageRfcField();
				MessageRfcFieldPK pk = new MessageRfcFieldPK(msgVo,StringUtils.left(msgBean.getContentType(),50));
				rfcFieldsVo.setMessageRfcFieldPK(pk);
					// we don't have content type, so just stick one here.
				if (StringUtils.isNotBlank(msgBean.getFinalRcpt())) {
					EmailAddress frcpt = emailAddrDao.findSertAddress(msgBean.getFinalRcpt());
					rfcFieldsVo.setFinalRcptAddrRowId(frcpt.getRowId());
				}
				rfcFieldsVo.setOriginalRecipient(StringUtils.left(msgBean.getOrigRcpt(),255));
				rfcFieldsVo.setOriginalMsgSubject(StringUtils.left(msgBean.getOrigSubject(),255));
				msgVo.getMessageRfcFieldList().add(rfcFieldsVo);
			}
		}
		
		// save addresses
		saveAddress(msgBean.getFrom(), EmailAddrType.FROM_ADDR, msgVo);
		saveAddress(msgBean.getTo(), EmailAddrType.TO_ADDR, msgVo);
		saveAddress(msgBean.getReplyto(), EmailAddrType.REPLYTO_ADDR, msgVo);
		saveAddress(msgBean.getCc(), EmailAddrType.CC_ADDR, msgVo);
		saveAddress(msgBean.getBcc(), EmailAddrType.BCC_ADDR, msgVo);
		
		// save message raw stream if received by MailReader
		if (msgBean.getHashMap().containsKey(MessageBeanBuilder.MSG_RAW_STREAM)
				&& msgBean.getIsReceived()) {
			// save raw stream for in-bound mails only
			// out-bound raw stream is saved in MailSender
			MessageStream msgStreamVo = new MessageStream();
			msgStreamVo.setMessageInbox(msgVo);
			msgStreamVo.setFromAddrRowId(msgVo.getFromAddress().getRowId());
			msgStreamVo.setToAddrRowId(msgVo.getToAddress().getRowId());
			msgStreamVo.setMsgSubject(msgVo.getMsgSubject());
			msgStreamVo.setMsgStream((byte[]) msgBean.getHashMap().get(
					MessageBeanBuilder.MSG_RAW_STREAM));
			msgVo.setMessageStream(msgStreamVo);
		}
		if (isDebugEnabled) {
			logger.debug("Message to update" + LF + PrintUtil.prettyPrint(msgVo));
		}
		msgInboxDao.update(msgVo);

		if (isDebugEnabled) {
			logger.debug("saveMessage() - Message has been saved to database, RowId: "
					+ msgVo.getRowId());
		}
		return msgVo.getRowId();
	}
	
	/**
	 * not implemented yet
	 * @param msgBean
	 */
	public void saveMessageFlowLogs(MessageBean msgBean) {
		MessageInbox msgVo = msgInboxDao.getByRowId(msgBean.getMsgId());
		if (msgVo == null) {
			logger.error("MessageInbox record not found by RowId: " + msgBean.getMsgId());
			return;
		}
		MessageActionLog msgActionLogsVo = new MessageActionLog();
		MessageActionLogPK pk = new MessageActionLogPK();
		msgActionLogsVo.setMessageActionLogPK(pk);
		pk.setMessageInbox(msgVo);
		// find lead message id
		if (msgBean.getMsgRefId() != null) {
			List<MessageActionLog> list = msgActionLogsDao.getByLeadMsgId(msgBean.getMsgRefId());
			if (list.isEmpty()) {
				logger.error("saveMessageFlowLogs() - record not found by MsgRefId: "
						+ msgBean.getMsgRefId());
			}
			else {
				MessageActionLog vo = list.get(0);
				pk.setLeadMessageRowId(vo.getRowId());
			}
		}
		if (pk.getLeadMessageRowId() < 0) {
			pk.setLeadMessageRowId(msgBean.getMsgId());
		}
		msgActionLogsVo.setActionService(RuleNameEnum.SEND_MAIL.getValue());
		msgActionLogsDao.insert(msgActionLogsVo);
	}
	
	/**
	 * returns data from MsgInbox, MsgHeaders, Attachments, and RFCFields
	 * 
	 * @param msgId -
	 *            message Id
	 * @return a MessageInbox or null if not found
	 */
	public MessageInbox getMessageByPK(int msgId) {
		MessageInbox msgInboxVo = msgInboxDao.getByRowId(msgId);
		return msgInboxVo;
	}
	
	/**
	 * Returns data from getMessageByPK plus data from DeliveryStatus,
	 * MsgRendered and MsgStream.
	 * 
	 * @param msgId -
	 *            message Id
	 * @return a MessageInbox or null if not found
	 */
	public MessageInbox getAllDataByMsgId(int msgId) {
		MessageInbox msgInboxVo = msgInboxDao.getAllDataByPrimaryKey(msgId);
		return msgInboxVo;
	}
	
	private void saveAddress(Address[] addrs, EmailAddrType addrType, MessageInbox msgVo) {
		if (addrs == null || addrs.length == 0) {
			return;
		}
		for (int i = 0; i < addrs.length; i++) {
			Address addr = addrs[i];
			if (addr != null) {
				MessageAddress addrVo = new MessageAddress();
				addrVo.setMessageInbox(msgVo);
				addrVo.setAddressType(addrType.getValue());
				EmailAddress email = emailAddrDao.findSertAddress(StringUtils.left(addr.toString(),255));
				addrVo.setEmailAddrRowId(email.getRowId());
				msgVo.getMessageAddressList().add(addrVo);
			}
		}
	}
	
	// get the first email address from the list and return its EmailAddrId
	private Integer getEmailAddrId(Address[] addrs) {
		Integer id = null;
		if (addrs != null && addrs.length > 0) {
			for (int i = 0; i < addrs.length; i++) {
				id = getEmailAddrId(addrs[i].toString());
				if (id != null)
					break;
			}
		}
		return id;
	}
	
	private Integer getEmailAddrId(String addr) {
		Integer id = null;
		if (StringUtils.isNotBlank(addr)) {
			EmailAddress emailAddrVo = emailAddrDao.findSertAddress(addr.trim());
			id = Integer.valueOf(emailAddrVo.getRowId());
 		}
		return id;
	}

}

	