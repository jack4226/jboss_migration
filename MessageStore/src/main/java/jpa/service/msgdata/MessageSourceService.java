package jpa.service.msgdata;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageSource;
import jpa.repository.msg.MessageSourceRepository;

@Component("messageSourceService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageSourceService implements java.io.Serializable {
	private static final long serialVersionUID = -7039424791259148458L;

	static Logger logger = Logger.getLogger(MessageSourceService.class);
	
	@Autowired
	MessageSourceRepository repository;
	
	public MessageSource getByMsgSourceId(String sourceId) {
		return repository.findOneByMsgSourceId(sourceId);
	}
	
	public MessageSource getByRowId(int rowId) {
		return repository.findOne(rowId);
	}

	public List<MessageSource> getByFromAddress(String address) {
		return repository.findAllByFromAddress_Address(address);
	}

	public List<MessageSource> getAll() {
		return repository.findAll();
	}
	
	public void delete(MessageSource source) {
		if (source==null) return;
		repository.delete(source);
	}

	public int deleteByMsgSourceId(String sourceId) {
		return repository.deleteByMsgSourceId(sourceId);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(MessageSource source) {
		repository.saveAndFlush(source);
	}
	
	public void update(MessageSource source) {
		repository.saveAndFlush(source);
	}

}
