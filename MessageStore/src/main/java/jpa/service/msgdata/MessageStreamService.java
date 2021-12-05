package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageStream;
import jpa.repository.msg.MessageStreamRepository;

@Component("messageStreamService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageStreamService implements java.io.Serializable {
	private static final long serialVersionUID = -7059809433237042013L;

	static Logger logger = Logger.getLogger(MessageStreamService.class);
	
	@Autowired
	MessageStreamRepository repository;

	public Optional<MessageStream> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public List<MessageStream> getByFromAddress(String address) {
		List<MessageStream> list = repository.findAllByFromAddress(address);
		return list;
	}

	public MessageStream getByMsgInboxId(int msgId) {
		return repository.findOneByMessageInbox_RowId(msgId);
	}

	public MessageStream getLastRecord() {
		return repository.findFirstByOrderByRowIdDesc();
	}

	public void delete(MessageStream stream) {
		if (stream == null) return;
		repository.delete(stream);;
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByMsgInboxId(int msgId) {
		return repository.deleteByMessageInboxId(msgId);
	}

	public void update(MessageStream stream) {
		repository.saveAndFlush(stream);
	}

	public void insert(MessageStream stream) {
		repository.saveAndFlush(stream);
	}

}
