package jpa.service.maillist;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import jpa.constant.StatusId;
import jpa.model.MailingList;
import jpa.repository.MailingListRepository;
import jpa.util.EmailAddrUtil;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("mailingListService")
@Transactional(propagation=Propagation.REQUIRED)
public class MailingListService implements java.io.Serializable {
	private static final long serialVersionUID = 8375902506904904765L;

	static Logger logger = Logger.getLogger(MailingListService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	MailingListRepository repository;
	
	final static String GroupBy = "group by " +
				" a.row_id, " +
				" a.ListId, " +
				" a.DisplayName, " +
				" a.AcctUserName, " +
				" a.Description, " +
				" a.status_id, " +
				" a.IsBuiltin, " +
				" a.IsSendText, " +
				" a.CreateTime, " +
				" a.updt_user_id, " +
				" a.updt_time, " +
				" a.SenderDataRowId, " +
				" a.ListMasterEmailAddr ";
	
	public MailingList getByListId(String listId) {
		return repository.findOneByListId(listId);
	}

	public MailingList getByListAddress(String address) {
		String domain = EmailAddrUtil.getEmailDomainName(address);
		String acctUser = EmailAddrUtil.getEmailUserName(address);
		return repository.findOneByAcctUserNameAndSenderData_DomainName(acctUser, domain);
	}

	public List<MailingList> getByAddressWithCounts(String address) {
		String sql =
			"select a.*, b.isSubscribed, " +
			" sum(b.SentCount) as sentCount, " +
			" sum(b.OpenCount) as openCount, " +
			" sum(b.ClickCount) as clickCount " +
			"from Mailing_List a " +
			"left outer join Subscription b on a.row_id = b.MailingListRowId " +
			"join Email_Address e on e.row_id = b.EmailAddrRowId " +
				" where e.address=? ";
		sql += GroupBy + ", b.isSubscribed ";
		try {
			Query query = em.createNativeQuery(sql,MailingList.MAPPING_MAILING_LIST_WITH_COUNTS);
			query.setParameter(1, address);
			@SuppressWarnings("unchecked")
			List<Object[]> objList = query.getResultList();
			List<MailingList> list = new ArrayList<MailingList>();
			for (Object[] listObj : objList) {
				MailingList mlist = (MailingList) listObj[0];
				mlist.setSentCount(numberToInteger(listObj[1]));
				mlist.setOpenCount(numberToInteger(listObj[2]));
				mlist.setClickCount(numberToInteger(listObj[3]));
				list.add(mlist);
			}
			return list;
		}
		finally {
		}
	}

	/*
	 * return an array with 4 elements:
	 * 1) MailingList
	 * 2) through 4) BigDecimal (MySQL) or BigInteger (PostgreSQL)
	 */
	public MailingList getByListIdWithCounts(String listId) {
		String sql = "select a.*, " +
				" sum(b.SentCount) as sentCount, sum(b.OpenCount) as openCount," +
				" sum(b.ClickCount) as clickCount, " +
				" min(case when b.isSubscribed then 0 else 1 end) = 0 as isSubscribed " +
				"from Mailing_List a " +
				" LEFT OUTER JOIN Subscription b on a.row_id = b.MailingListRowId " +
				" JOIN sender_data c on a.SenderDataRowId = c.row_id " +
				" where a.ListId = ?1 " +
				GroupBy;
		try {
			Query query = em.createNativeQuery(sql,MailingList.MAPPING_MAILING_LIST_WITH_COUNTS);
			query.setParameter(1, listId);
			Object[] listObj = (Object[]) query.getSingleResult();
			MailingList mailingList = (MailingList) listObj[0];
			mailingList.setSentCount(numberToInteger(listObj[1]));
			mailingList.setOpenCount(numberToInteger(listObj[2]));
			mailingList.setClickCount(numberToInteger(listObj[3]));
			return mailingList;
		}
		catch (NoResultException e) {
			return null;
		}
		finally {
		}
	}
	
	private Integer numberToInteger(Object number) {
		if (number instanceof Number) {
			return ((Number)number).intValue();
		}
		return null;
	}

	public Optional<MailingList> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<MailingList> getAll(boolean onlyActive) {
		if (onlyActive) {
			return repository.findAllByStatusIdOrderByListId(StatusId.ACTIVE.getValue());
		}
		else {
			return repository.findAllByOrderByListId();
		}
	}
	
	public void delete(MailingList mailingList) {
		if (mailingList == null) return;
		repository.delete(mailingList);
	}

	public int deleteByListId(String listId) {
		return repository.deleteByListId(listId);
	}

	public int deleteBySenderId(String senderId) {
		return repository.deleteBySenderId(senderId);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(MailingList mailingList) {
		repository.saveAndFlush(mailingList);
	}

	public void update(MailingList mailingList) {
		repository.saveAndFlush(mailingList);
	}
}
