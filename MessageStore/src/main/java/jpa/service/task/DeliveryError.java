package jpa.service.task;

import java.sql.Timestamp;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.constant.MsgStatusCode;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.model.msg.MessageDeliveryStatus;
import jpa.model.msg.MessageDeliveryStatusPK;
import jpa.model.msg.MessageInbox;
import jpa.service.common.EmailAddressService;
import jpa.service.msgdata.MessageDeliveryStatusService;
import jpa.service.msgdata.MessageInboxService;
import jpa.util.PrintUtil;

@Component("deliveryError")
@Transactional(propagation=Propagation.REQUIRED)
public class DeliveryError extends TaskBaseAdapter {
	private static final long serialVersionUID = -4372604755210330099L;
	static final Logger logger = Logger.getLogger(DeliveryError.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	MessageDeliveryStatusService deliveryStatusDao;
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private MessageInboxService msgInboxDao;

	/**
	 * Obtain delivery status information from MessageBean's DSN or RFC reports.
	 * Update DeliveryStatus table and MsgOutbox table by MsgRefId (the original
	 * message).
	 * 
	 * @return a Long value representing the MsgId inserted into DeliveryStatus
	 *         table, or -1 if nothing is saved.
	 */
	public Integer process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean() == null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		if (messageBean.getMsgRefId() == null) {
			logger.warn("Inbox MsgRefId not found, nothing to update");
			return Integer.valueOf(-1);
		}
		if (StringUtils.isBlank(messageBean.getFinalRcpt())) {
			logger.warn("Final Recipient not found, nothing to update");
			return Integer.valueOf(-1);
		}
		
		int msgId = messageBean.getMsgRefId();
		
		// Find the message where the delivery status to be added
		MessageInbox msgInboxVo = msgInboxDao.getByRowId(msgId);
		if (msgInboxVo == null) {
			logger.warn("MsgInbox record not found for MsgId: " + msgId);
			return Integer.valueOf(-1);
		}
		if (msgInboxVo.getToAddress() == null) {
			logger.error("MsgInbox record has a null TO address for MsgId: " + msgId);
			return Integer.valueOf(-1);
		}
		
		// check the Final Recipient and the original TO address is the same
		EmailAddress emailAddrVo = emailAddrDao.findSertAddress(messageBean.getFinalRcpt());
		if (msgInboxVo.getToAddress().getRowId() != emailAddrVo.getRowId()) {
			String origTo = msgInboxVo.getToAddress().getAddress();
			logger.warn("Final Recipient <" + messageBean.getFinalRcpt()
					+ "> is different from original email's TO address <"
					+ origTo + ">");
		}
		
		// insert into deliveryStatus
		MessageDeliveryStatus deliveryStatusVo = new MessageDeliveryStatus();
		MessageDeliveryStatusPK pk = new MessageDeliveryStatusPK(msgInboxVo, emailAddrVo.getRowId());
		deliveryStatusVo.setMessageDeliveryStatusPK(pk);
		
		if (StringUtils.isNotBlank(messageBean.getRfcMessageId())) {
			deliveryStatusVo.setSmtpMessageId(StringUtils.left(messageBean.getRfcMessageId(),255));
		}
		else {
			deliveryStatusVo.setSmtpMessageId(StringUtils.left(messageBean.getSmtpMessageId(),255));
		}
		if (StringUtils.isNotBlank(messageBean.getDsnDlvrStat())) {
			deliveryStatusVo.setDeliveryStatus(messageBean.getDsnDlvrStat());
		}
		else if (StringUtils.isNotBlank(messageBean.getDsnText())) {
			deliveryStatusVo.setDsnText(messageBean.getDsnText());
		}
		deliveryStatusVo.setDsnReason(StringUtils.left(messageBean.getDiagnosticCode(),255));
		deliveryStatusVo.setDsnStatus(StringUtils.left(messageBean.getDsnStatus(),50));
		
		deliveryStatusVo.setFinalRecipientAddress(StringUtils.left(messageBean.getFinalRcpt(),255));
		
		if (StringUtils.isNotBlank(messageBean.getOrigRcpt())) {
			EmailAddress vo = emailAddrDao.findSertAddress(messageBean.getOrigRcpt());
			deliveryStatusVo.setOriginalRcptAddrRowId(vo.getRowId());
		}
		
		try {
			boolean dlvrStatusExist = false;
			for (MessageDeliveryStatus status : msgInboxVo.getMessageDeliveryStatusList()) {
				if (status.getMessageDeliveryStatusPK().equals(deliveryStatusVo.getMessageDeliveryStatusPK())) {
					if (StringUtils.isNotBlank(deliveryStatusVo.getSmtpMessageId())) {
						status.setSmtpMessageId(deliveryStatusVo.getSmtpMessageId());
					}
					status.setReceivedCount(status.getReceivedCount() + 1);
					dlvrStatusExist = true;
					break;
				}
			}
			if (dlvrStatusExist == false) {
				//deliveryStatusDao.insert(deliveryStatusVo);
				msgInboxVo.getMessageDeliveryStatusList().add(deliveryStatusVo);
			}
			
			if (isDebugEnabled) {
				logger.debug("Insert DeliveryStatus:" + LF + PrintUtil.prettyPrint(deliveryStatusVo,3));
			}
		}
		catch (DataIntegrityViolationException e) {
			logger.error("DataIntegrityViolationException caught, ignore.", e);
		}
		// update MsgInbox status (delivery failure)
		msgInboxVo.setStatusId(MsgStatusCode.DELIVERY_FAILED.getValue());
		msgInboxVo.setUpdtTime(new Timestamp(System.currentTimeMillis()));
		msgInboxVo.setUpdtUserId(Constants.DEFAULT_USER_ID);
		
		msgInboxDao.update(msgInboxVo);
		ctx.getRowIds().add(msgInboxVo.getRowId());
		return Integer.valueOf(msgId);
	}
	
}
