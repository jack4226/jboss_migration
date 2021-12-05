package jpa.service.common;

import java.util.List;
import java.util.Optional;

import jpa.model.IdTokens;
import jpa.repository.IdTokensRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("idTokensService")
@Transactional(propagation=Propagation.REQUIRED)
public class IdTokensService implements java.io.Serializable {
	private static final long serialVersionUID = 322132246530329818L;

	static Logger logger = LogManager.getLogger(IdTokensService.class);
	
	//@PersistenceContext(unitName="MessageDB")
	
	@Autowired
	IdTokensRepository repository;
	
	public IdTokens getBySenderId(String senderId) {
		return repository.findOneBySenderData_SenderId(senderId);
	}
	
	public Optional<IdTokens> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<IdTokens> getAll() {
		return repository.findAll();
	}
	
	public void delete(IdTokens idTokens) {
		if (idTokens == null) return;
		repository.delete(idTokens);
	}

	public int deleteBySenderId(String senderId) {
		return repository.deleteBySenderId(senderId);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(IdTokens idTokens) {
		repository.saveAndFlush(idTokens);
	}

	public void update(IdTokens idTokens) {
		// XXX try save() if encounter a long delay in IdTokens1Test with hibernate
		repository.saveAndFlush(idTokens);
	}
}
