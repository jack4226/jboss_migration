package jpa.service.msgin;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.MailInbox;
import jpa.model.MailInboxPK;

@Component("mailInboxService")
@Transactional(propagation=Propagation.REQUIRED)
public class MailInboxService implements java.io.Serializable {
	private static final long serialVersionUID = -8850544651099338619L;

	static Logger logger = LogManager.getLogger(MailInboxService.class);
	
	@Autowired
	EntityManager em;
	
	public MailInbox getByPrimaryKey(MailInboxPK pk) {
		try {
			Query query = em.createQuery("select t from MailInbox t " +
					" where t.mailInboxPK.userId=:userId and t.mailInboxPK.hostName=:hostName");
			query.setParameter("userId", pk.getUserId());
			query.setParameter("hostName", pk.getHostName());
			MailInbox inbox = (MailInbox) query.getSingleResult();
			return inbox;
		}
		catch (NoResultException e) {
			return null;
		}
		finally {
		}
	}
	
	public MailInbox getByRowId(int rowId) {
		try {
			Query query = em.createQuery("select t from MailInbox t where t.rowId = :rowId");
			query.setParameter("rowId", rowId);
			MailInbox inbox = (MailInbox) query.getSingleResult();
			return inbox;
		}
		catch (NoResultException e) {
			return null;
		}
		finally {
		}
	}
	
	public List<MailInbox> getAll(boolean onlyActive) {
		String sql = "select t from MailInbox t";
		if (onlyActive) {
			sql += " where t.statusId=:statusId ";
		}
		sql += " order by rowId ";
		try {
			Query query = em.createQuery(sql);
			if (onlyActive) {
				query.setParameter("statusId", StatusId.ACTIVE.getValue());
			}
			@SuppressWarnings("unchecked")
			List<MailInbox> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public void delete(MailInbox inbox) {
		if (inbox == null) return;
		try {
			em.remove(inbox);
		}
		finally {
		}
	}

	public int deleteByPrimaryKey(MailInboxPK pk) {
		try {
			Query query = em.createQuery("delete from MailInbox t where " +
					" t.mailInboxPK.userId=:userId and t.mailInboxPK.hostName=:hostName");
			query.setParameter("userId", pk.getUserId());
			query.setParameter("hostName", pk.getHostName());
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		try {
			Query query = em.createQuery("delete from MailInbox t where t.rowId=:rowId");
			query.setParameter("rowId", rowId);
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public void insert(MailInbox inbox) {
		try {
			em.persist(inbox);
			em.flush();
		}
		finally {
		}
	}

	public void update(MailInbox inbox) {
		try {
			if (em.contains(inbox)) {
				em.persist(inbox);
			}
			else {
				em.merge(inbox);
			}
		}
		finally {
		}
	}
}
