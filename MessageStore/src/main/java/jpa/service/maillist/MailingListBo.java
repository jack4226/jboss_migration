package jpa.service.maillist;

import java.io.IOException;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jpa.constant.CarrierCode;
import jpa.constant.MailingListDeliveryType;
import jpa.data.preload.RuleNameEnum;
import jpa.exception.DataValidationException;
import jpa.exception.OutOfServiceException;
import jpa.exception.TemplateException;
import jpa.exception.TemplateNotFoundException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailTemplate;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.service.common.EmailTemplateService;
import jpa.service.common.SubscriptionService;
import jpa.service.task.BroadcastToList;
import jpa.service.task.SendMessage;
import jpa.util.EmailAddrUtil;

@Component("mailingListBo")
@Scope(value="prototype")
@Lazy(value=true)
public class MailingListBo {
	static final Logger logger = Logger.getLogger(MailingListBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailTemplateBo emailTemplateBo;
	@Autowired
	private EmailTemplateService emailTemplateDao;
	@Autowired
	private MailingListService mailingListDao;
	@Autowired
	private SubscriptionService subscriptionDao;
	@Autowired
	private BroadcastToList broadcastBo;
	@Autowired
	private SendMessage sendMailBo;
	
	/**
	 * broadcast to the mailing list retrieved from the template by the provided
	 * template id. The default list id from the template is used to obtain the
	 * TO addresses of the subscribers. The message subject and message body
	 * from the template are used to construct a MessageBean. The messageBean is
	 * then passed to BroadcaseBo for processing.
	 * 
	 * @param templateId -
	 *            unique id of a template where the message information come
	 *            from
	 * @return number of mails sent
	 * @throws OutOfServiceException
	 * @throws TemplateNotFoundException
	 * @throws DataValidationException
	 */
	public int broadcast(String templateId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		EmailTemplate vo = emailTemplateDao.getByTemplateId(templateId);
		if (vo == null) {
			throw new TemplateNotFoundException("Could not find Template by Id: " + templateId);
		}
		return broadcast(vo);
	}
	
	/**
	 * broadcast to the mailing list retrieved from a template, use the provided
	 * list id.
	 * 
	 * @param templateId-
	 *            unique id of a template where the message information come
	 *            from
	 * @param listId -
	 *            mailing list id to be used by the template
	 * @return number of mails sent
	 * @throws OutOfServiceException
	 * @throws TemplateNotFoundException
	 * @throws DataValidationException
	 */
	public int broadcast(String templateId, String listId) throws OutOfServiceException,
			TemplateNotFoundException, DataValidationException {
		MailingList mailingList = mailingListDao.getByListId(listId);
		if (mailingList == null) {
			throw new DataValidationException("Could not find MailingList by Id: " + listId);
		}
		EmailTemplate vo = emailTemplateDao.getByTemplateId(templateId);
		if (vo == null) {
			throw new TemplateNotFoundException("Could not find Template by Id: " + templateId);
		}
		vo.setMailingList(mailingList);
		return broadcast(vo);
	}

	/**
	 * Send the email off using provided information.
	 * 
	 * @param toAddr -
	 *            the target email address
	 * @param variables -
	 *            name/value pair of variables used to render the template
	 * @param templateId -
	 *            template id used to retrieve the message template
	 * @return number of mails sent
	 * @throws OutOfServiceException
	 * @throws TemplateNotFoundException
	 * @throws DataValidationException
	 * @throws TemplateException 
	 */
	public int send(String toAddr, Map<String, String> variables, String templateId)
			throws DataValidationException, TemplateNotFoundException, OutOfServiceException {
		// render email template
		TemplateRenderVo renderVo = null;
		try {
			renderVo = emailTemplateBo.renderEmailTemplate(toAddr, variables, templateId);
		}
		catch (TemplateException e) {
			throw new OutOfServiceException("TemplateException caught",e);
		}		// create MessageBean
		MessageBean msgBean = createMessageBean(renderVo.getEmailTemplate());
		//msgBean.getBodyNode().setValue(renderVo.getBody());
		msgBean.setBody(renderVo.getBody());
		msgBean.setSubject(renderVo.getSubject());
		msgBean.setRuleName(RuleNameEnum.SEND_MAIL.getValue());
		try {
			msgBean.setTo(InternetAddress.parse(toAddr));
		}
		catch (AddressException e) {
			throw new DataValidationException("Input toAddr is invalid: " + toAddr, e);
		}
		try {
			msgBean.setFrom(InternetAddress.parse(renderVo.getFromAddr()));
		}
		catch (AddressException e) {
			throw new DataValidationException("Invalid FROM address found from list: "
					+ renderVo.getFromAddr());
		}
		if (StringUtils.isNoneBlank(renderVo.getCcAddr())) {
			try {
				msgBean.setCc(InternetAddress.parse(renderVo.getCcAddr()));
			}
			catch (AddressException e) {
				logger.error("send() - ccAddr is invalid: " + renderVo.getCcAddr());
			}
		}
		if (StringUtils.isNoneBlank(renderVo.getBccAddr())) {
			try {
				msgBean.setBcc(InternetAddress.parse(renderVo.getBccAddr()));
			}
			catch (AddressException e) {
				logger.error("send() -bccAddr is invalid: " + renderVo.getBccAddr());
			}
		}
		if (isDebugEnabled) {
			logger.debug("send() - MessageBean created:" + LF + msgBean);
		}
		
		Integer mailsSent = 0;
		try {
			mailsSent = sendMailBo.process(new MessageContext(msgBean));
		}
		catch (MessagingException e) {
			throw new OutOfServiceException("MessagingException caught",e);
		}
		catch (IOException e) {
			throw new OutOfServiceException("IOException caught",e);
		}
		return mailsSent;
	}
	
	private int broadcast(EmailTemplate vo) throws DataValidationException, OutOfServiceException {
		MessageBean msgBean = createMessageBean(vo);
		if (isDebugEnabled) {
			logger.debug("broadcast() - MessageBean created:" + LF + msgBean);
		}
		Integer msgsSent = null;
		try {
			MessageContext ctx = new MessageContext(msgBean);
			ctx.setTaskArguments(msgBean.getRuleName());
			msgsSent = broadcastBo.process(ctx);
		}
		catch (MessagingException e) {
			throw new OutOfServiceException("MessagingException caught",e);
		}
		catch (TemplateException e) {
			throw new OutOfServiceException("TemplateException caught",e);
		}
		catch (IOException e) {
			throw new OutOfServiceException("IOException caught",e);
		}
		return msgsSent;
	}

	final static String LF = System.getProperty("line.separator", "\n");
	
	private MessageBean createMessageBean(EmailTemplate tmpltVo)
			throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering createMessageBean() method...");

		MessageBean msgBean = new MessageBean();
		msgBean.setCarrierCode(CarrierCode.SMTPMAIL);
		msgBean.setSenderId(tmpltVo.getSenderData().getSenderId());
		msgBean.setSubject(tmpltVo.getSubject());
		msgBean.setSendDate(new java.util.Date());
		
		msgBean.setIsReceived(false);
		msgBean.setOverrideTestAddr(false);
		msgBean.setRuleName(RuleNameEnum.BROADCAST.getValue());
		msgBean.setEmBedEmailId(tmpltVo.getIsEmbedEmailId());
		msgBean.setToSubscribersOnly(MailingListDeliveryType.SUBSCRIBERS_ONLY.getValue().equals(tmpltVo.getDeliveryOption()));
		msgBean.setToProspectsOnly(MailingListDeliveryType.PROSPECTS_ONLY.getValue().equals(tmpltVo.getDeliveryOption()));
		
		// set message body and attachments
		String msgBody = tmpltVo.getBodyText();
		if (tmpltVo.isHtml()) {
			msgBean.setContentType("text/html");
		}
		else {
			msgBean.setContentType("text/plain");
		}
		msgBean.setBody(msgBody);
		
		// check mailing list id
		String listId = tmpltVo.getMailingList().getListId();
		msgBean.setMailingListId(listId);
		MailingList listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Could not find Mailing List by Id: " + listId);
		}
		return msgBean;
	}

	public Subscription subscribe(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("subscribe() -  emailAddr: " + emailAddr + ", listAddr: " + listId);
		return addOrRemove(emailAddr, listId, true);
	}

	public Subscription unSubscribe(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("unSubscribe() - emailAddr: " + emailAddr + ", listAddr: " + listId);
		return addOrRemove(emailAddr, listId, false);
	}

	public Subscription optInRequest(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("optInRequest() -  emailAddr: " + emailAddr + ", listAddr: " + listId);
		return optInOrConfirm(emailAddr, listId, false);
	}

	public Subscription optInConfirm(String emailAddr, String listId) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("optInConfirm() -  emailAddr: " + emailAddr + ", listAddr: " + listId);
		return optInOrConfirm(emailAddr, listId, true);
	}

	private Subscription addOrRemove(String emailAddr, String listId, boolean addToList)
			throws DataValidationException {
		// validate email address and list id
		validateInput(emailAddr, listId);
		// retrieve/insert email address from/into EmailAddr table
		Subscription sub;
		if (addToList) {
			sub = subscriptionDao.subscribe(emailAddr, listId);
			logger.info(emailAddr + " added to list: " + listId);
		}
		else {
			sub = subscriptionDao.unsubscribe(emailAddr, listId);
			logger.info(emailAddr + " removed from list: " + listId);
		}
		return sub;
	}

	private Subscription optInOrConfirm(String emailAddr, String listId, boolean confirm)
			throws DataValidationException {
		// validate email address and list id
		validateInput(emailAddr, listId);
		// opt-in or confirm subscription
		Subscription sub;
		if (confirm) {
			sub = subscriptionDao.optInConfirm(emailAddr, listId);
			logger.info(emailAddr + " confirmed to list: " + listId);
		}
		else {
			sub = subscriptionDao.optInRequest(emailAddr, listId);
			logger.info(emailAddr + " opt-in'ed to list: " + listId);
		}
		return sub;
	}

	private void validateInput(String emailAddr, String listId)
			throws DataValidationException {
		// validate email address
		if (StringUtils.isBlank(emailAddr)) {
			throw new DataValidationException("Email Address is not valued.");
		}
		if (!EmailAddrUtil.isRemoteEmailAddress(emailAddr)) {
			throw new DataValidationException("Email Address is invalid: " + emailAddr);
		}
		// validate mailing list id
		if (StringUtils.isBlank(listId)) {
			throw new DataValidationException("Mailing List Id is not valued.");
		}
		MailingList listVo = mailingListDao.getByListId(listId);
		if (listVo == null) {
			throw new DataValidationException("Mailing List does not exist: " + listId);
		}
	}

}
