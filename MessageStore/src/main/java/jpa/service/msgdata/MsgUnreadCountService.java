package jpa.service.msgdata;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MsgUnreadCount;
import jpa.repository.msg.MsgUnreadCountRepository;

@Component("msgUnreadCountService")
@Transactional(propagation=Propagation.REQUIRED)
public class MsgUnreadCountService implements java.io.Serializable {
	private static final long serialVersionUID = -967482059475014017L;
	
	static Logger logger = Logger.getLogger(MsgUnreadCountService.class);
	
	@Autowired
	MsgUnreadCountRepository repository;

	public MsgUnreadCount getByRowId(int rowId) {
		return repository.findOne(rowId);
	}
	
	public List<MsgUnreadCount> getAll() {
		return repository.findAll();
	}
	
	public void insert(MsgUnreadCount unreadCount) {
		repository.saveAndFlush(unreadCount);
	}

	public void update(MsgUnreadCount unreadCount) {
		repository.saveAndFlush(unreadCount);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}
}
