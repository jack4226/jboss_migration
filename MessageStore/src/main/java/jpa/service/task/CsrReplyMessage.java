package jpa.service.task;

import java.io.IOException;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.service.msgout.MailSenderBo;
import jpa.service.msgout.SmtpException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("csrReplyMessage")
public class CsrReplyMessage extends TaskBaseAdapter {
	private static final long serialVersionUID = 50896288572118819L;
	static final Logger logger = LogManager.getLogger(CsrReplyMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MailSenderBo mailSenderBo;

	/**
	 * The input MessageBean should contain the CSR reply message plus the
	 * original message. If the input bean's getTo() method returns a null,
	 * get it from the original message's From address.
	 * 
	 * @param messageBean -
	 *            the original email must be saved via setOriginalMail() before
	 *            calling this method.
	 * @return a Integer value representing number of addresses the message has
	 *         been replied to.
	 * @throws IOException 
	 */
	public Integer process(MessageContext ctx) throws DataValidationException,
			AddressException, IOException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		if (messageBean.getOriginalMail()==null) {
			throw new DataValidationException("Original MessageBean is null");
		}
		if (messageBean.getOriginalMail().getMsgId()==null) {
			throw new DataValidationException("Original MessageBean's MsgId is null");
		}
		
		if (messageBean.getTo() == null) {
			// validate the TO address, just for safety
			InternetAddress.parse(messageBean.getOriginalMail().getFromAsString());
			messageBean.setTo(messageBean.getOriginalMail().getFrom());
		}
		if (messageBean.getFrom() == null) {
			messageBean.setFrom(messageBean.getOriginalMail().getTo());
		}
		if (StringUtils.isBlank(messageBean.getSenderId())) {
			messageBean.setSenderId(messageBean.getOriginalMail().getSenderId());
		}
		messageBean.setSubrId(messageBean.getOriginalMail().getSubrId());
		if (isDebugEnabled) {
			logger.debug("Address(es) to reply to: " + messageBean.getToAsString());
		}
		
		// write to MailSender input queue
		messageBean.setMsgRefId(messageBean.getOriginalMail().getMsgId());
		// send the reply off
		try {
			mailSenderBo.process(ctx);
			if (isDebugEnabled) {
				logger.debug("Message replied to: " + messageBean.getToAsString());
			}
		}
		catch (SmtpException e) {
			throw new IOException(e.getMessage(), e);
		}
		return Integer.valueOf(messageBean.getTo().length);
	}
}
