package jpa.service.msgout;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.MailServerType;
import jpa.constant.StatusId;
import jpa.model.SmtpServer;
import jpa.repository.SmtpServerRepository;

@Component("smtpServerService")
@Transactional(propagation=Propagation.REQUIRED)
public class SmtpServerService implements java.io.Serializable {
	private static final long serialVersionUID = 5535796998527412454L;

	static Logger logger = LogManager.getLogger(SmtpServerService.class);
	
	@Autowired
	EntityManager em;

	@Autowired
	SmtpServerRepository repository;
	
	public Optional<SmtpServer> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public SmtpServer getByServerName(String serverName) {
		return repository.findOneByServerName(serverName);
	}

	public List<SmtpServer> getAll(boolean onlyActive, Boolean isSecure) {
		String sql = 
				"select t " +
					" from SmtpServer t ";
		if (onlyActive) {
			sql += " where t.statusId=:statusId ";
		}
		if (isSecure!=null) {
			if (sql.indexOf("where")>0) {
				sql += " and t.isUseSsl=:isUseSsl ";
			}
			else {
				sql += " where t.isUseSsl=:isUseSsl ";
			}
		}
		sql += " order by t.rowId ";
		try {
			Query query = em.createQuery(sql);
			if (onlyActive) {
				query.setParameter("statusId", StatusId.ACTIVE.getValue());
			}
			if (isSecure!=null) {
				query.setParameter("isUseSsl", isSecure);
			}
			@SuppressWarnings("unchecked")
			List<SmtpServer> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<SmtpServer> getByServerType(MailServerType type, boolean onlyActive) {
		if (type == null) {
			throw new IllegalArgumentException("Mail server type cannot be null !!!");
		}
		if (onlyActive) {
			return repository.findAllByServerTypeAndStatusId(type.value(), StatusId.ACTIVE.getValue());
		}
		else {
			return repository.findAllByServerType(type.value());
		}
	}

	public void delete(SmtpServer var) {
		if (var == null) return;
		repository.delete(var);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByServerName(String serverName) {
		return repository.deleteByServerName(serverName);
	}

	public void update(SmtpServer var) {
		repository.saveAndFlush(var);
	}

	public void insert(SmtpServer var) {
		repository.saveAndFlush(var);
	}

}
