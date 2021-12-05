package jpa.service.common;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.UserData;
import jpa.repository.UserDataRepository;

@Component("userDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class UserDataService implements Serializable {
	private static final long serialVersionUID = 6193420138194185032L;

	static Logger logger = Logger.getLogger(UserDataService.class);
	
	@Autowired
	UserDataRepository repository;

	public UserData getByUserId(String userId) {
		return repository.findOneByUserId(userId);
	}
	
	public UserData getForLogin(String userId, String password) {
		return repository.findOneByUserIdAndPassword(userId, password);
	}
	
	public Optional<UserData> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<UserData> getAll() {
		return repository.findAllByOrderByUserId();
	}
	
	public void delete(UserData user) {
		if (user==null) return;
		repository.delete(user);
	}

	public int deleteByUserId(String userId) {
		return repository.deleteByUserId(userId);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(UserData user) {
		repository.saveAndFlush(user);
	}
	
	public void update(UserData user) {
		repository.saveAndFlush(user);
	}

	public int update4Web(UserData user) {
		return repository.updateUserDataForWeb(user.getSessionId(), user.getLastVisitTime(), user.getHits(), user.getRowId());
	}

}
