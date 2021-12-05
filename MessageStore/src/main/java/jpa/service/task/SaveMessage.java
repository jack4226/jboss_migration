package jpa.service.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.data.preload.RuleActionDetailEnum;
import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.service.msgdata.MessageInboxService;
import jpa.service.msgin.MessageInboxBo;

@Component("saveMessage")
@Transactional(propagation=Propagation.REQUIRED)
public class SaveMessage extends TaskBaseAdapter {
	private static final long serialVersionUID = -5524706653539538026L;
	static final Logger logger = LogManager.getLogger(SaveMessage.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private MessageInboxBo msgInboxBo;
	@Autowired
	private MessageInboxService inboxService;

	/**
	 * Save the message into the MsgInbox and its satellite tables.
	 * 
	 * @return a Integer value representing the msgId inserted into MsgInbox.
	 */
	public Integer process(MessageContext ctx) throws DataValidationException {
		if (isDebugEnabled)
			logger.debug("Entering process() method...");
		if (ctx==null || ctx.getMessageBean()==null) {
			throw new DataValidationException("input MessageBean is null");
		}
		
		MessageBean msgBean = ctx.getMessageBean();
		
		if (msgBean.getMsgId() != null && msgBean.getRuleName() != null) {
			String mapKey = Constants.RULE_ACTION;
			if (msgBean.getHashMap().containsKey(mapKey)
					&& RuleActionDetailEnum.ASSIGN_RULENAME.name().equals(msgBean.getHashMap().get(mapKey))) {
				msgBean.getHashMap().remove(mapKey); // not to affect the rest of the tasks
				int rowsUpdated = inboxService.updateRuleName(msgBean.getMsgId(), msgBean.getRuleName());
				if (rowsUpdated > 0) {
					ctx.getRowIds().add(msgBean.getMsgId());
					return msgBean.getMsgId();
				}
			}
		}
		
		int rowId = msgInboxBo.saveMessage(msgBean);
		ctx.getRowIds().add(rowId);
		
		return rowId;
	}
	
}
