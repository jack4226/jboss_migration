package jpa.service.msgdata;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	static Logger logger = LogManager.getLogger(MessageRenderedService.class);
	
	@Autowired
	MessageRenderedRepository repository;

	public Optional<MessageRendered> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public Optional<MessageRendered> getByPrimaryKey(int rowId) {
		return getByRowId(rowId);
	}

	public Optional<MessageRendered> getAllDataByPrimaryKey(int rowId) {
		Optional<MessageRendered> mr = getByRowId(rowId);
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
