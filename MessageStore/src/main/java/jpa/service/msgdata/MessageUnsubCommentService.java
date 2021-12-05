package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageUnsubComment;
import jpa.repository.msg.MessageUnsubCommentRepository;

@Component("messageUnsubCommentService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageUnsubCommentService implements java.io.Serializable {
	private static final long serialVersionUID = -4830933844969528333L;

	static Logger logger = Logger.getLogger(MessageUnsubCommentService.class);

	@Autowired
	MessageUnsubCommentRepository repository;

	public Optional<MessageUnsubComment> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public MessageUnsubComment getByMsgInboxId(int msgId) {
		return repository.findOneByMessageInbox_RowId(msgId);
	}

	public List<MessageUnsubComment> getByFromAddress(String address) {
		return repository.findAllByEmailAddr_Address(address);
	}

	public void delete(MessageUnsubComment unsubComment) {
		if (unsubComment == null) return;
		repository.delete(unsubComment);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByMsgInboxId(int msgId) {
		return repository.deleteByMessageInboxId(msgId);
	}

	public void update(MessageUnsubComment unsubComment) {
		repository.saveAndFlush(unsubComment);
	}

	public void insert(MessageUnsubComment unsubComment) {
		repository.saveAndFlush(unsubComment);
	}

}
