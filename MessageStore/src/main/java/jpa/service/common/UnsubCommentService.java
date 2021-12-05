package jpa.service.common;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.UnsubComment;
import jpa.repository.UnsubCommentRepository;

@Component("unsubCommentService")
@Transactional(propagation=Propagation.REQUIRED)
public class UnsubCommentService implements java.io.Serializable {
	private static final long serialVersionUID = -6069568690334155415L;

	static Logger logger = Logger.getLogger(UnsubCommentService.class);
	
	@Autowired
	UnsubCommentRepository repository;

	public List<UnsubComment> getByAddress(String address) {
		return repository.findAllByEmailAddress_Address(address);
	}
	
	public Optional<UnsubComment> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<UnsubComment> getByMailingListId(String listId) {
		return repository.findAllByMailingList_ListId(listId);
	}
	
	public List<UnsubComment> getByAddressAndListId(String address, String listId) {
		return repository.findAllByEmailAddress_AddressAndMailingList_ListId(address, listId);
	}
	
	public void delete(UnsubComment comment) {
		if (comment==null) return;
		repository.delete(comment);
	}

	public int deleteByAddress(String address) {
		return repository.deleteByAddress(address);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(UnsubComment comment) {
		repository.saveAndFlush(comment);
	}
	
	public void update(UnsubComment comment) {
		repository.saveAndFlush(comment);
	}
	
}
