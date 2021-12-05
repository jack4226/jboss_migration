package jpa.service.common;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.SessionUpload;
import jpa.model.SessionUploadPK;
import jpa.repository.SessionUploadRepository;

@Component("sessionUploadService")
@Transactional(propagation=Propagation.REQUIRED)
public class SessionUploadService implements java.io.Serializable {
	private static final long serialVersionUID = -2378096938090291686L;

	static Logger logger = LogManager.getLogger(SessionUploadService.class);
	
	@Autowired
	SessionUploadRepository repository;

	public List<SessionUpload> getBySessionId(String sessionId) {
		return repository.findAllBySessionUploadPK_SessionId(sessionId);
	}
	
	public Optional<SessionUpload> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public SessionUpload getByPrimaryKey(SessionUploadPK pk) {
		return repository.findOneBySessionUploadPK_SessionIdAndSessionUploadPK_SessionSequence(pk.getSessionId(), pk.getSessionSequence());
	}
	
	public List<SessionUpload> getByUserId(String userId) {
		return repository.findAllByUserData_UserId(userId);
	}
	
	public void delete(SessionUpload session) {
		if (session==null) return;
		repository.delete(session);
	}

	public int deleteAll() {
		return repository.deleteAllRecords();
	}

	public int deleteExpired(int minutes) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, -minutes); // roll back time
		Timestamp rollback = new Timestamp(cal.getTimeInMillis());
		return repository.deleteExpired(rollback);
	}

	public int deleteBySessionId(String sessionId) {
		return repository.deleteBySessionId(sessionId);
	}

	public int deleteByPrimaryKey(SessionUploadPK pk) {
		return repository.deleteByPrimaryKey(pk.getSessionId(), pk.getSessionSequence());
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(SessionUpload session) {
		repository.saveAndFlush(session);
	}
	
	public void insertLast(SessionUpload session) {
		Integer lastSeq = repository.findLastSessionSequence(session.getSessionUploadPK().getSessionId());
		if (lastSeq != null) {
			session.getSessionUploadPK().setSessionSequence(lastSeq + 1);
		}
		else {
			session.getSessionUploadPK().setSessionSequence(0);
		}
		repository.saveAndFlush(session);
	}
	
	public void update(SessionUpload session) {
		repository.saveAndFlush(session);
	}
	
}
