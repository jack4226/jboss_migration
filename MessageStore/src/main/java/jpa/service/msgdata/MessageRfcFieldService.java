package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageRfcField;
import jpa.model.msg.MessageRfcFieldPK;
import jpa.repository.msg.MessageRfcFieldRepository;

@Component("messageRfcFieldService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageRfcFieldService implements java.io.Serializable {
	private static final long serialVersionUID = 4201242257880602396L;

	static Logger logger = Logger.getLogger(MessageRfcFieldService.class);

	@Autowired
	MessageRfcFieldRepository repository;
	
	public Optional<MessageRfcField> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public MessageRfcField getByPrimaryKey(MessageRfcFieldPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.findOneByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getRfcType());
	}

	public List<MessageRfcField> getByMsgInboxId(int msgId) {
		return repository.findAllBymessageRfcFieldPK_MessageInbox_RowId(msgId);
	}

	public void delete(MessageRfcField rfcField) {
		if (rfcField == null) return;
		repository.delete(rfcField);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByPrimaryKey(MessageRfcFieldPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getRfcType());
	}

	public int deleteByMsgInboxId(int msgId) {
		return repository.deleteByMessageInboxId(msgId);
	}

	public void update(MessageRfcField rfcField) {
		repository.saveAndFlush(rfcField);
	}

	public void insert(MessageRfcField rfcField) {
		repository.saveAndFlush(rfcField);
	}

}
