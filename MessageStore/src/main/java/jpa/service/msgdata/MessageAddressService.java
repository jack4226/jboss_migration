package jpa.service.msgdata;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageAddress;
import jpa.repository.msg.MessageAddressRepository;

@Component("messageAddressService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageAddressService implements java.io.Serializable {
	private static final long serialVersionUID = 4802721639456016361L;

	static Logger logger = Logger.getLogger(MessageAddressService.class);

	@Autowired
	MessageAddressRepository repository;
	
	public MessageAddress getByRowId(int rowId) {
		return repository.findOne(rowId);
	}

	public MessageAddress getByPrimaryKey(int msgId, String addrType, String address) {
		return repository.findOneByPrimaryKey(msgId, addrType, address);
	}

	public List<MessageAddress> getByMsgInboxId(int msgId) {
		return repository.findAllByMessageInbox_RowId(msgId);
	}

	public void delete(MessageAddress msgAddress) {
		if (msgAddress == null) return;
		repository.delete(msgAddress);;
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByPrimaryKey(int msgId, String addrType, String addrValue) {
		return repository.deleteByPrimaryKey(msgId, addrType, addrValue);
	}

	public int deleteByMsgInboxId(int msgId) {
		return repository.deleteByMessageInboxId(msgId);
	}

	public void update(MessageAddress msgAddress) {
		repository.saveAndFlush(msgAddress);
	}

	public void insert(MessageAddress msgAddress) {
		repository.saveAndFlush(msgAddress);
	}

}
