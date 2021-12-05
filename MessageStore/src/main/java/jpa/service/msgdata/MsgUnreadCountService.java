package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

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

	public MsgUnreadCount getCount() {
		List<MsgUnreadCount> list = getAll();
		if (list.isEmpty()) {
			MsgUnreadCount count = new MsgUnreadCount();
			upsert(count);
			return count;
		}
		else {
			return list.get(0);
		}
	}
	
	public void increaseInboxCount() {
		increaseInboxCount(1);
	}
	
	public void increaseInboxCount(int inc) {
		MsgUnreadCount count = getCount();
		count.setInboxUnreadCount(count.getInboxUnreadCount() + inc);
		upsert(count);
	}
	
	public void increaseSentCount() {
		increaseSentCount(1);
	}
	
	public void increaseSentCount(int inc) {
		MsgUnreadCount count = getCount();
		count.setSentUnreadCount(count.getSentUnreadCount() + inc);
		upsert(count);
	}
	
	public Optional<MsgUnreadCount> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<MsgUnreadCount> getAll() {
		return repository.findAll();
	}
	
	public void upsert(MsgUnreadCount unreadCount) {
		if (getAll().isEmpty()) {
			repository.saveAndFlush(unreadCount);
		}
		else if (unreadCount.getRowId() != null) {
			repository.saveAndFlush(unreadCount);
		}
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}
}
