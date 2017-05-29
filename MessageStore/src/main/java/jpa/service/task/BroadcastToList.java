package jpa.service.task;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jpa.constant.Constants;
import jpa.constant.MailingListDeliveryType;
import jpa.constant.MobileCarrierEnum;
import jpa.constant.StatusId;
import jpa.constant.VariableName;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.message.util.EmailIdParser;
import jpa.model.BroadcastMessage;
import jpa.model.BroadcastTracking;
import jpa.model.MailingList;
import jpa.model.SubscriberData;
import jpa.model.Subscription;
import jpa.service.common.SubscriberDataService;
import jpa.service.common.SubscriptionService;
import jpa.service.maillist.BroadcastMessageService;
import jpa.service.maillist.BroadcastTrackingService;
import jpa.service.maillist.EmailTemplateBo;
import jpa.service.maillist.MailingListService;
import jpa.service.maillist.TemplateRenderVo;
import jpa.service.msgout.MailSenderBo;
import jpa.service.msgout.SmtpException;
import jpa.util.EmailSender;
import jpa.util.HtmlConverter;
import jpa.util.PhoneNumberUtil;
import jpa.variable.RenderUtil;

@Component("broadcastToList")
public class BroadcastToList extends TaskBaseAdapter {
	private static final long serialVersionUID = -7248771079550892321L;
	static final Logger logger = Logger.getLogger(BroadcastToList.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailingListService mailingListDao;
	@Autowired
	private SubscriptionService subscriptionDao;
	@Autowired
	private SubscriberDataService subscriberDao;
	@Autowired
	private EmailTemplateBo emailTemplateBo;
	@Autowired
	private MailSenderBo mailSenderBo;
	@Autowired
	private BroadcastMessageService bcstMsgDao;
	@Autowired
	private BroadcastTrackingService bcstTrackingDao;

	/**
	 * Send the email to the addresses on the Mailing List.
	 * 
	 * @param msgBean -
	 *            message to broadcast
	 * @return an Integer value representing number of addresses the message has
	 *         been sent to.
	 * @throws TemplateException 
	 * @throws IOException 
	 */
	public Integer process(MessageContext ctx) throws DataValidationException,
			AddressException, TemplateException, IOException {
		if (isDebugEnabled) {
			logger.debug("Entering process() method...");
		}
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		if (!RuleNameEnum.BROADCAST.getValue().equals(messageBean.getRuleName())) {
			throw new DataValidationException("Invalid Rule Name: " + messageBean.getRuleName());
		}
		if (StringUtils.isNotBlank(ctx.getTaskArguments())) {
			// mailing list from MessageBean takes precedence
			if (StringUtils.isBlank(messageBean.getMailingListId())) {
				messageBean.setMailingListId(ctx.getTaskArguments());
			}
		}
		if (StringUtils.isBlank(messageBean.getMailingListId())) {
			throw new DataValidationException("Mailing List was not provided.");
		}
		
		messageBean.setIsReceived(false);
		int mailsSentTotal = 0;
		Boolean saveEmbedEmailId = messageBean.getEmBedEmailId();
		String listId = messageBean.getMailingListId();
		MailingList listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List " + listId + " not found.");
		}
		if (!listVo.isActive()) {
			logger.warn("MailingList " + listId + " is not active.");
			return Integer.valueOf(0);
		}
		String _from = listVo.getListEmailAddr();
		String dispName = listVo.getDisplayName();
		if (StringUtils.isNotBlank(dispName)) {
			_from = dispName + "<" + _from + ">";
		}
		logger.info("Broadcasting to Mailing List: " + listId + ", From: " + _from);
		Address[] from = InternetAddress.parse(_from);
		// set FROM to list address
		messageBean.setFrom(from);
		// get message body from body node
		String bodyText = null;
		if (messageBean.getBodyNode() != null) {
			bodyText = new String(messageBean.getBodyNode().getValue());
		}
		if (bodyText == null) {
			throw new DataValidationException("Message body is empty.");
		}
		// construct and save BroadcastMessage
		BroadcastMessage broadcast = new BroadcastMessage();
		broadcast.setMailingList(listVo);
		broadcast.setEmailTemplate(null);
		if (messageBean.getToSubscribersOnly()) {
			broadcast.setDeliveryType(MailingListDeliveryType.SUBSCRIBERS_ONLY.getValue());
		}
		else if (messageBean.getToProspectsOnly()) {
			broadcast.setDeliveryType(MailingListDeliveryType.PROSPECTS_ONLY.getValue());
		}
		else {
			broadcast.setDeliveryType(MailingListDeliveryType.ALL_ON_LIST.getValue());
		}
		broadcast.setMsgBody(bodyText);
		broadcast.setMsgSubject(messageBean.getSubject());
		broadcast.setStatusId(StatusId.ACTIVE.getValue());
		java.sql.Timestamp currTime = new java.sql.Timestamp(System.currentTimeMillis());
		broadcast.setStartTime(currTime);
		broadcast.setUpdtTime(currTime);
		broadcast.setUpdtUserId(Constants.DEFAULT_USER_ID);
		bcstMsgDao.insert(broadcast);
		// end of BroadcastMessage
		
		/* 
		 * TODO Search HTML links in message body and wrap them with the tracking link
		 */
		
		// extract variables from message body
		List<String> varNames = RenderUtil.retrieveVariableNames(bodyText);
		if (isDebugEnabled) {
			logger.debug("Body Variable names: " + varNames);
		}
		// extract variables from message subject
		String subjText = messageBean.getSubject() == null ? "" : messageBean.getSubject();
		List<String> subjVarNames = RenderUtil.retrieveVariableNames(subjText);
		if (!subjVarNames.isEmpty()) {
			varNames.addAll(subjVarNames);
			if (isDebugEnabled) {
				logger.debug("Subject Variable names: " + subjVarNames);
			}
		}
		// get subscribers
		List<Subscription> subrs = null;
		if (messageBean.getToSubscribersOnly()) {
			subrs = subscriptionDao.getByListIdSubscribersOnly(listId);
		}
		else if (messageBean.getToProspectsOnly()) {
			subrs = subscriptionDao.getByListIdProsperctsOnly(listId);
		}
		else {
			subrs = subscriptionDao.getByListId(listId);
		}
		// sending email to each subscriber
		for (Subscription subr : subrs) {
			// construct BroadcastTracking and save the record
			BroadcastTracking tracking = new BroadcastTracking();
			tracking.setBroadcastMessage(broadcast);
			tracking.setEmailAddress(subr.getEmailAddress());
			tracking.setStatusId(StatusId.ACTIVE.getValue());
			tracking.setUpdtUserId(Constants.DEFAULT_USER_ID);
			tracking.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
			bcstTrackingDao.insert(tracking);
			
			//messageBean.setSubject(subjText);
			//messageBean.getBodyNode().setValue(bodyText);
			int mailsSent = constructAndSendMessage(ctx, tracking, subr, listVo, subjText, bodyText, varNames, saveEmbedEmailId, false);
			if (listVo.isSendText()) {
				mailsSent += constructAndSendMessage(ctx, tracking, subr, listVo, subjText, bodyText, varNames, saveEmbedEmailId, true);
			}
			mailsSentTotal += mailsSent;
			if (mailsSent > 0) {
				bcstTrackingDao.updateSentCount(tracking.getRowId(), mailsSent);
			}
		}
		if (mailsSentTotal > 0) {
			// update sent count to the Broadcasted message
			bcstMsgDao.updateSentCount(broadcast.getRowId(), mailsSentTotal);
		}
		return Integer.valueOf(mailsSentTotal);
	}
	
	private int constructAndSendMessage(MessageContext ctx, BroadcastTracking bcstTrk, Subscription subr,
			MailingList listVo, String subjText, String bodyText, List<String> varNames,
			Boolean saveEmbedEmailId, boolean isText)
			throws DataValidationException, TemplateException, IOException {
		MessageBean msgBean = ctx.getMessageBean();
		String listId = msgBean.getMailingListId();
		Address[] to = null;
		String toAddress = null;
		try {
			if (isText) {
				SubscriberData subrVo = subscriberDao.getByEmailAddress(subr.getEmailAddress().getAddress());
				if (subrVo != null) {
					if (StringUtils.isNotBlank(subrVo.getMobilePhone())
							&& StringUtils.isNotBlank(subrVo.getMobileCarrier())) {
						try {
							MobileCarrierEnum mc = MobileCarrierEnum.getByValue(subrVo.getMobileCarrier());
							String phone = PhoneNumberUtil.convertTo10DigitNumber(subrVo.getMobilePhone());
							if (StringUtils.isNotBlank(mc.getCountry())) {
								phone = mc.getCountry() + phone;
							}
							toAddress = phone+"@"+mc.getText();
							to = InternetAddress.parse(toAddress);
						}
						catch (NumberFormatException e) {
							logger.error("Invalid mobile phone number (" + subrVo.getMobilePhone() + ") found in Subscriber_Data!");
						}
						catch (IllegalArgumentException e) {
							String msg = "Mobile carrier (" + subrVo.getMobileCarrier() + ") not found in enum MobileCarrierEnum!";
							logger.error(msg);
							// notify programming
							String subj = "(" + subrVo.getMobileCarrier() + ") need to be added to the system - {0}";
							EmailSender.sendEmail(subj, msg, null, EmailSender.EmailList.ToDevelopers);
						}
					}
				}
				if (to == null) {
					return 0;
				}
			}
			else {
				toAddress = subr.getEmailAddress().getAddress();
				to = InternetAddress.parse(toAddress);
			}
		}
		catch (AddressException e) {
			logger.error("Invalid TO address, ignored: " + toAddress, e);
			return 0;
		}
		/*
		String mailingAddr = StringUtil.removeDisplayName(listVo.getEmailAddr(), true);
		if (sub.getEmailAddr().toLowerCase().indexOf(mailingAddr) >= 0) {
			logger.warn("Loop occurred, ignore mailing list address: " + sub.getEmailAddr());
			continue;
		}
		*/
		Map<String, String> variables = new HashMap<String, String>();
		String varName = VariableName.LIST_VARIABLE_NAME.BroadcastMsgId.name(); // TODO change to tracking id
		variables.put(varName, String.valueOf(bcstTrk.getBroadcastMessage().getRowId())); // TODO
		logger.info("Sending Broadcast Email to: " + toAddress);
		TemplateRenderVo renderVo = null;
		renderVo = emailTemplateBo.renderEmailText(toAddress, variables, subjText,
				bodyText, listId, varNames);
		// set TO to subscriber address
		msgBean.setTo(to);
		String body = renderVo.getBody();
		if ("text/html".equals(msgBean.getBodyContentType())
				&& !subr.getEmailAddress().isAcceptHtml() || isText) {
			// convert to plain text
			try {
				body = HtmlConverter.getInstance().convertToText(body);
				msgBean.getBodyNode().setContentType("text/plain");
			}
			catch (ParserException e) {
				logger.error("Failed to convert from html to plain text for: " + body);
				logger.error("ParserException caught", e);
			}
		}
		msgBean.getBodyNode().setValue(body);
		msgBean.setSubject(renderVo.getSubject());
		/*
		 * Remove existing Email_Id from header to ensure that a fresh Email_Id
		 * is always embedded in the header.
		 */
		msgBean.removeHeader(EmailIdParser.getDefaultParser().getEmailIdXHdrName());
		if (isText) { // do not embed email id in text message
			msgBean.setEmBedEmailId(Boolean.FALSE);
		}
		else {
			msgBean.setEmBedEmailId(saveEmbedEmailId);
			subscriptionDao.updateSentCount(subr.getRowId());
		}
		// invoke mail sender to send the mail off
		try {
			mailSenderBo.process(ctx);
			if (isDebugEnabled) {
				logger.debug("Message sent to: " + msgBean.getToAsString());
			}
		}
		catch (SmtpException e) {
			throw new IOException(e.getMessage(), e);
		}
		int mailsSent = msgBean.getTo().length;
		return mailsSent;
	}

}
