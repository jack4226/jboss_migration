package jpa.service.task;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.MsgStatusCode;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.msg.MessageInbox;
import jpa.service.msgdata.MessageInboxService;

@Component("openMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class OpenMessage extends TaskBaseAdapter {
	private static final long serialVersionUID = 5058402748410505313L;
	static final Logger logger = Logger.getLogger(OpenMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageInboxService msgInboxDao;

	/**
	 * Open the message by MsgId.
	 * @return a Integer representing the msgId opened.
	 */
	public Integer process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		MessageBean messageBean = ctx.getMessageBean();
		int msgId = -1;
		if (messageBean.getMsgId()==null) {
			logger.warn("MessageBean.msgId is null, nothing to open");
			return Integer.valueOf(msgId);
		}
		
		MessageInbox msgInboxVo = msgInboxDao.getByRowId(messageBean.getMsgId());
		if (msgInboxVo != null) {
			msgId = msgInboxVo.getRowId();
			if (!MsgStatusCode.OPENED.getValue().equals(msgInboxVo.getStatusId())) {
				msgInboxVo.setStatusId(MsgStatusCode.OPENED.getValue());
				msgInboxDao.update(msgInboxVo);
			}
			if (isDebugEnabled)
				logger.debug("Message with row_id of (" + msgInboxVo.getRowId() + ") is Opened.");
		}
		else {
			logger.error("Message record not found by row_id (" + messageBean.getMsgId() + ").");
		}
		return Integer.valueOf(msgId);
	}
}
