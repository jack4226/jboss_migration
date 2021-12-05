package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageActionLog;
import jpa.model.msg.MessageActionLogPK;
import jpa.repository.msg.MessageActionLogRepository;

@Component("messageActionLogService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageActionLogService implements java.io.Serializable {
	private static final long serialVersionUID = -3216111798576623837L;

	static Logger logger = LogManager.getLogger(MessageActionLogService.class);

	@Autowired
	MessageActionLogRepository repository;
	
	public Optional<MessageActionLog> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public MessageActionLog getByPrimaryKey(MessageActionLogPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.findOneByMessageActionLogPK_MessageInbox_RowIdAndMessageActionLogPK_LeadMessageRowId(
				pk.getMessageInbox().getRowId(), pk.getLeadMessageRowId());
	}

	public List<MessageActionLog> getByMsgInboxId(int msgId) {
		return repository.findAllByMessageActionLogPK_MessageInbox_RowId(msgId);
	}

	public List<MessageActionLog> getByLeadMsgId(int msgId) {
		return repository.findAllByMessageActionLogPK_LeadMessageRowId(msgId);
	}

	public void delete(MessageActionLog actionLog) {
		if (actionLog == null) return;
		repository.delete(actionLog);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByPrimaryKey(MessageActionLogPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getLeadMessageRowId());
	}

	public int deleteByMsgInboxId(int msgId) {
		return repository.deleteByMessageInboxId(msgId);
	}

	public int deleteByLeadMsgId(int msgId) {
		return repository.deleteByLeadMessageId(msgId);
	}

	public void update(MessageActionLog actionLog) {
		repository.saveAndFlush(actionLog);
	}

	public void insert(MessageActionLog actionLog) {
		repository.saveAndFlush(actionLog);
	}

}
