package jpa.service.task;

import java.io.IOException;
import java.util.Map;

import javax.mail.Address;
import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jpa.exception.DataValidationException;
import jpa.exception.TemplateException;
import jpa.exception.TemplateNotFoundException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.EmailAddress;
import jpa.service.common.EmailAddressService;
import jpa.service.maillist.EmailTemplateBo;
import jpa.service.maillist.TemplateRenderVo;
import jpa.service.msgout.MailSenderBo;
import jpa.service.msgout.SmtpException;
import jpa.util.HtmlConverter;

@Component("autoReplyMessage")
public class AutoReplyMessage extends TaskBaseAdapter {
	private static final long serialVersionUID = 6364742594226515121L;
	static final Logger logger = LogManager.getLogger(AutoReplyMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private EmailAddressService emailAddrDao;
	@Autowired
	private EmailTemplateBo tmpltBo;
	@Autowired
	private MailSenderBo mailSenderBo;
	
	/**
	 * Construct the reply text from the EmailTemplateId passed in the
	 * TaskArguments, render the text and send the reply message.
	 * 
	 * @param messageBean
	 *            - the original email that is replying to.
	 * @return a Integer value representing number of addresses the message is
	 *         replied to.
	 * @throws AddressException
	 * @throws TemplateException
	 * @throws IOException
	 */
	public Integer process(MessageContext ctx) throws DataValidationException,
			AddressException, TemplateException, IOException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		if (StringUtils.isBlank(ctx.getTaskArguments())) {
			throw new DataValidationException("Arguments(TemplateId) is not valued.");
		}
		else if (isDebugEnabled) {
			logger.debug("Arguments passed: " + ctx.getTaskArguments());
		}
		MessageBean messageBean = ctx.getMessageBean();
		// check FROM address
		Address[] from = messageBean.getFrom();
		if (from == null || from.length == 0) {
			throw new DataValidationException("FROM is not valued, no one to reply to.");
		}
		// create a reply message bean
		MessageBean replyBean = new MessageBean();
		replyBean.setFrom(messageBean.getTo());
		if (messageBean.getMsgId() != null) {
			replyBean.setMsgRefId(messageBean.getMsgId());
		}
		else if (messageBean.getMsgRefId() != null) {
			replyBean.setMsgRefId(messageBean.getMsgRefId());
		}
		replyBean.setMailboxUser(messageBean.getMailboxUser());
		//replyBean.setOriginalMail(messageBean);
		int msgsSent = 0;
		for (int i = 0; i < from.length; i++) {
			Address _from = from[i];
			// check FROM address
			if (_from == null || StringUtils.isBlank(_from.toString())) {
				continue;
			}
			// select the address from database (or insert if it does not exist)
			EmailAddress vo = emailAddrDao.findSertAddress(_from.toString());
			Map<String, String> variables = null;
			TemplateRenderVo renderVo = null;
			try {
				// Mailing List id may have been provided by upstream process (subscribe)
				renderVo = tmpltBo.renderEmailTemplate(vo.getAddress(), variables, ctx.getTaskArguments(),
						messageBean.getMailingListId());
			}
			catch (TemplateNotFoundException e) {
				throw new DataValidationException("Email Template not found by Id: "
						+ ctx.getTaskArguments());
			}
			replyBean.setSubject(renderVo.getSubject());
			String body = renderVo.getBody();
			if (renderVo.getEmailTemplate() != null && renderVo.getEmailTemplate().isHtml()) {
				if (vo.isAcceptHtml()) {
					replyBean.setContentType("text/html");
				}
				else {
					try {
						body = HtmlConverter.getInstance().convertToText(body);
					}
					catch (ParserException e) {
						logger.error("Failed to convert from html to plain text for: " + body);
						logger.error("ParserException caught", e);
					}
				}
			}
			replyBean.setBody(body);
			replyBean.setSenderId(messageBean.getSenderId());
			if (StringUtils.isNotBlank(messageBean.getSenderId())) {
				replyBean.setSenderId(messageBean.getSenderId());
			}
			else if (StringUtils.isNotBlank(renderVo.getSenderId())) {
				replyBean.setSenderId(renderVo.getSenderId());
			}
			replyBean.setSubrId(messageBean.getSubrId());
			// set recipient address
			Address[] _to = {_from};
			replyBean.setTo(_to);
			try {
				ctx.setMessageBean(replyBean);
				mailSenderBo.process(ctx);
				if (isDebugEnabled) {
					logger.debug("Message replied to: " + replyBean.getToAsString());
				}
			}
			catch (SmtpException e) {
				throw new IOException(e.getMessage(), e);
			}
			msgsSent++;
			if (isDebugEnabled) {
				logger.debug("Reply message processed: " + LF + replyBean);
			}
		}
		return Integer.valueOf(msgsSent);
	}
}
