package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageHeader;
import jpa.model.msg.MessageHeaderPK;
import jpa.repository.msg.MessageHeaderRepository;

@Component("messageHeaderService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageHeaderService implements java.io.Serializable {
	private static final long serialVersionUID = -8249120539861689753L;

	static Logger logger = LogManager.getLogger(MessageHeaderService.class);
	
	@Autowired
	MessageHeaderRepository repository;
	
	public Optional<MessageHeader> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public MessageHeader getByPrimaryKey(MessageHeaderPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.findOneByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getHeaderSequence());
	}

	public List<MessageHeader> getByMsgInboxId(int msgId) {
		return repository.findAllByMessageHeaderPK_MessageInbox_RowId(msgId);
	}

	public void delete(MessageHeader msgHeader) {
		if (msgHeader == null) return;
		repository.delete(msgHeader);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByPrimaryKey(MessageHeaderPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getHeaderSequence());
	}

	public int deleteByMsgInboxId(int msgId) {
		return repository.deleteByMessageInboxId(msgId);
	}

	public void update(MessageHeader msgHeader) {
		repository.saveAndFlush(msgHeader);
	}

	public void insert(MessageHeader msgHeader) {
		repository.saveAndFlush(msgHeader);
	}

}
