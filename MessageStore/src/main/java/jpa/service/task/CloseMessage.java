package jpa.service.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.MsgStatusCode;
import jpa.data.preload.FolderEnum;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.model.msg.MessageFolder;
import jpa.model.msg.MessageInbox;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;

@Component("closeMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class CloseMessage extends TaskBaseAdapter {
	private static final long serialVersionUID = 8626771378364657544L;
	static final Logger logger = LogManager.getLogger(CloseMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageInboxService inboxService;
	@Autowired
	private MessageFolderService folderService;

	/**
	 * Close the message by MsgId.
	 * @return a Integer representing the msgId closed.
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
			logger.warn("MessageBean.msgId is null, nothing to close");
			return Integer.valueOf(msgId);
		}
		
		MessageInbox msgInboxVo = inboxService.getByRowId(messageBean.getMsgId());
		if (msgInboxVo != null) {
			msgId = msgInboxVo.getRowId();
			msgInboxVo.setStatusId(MsgStatusCode.CLOSED.getValue());
			MessageFolder folder = folderService.getOneByFolderName(FolderEnum.Closed.name());
			if (folder != null) { // should always be true
				msgInboxVo.setMessageFolder(folder);
			}
			inboxService.update(msgInboxVo);
			if (isDebugEnabled)
				logger.debug("Message with row_id of (" + msgInboxVo.getRowId() + ") is Closed.");
		}
		else {
			logger.error("Message with RowId (" + msgId + ") not found in Message_Inbox!");
		}
		return Integer.valueOf(msgId);
	}
	
}
