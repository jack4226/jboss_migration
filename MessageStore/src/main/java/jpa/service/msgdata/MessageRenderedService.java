package jpa.service.msgdata;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageRendered;
import jpa.repository.msg.MessageRenderedRepository;

@Component("messageRenderedService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageRenderedService implements java.io.Serializable {
	private static final long serialVersionUID = -1300601306632111600L;

	static Logger logger = Logger.getLogger(MessageRenderedService.class);
	
	@Autowired
	MessageRenderedRepository repository;

	public MessageRendered getByRowId(int rowId) {
		return repository.findOne(rowId);
	}

	public MessageRendered getByPrimaryKey(int rowId) {
		return getByRowId(rowId);
	}

	public MessageRendered getAllDataByPrimaryKey(int rowId) {
		MessageRendered mr = getByRowId(rowId);
		return mr;
	}
	
	public MessageRendered getFirstRecord() {
		return repository.findFirstByOrderByRowId();
	}

	public MessageRendered getLastRecord() {
		return repository.findFirstByOrderByRowIdDesc();
	}

	public MessageRendered getPrevoiusRecord(MessageRendered inbox) {
		return repository.findTop1ByRowIdLessThanOrderByRowIdDesc(inbox.getRowId());
	}

	public MessageRendered getNextRecord(MessageRendered inbox) {
		return repository.findTop1ByRowIdGreaterThanOrderByRowId(inbox.getRowId());
	}

	public void delete(MessageRendered rendered) {
		if (rendered == null) return;
		repository.delete(rendered);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void update(MessageRendered rendered) {
		repository.saveAndFlush(rendered);
	}

	public void insert(MessageRendered rendered) {
		repository.saveAndFlush(rendered);
	}

}
