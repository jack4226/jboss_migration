package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageAttachment;
import jpa.model.msg.MessageAttachmentPK;
import jpa.repository.msg.MessageAttachmentRepository;

@Component("messageAttachmentService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageAttachmentService implements java.io.Serializable {
	private static final long serialVersionUID = -844000059190931006L;

	static Logger logger = Logger.getLogger(MessageAttachmentService.class);
	
	@Autowired
	MessageAttachmentRepository repository;
	
	public Optional<MessageAttachment> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public MessageAttachment getByPrimaryKey(MessageAttachmentPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.findOneByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getAttachmentDepth(), pk.getAttachmentSequence());
	}

	public List<MessageAttachment> getByMsgInboxId(int msgId) {
		return repository.findAllByMessageAttachmentPK_MessageInbox_RowId(msgId);
	}

	public void delete(MessageAttachment msgAttch) {
		if (msgAttch == null) return;
		repository.delete(msgAttch);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByPrimaryKey(MessageAttachmentPK pk) {
		if (pk.getMessageInbox()==null) {
			throw new IllegalArgumentException("A MessageInbox instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getMessageInbox().getRowId(), pk.getAttachmentDepth(), pk.getAttachmentSequence());
	}

	public int deleteByMsgInboxId(int msgId) {
		return repository.deleteByMessageInboxId(msgId);
	}

	public void update(MessageAttachment msgAttch) {
		repository.saveAndFlush(msgAttch);
	}

	public void insert(MessageAttachment msgAttch) {
		repository.saveAndFlush(msgAttch);
	}

}
