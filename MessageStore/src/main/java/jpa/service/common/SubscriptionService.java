package jpa.service.common;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.Constants;
import jpa.constant.RuleCriteria;
import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.model.MailingList;
import jpa.model.Subscription;
import jpa.msgui.vo.PagingVo;
import jpa.repository.RepositoryUtils;
import jpa.repository.SubscriptionRepository;
import jpa.service.maillist.MailingListService;

@Component("subscriptionService")
@Transactional(propagation=Propagation.REQUIRED)
public class SubscriptionService implements java.io.Serializable {
	private static final long serialVersionUID = 2020862404406193032L;

	static Logger logger = Logger.getLogger(SubscriptionService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	SubscriptionRepository repository;
	
	@Autowired
	EmailAddressService emailAddrService;
	
	@Autowired
	MailingListService mailingListService;
	
	public List<Subscription> getByListId(String listId) {
		return repository.findAllByMailingList_ListId(listId);
	}
	
	public List<Subscription> getByListIdSubscribersOnly(String listId) {
		return repository.findAllByListIdSubscribersOnly(listId);
	}
	
	public List<Subscription> getByListIdProsperctsOnly(String listId) {
		return repository.findAllByListIdProsperctsOnly(listId);
	}
	
	public List<Subscription> getByAddress(String address) {
		return repository.findAllByEmailAddress_Address(address);
	}
	
	public Subscription getByUniqueKey(int emailAddrRowId, String listId) {
		return repository.findOneByEmailAddress_RowIdAndMailingList_ListId(emailAddrRowId, listId);
	}
	
	public Subscription getByAddressAndListId(String address, String listId) {
		return repository.findOneByMailingList_ListIdAndEmailAddress_Address(listId, address);
	}
	
	public Subscription getByRowId(int rowId) {
		return repository.findOne(rowId);
	}
	
	public Subscription subscribe(String address, String listId) {
		EmailAddress emailAddr = emailAddrService.findSertAddress(address);
		MailingList list = mailingListService.getByListId(listId);
		if (list == null) {
			throw new IllegalArgumentException("Mailing List (" + listId + ") not found.");
		}
		Subscription sub = null;
		try {
			sub = getByAddressAndListId(emailAddr.getAddress(), list.getListId());
			if (sub != null) {
				if (!sub.isSubscribed()) {
					sub.setSubscribed(true);
					sub.setStatusId(StatusId.ACTIVE.getValue());
					sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
					sub.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
					update(sub);
				}
			}
			else {
				sub = new Subscription();
				sub.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
				sub.setEmailAddress(emailAddr);
				sub.setMailingList(list);
				sub.setSubscribed(true);
				sub.setStatusId(StatusId.ACTIVE.getValue());
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				insert(sub);
			}
		}
		finally {
			em.detach(sub);
		}
		return sub;
	}

	public Subscription unsubscribe(String address, String listId) {
		// to harvest email address from the request
		EmailAddress emailAddr = emailAddrService.findSertAddress(address);
		MailingList list = mailingListService.getByListId(listId);
		if (list == null) {
			throw new IllegalArgumentException("Mailing List (" + listId + ") not found.");
		}
		Subscription sub = null;
		try {
			sub = getByAddressAndListId(emailAddr.getAddress(), list.getListId());
			if (sub != null) {
				if (sub.isSubscribed()) {
					sub.setSubscribed(false);
					sub.setStatusId(StatusId.INACTIVE.getValue());
					sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
					sub.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
					update(sub);
				}
			}
			else {
				sub = new Subscription();
				sub.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
				sub.setEmailAddress(emailAddr);
				sub.setMailingList(list);
				sub.setSubscribed(false);
				sub.setStatusId(StatusId.INACTIVE.getValue());
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				insert(sub);
			}
		}
		finally {
			em.detach(sub);
		}
		return sub;
	}
	
	public Subscription addToList(String sbsrEmailAddr, String listEmailAddr) {
		MailingList mlist = mailingListService.getByListAddress(listEmailAddr);
		if (mlist == null) {
			throw new IllegalArgumentException("Mailing List Email Address (" + listEmailAddr + ") not found.");
		}
		return subscribe(sbsrEmailAddr, mlist.getListId());
	}
	
	public Subscription removeFromList(String sbsrEmailAddr, String listEmailAddr) {
		MailingList mlist = mailingListService.getByListAddress(listEmailAddr);
		if (mlist == null) {
			throw new IllegalArgumentException("Mailing List Email Address (" + listEmailAddr + ") not found.");
		}
		return unsubscribe(sbsrEmailAddr, mlist.getListId());
	}
	
	public Subscription optInRequest(String address, String listId) {
		EmailAddress emailAddr = emailAddrService.findSertAddress(address);
		MailingList list = mailingListService.getByListId(listId);
		if (list == null) {
			throw new IllegalArgumentException("Mailing List (" + listId + ") not found.");
		}
		Subscription sub = null;
		try {
			sub = getByAddressAndListId(emailAddr.getAddress(), list.getListId());
			if (sub != null) {
				if (!sub.isSubscribed() && !Boolean.TRUE.equals(sub.getIsOptIn())) {
					sub.setIsOptIn(Boolean.TRUE);
					sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
					sub.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
					update(sub);
				}
			}
			else {
				sub = new Subscription();
				sub.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
				sub.setEmailAddress(emailAddr);
				sub.setMailingList(list);
				sub.setSubscribed(false);
				sub.setIsOptIn(Boolean.TRUE);
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				insert(sub);
			}
		}
		finally {
			em.detach(sub);
		}
		return sub;
	}

	public Subscription optInConfirm(String address, String listId) {
		EmailAddress emailAddr = emailAddrService.findSertAddress(address);
		emailAddr.setStatusId(StatusId.ACTIVE.getValue());
		MailingList list = mailingListService.getByListId(listId);
		if (list == null) {
			throw new IllegalArgumentException("Mailing List (" + listId + ") not found.");
		}
		Subscription sub = null;
		try {
			sub = getByAddressAndListId(emailAddr.getAddress(), list.getListId());
			if (sub != null) {
				if (!sub.isSubscribed() && Boolean.TRUE.equals(sub.getIsOptIn())) {
					sub.setSubscribed(true);
					sub.setStatusId(StatusId.ACTIVE.getValue());
					sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
					sub.setUpdtTime(new java.sql.Timestamp(System.currentTimeMillis()));
					update(sub);
				}
			}
			else {
				sub = new Subscription();
				sub.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
				sub.setEmailAddress(emailAddr);
				sub.setMailingList(list);
				sub.setSubscribed(true);
				sub.setStatusId(StatusId.ACTIVE.getValue());
				sub.setUpdtUserId(Constants.DEFAULT_USER_ID);
				insert(sub);
			}
		}
		finally {
			em.detach(sub);
		}
		return sub;
	}

	public void delete(Subscription subscription) {
		if (subscription == null) return;
		repository.delete(subscription);;
	}

	public int deleteByListId(String listId) {
		return repository.deleteByListId(listId);
	}

	public int deleteByAddress(String address) {
		return repository.deleteBySubscriberEmailAddress(address);
	}

	public int deleteByUniqueKey(int emailAddrRowId, String listId) {
		return repository.deleteByUniqueKey(emailAddrRowId, listId);
	}

	public int deleteByAddressAndListId(String address, String listId) {
		return repository.deleteByAddressAndListId(address, listId);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(Subscription subscription) {
		repository.saveAndFlush(subscription);
	}

	public void update(Subscription subscription) {
		repository.saveAndFlush(subscription);
	}

	public int updateSentCount(int rowId) {
		return updateSentCount(rowId, 1);
	}

	public int updateSentCount(int rowId, int mailsSent) {
		String user = Constants.DEFAULT_USER_ID;
		java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
		return repository.updateSentCount(rowId, mailsSent, user, updtTime);
	}

	public int updateClickCount(int emailAddrRowId, String listId) {
		String user = Constants.DEFAULT_USER_ID;
		java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
		return repository.updateClickCount(emailAddrRowId, listId, user, updtTime);
	}

	public int updateOpenCount(int emailAddrRowId, String listId) {
		String user = Constants.DEFAULT_USER_ID;
		java.sql.Timestamp updtTime = new java.sql.Timestamp(System.currentTimeMillis());
		return repository.updateOpenCount(emailAddrRowId, listId, user, updtTime);
	}

	public long getSubscriptionCount(String listId, PagingVo vo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Long> query = builder.createQuery(Long.class);
	    Root<Subscription> root = query.from(Subscription.class);
	    
	    vo.setSearchCriteria(PagingVo.Column.listId, new PagingVo.Criteria(RuleCriteria.EQUALS, listId));
	    
	    List<Predicate> predicates = buildPredicates(vo, builder, root);
	    
		query.where(builder.and(predicates.toArray(new Predicate[] {})));
		
		query.select(builder.count(root));
		
		TypedQuery<Long> sqlQuery = em.createQuery(query);
	    
	    try {
	    	return sqlQuery.getSingleResult();
	    }
	    catch (NoResultException | EmptyResultDataAccessException  e) {
	    	return 0L;
	    }
	}
	
	public List<Subscription> getSubscriptionsWithPaging(String listId, PagingVo vo) {
		int pageNumber = vo.getPageNumber();
		int pageSize = vo.getPageSize();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Subscription> query = builder.createQuery(Subscription.class);
	    Root<Subscription> root = query.from(Subscription.class);
	    
	    vo.setSearchCriteria(PagingVo.Column.listId, new PagingVo.Criteria(RuleCriteria.EQUALS, listId));
	    
	    List<Predicate> predicates = buildPredicates(vo, builder, root);
	    
		query.where(builder.and(predicates.toArray(new Predicate[] {})));
		
		List<Order> orderByList = RepositoryUtils.buildOrderByList(vo, builder, root);
		query.orderBy(orderByList);
		
		TypedQuery<Subscription> sqlQuery = em.createQuery(query);
		
		if (pageSize > 0) {
			sqlQuery.setMaxResults(pageSize);
			sqlQuery.setFirstResult(pageNumber * pageSize);
		}
		
		return sqlQuery.getResultList();
	}
	
	private List<Predicate> buildPredicates(PagingVo vo, CriteriaBuilder builder, Root<Subscription> root) {
	    Join<?,?> joinEmail = root.join("emailAddress", JoinType.INNER);
	    Join<?,?> joinMList = root.join("mailingList", JoinType.INNER);
	    //Join<?,?> joinSbsrData = 
	    		joinEmail.join("subscriberData", JoinType.LEFT);
	    
	    List<Predicate> predicates = new ArrayList<Predicate>();
	    
		List<PagingVo.Column> colList = PagingVo.Column.getColumnsByType(PagingVo.Type.subscipt);
		if (!colList.contains(PagingVo.Column.listId)) {
			Object listIdObj = vo.getSearchValue(PagingVo.Column.listId);
			if (listIdObj != null) {
				String listId = listIdObj.toString();
				predicates.add(builder.equal(joinMList.<String>get(PagingVo.Column.listId.name()), listId.trim()));
			}
		}
		for (PagingVo.Column col : colList) {
			if (vo.getSearchValue(col) == null) {
				continue;
			}
			if (PagingVo.Column.listId.equals(col)) {
				String listId = vo.getSearchValue(col).toString();
				predicates.add(builder.equal(joinMList.<String>get(col.name()), listId.trim()));
			}
			else if (PagingVo.Column.statusId.equals(col)) {
				String statusId= vo.getSearchValue(col).toString();
				predicates.add(builder.equal(joinEmail.<String>get(col.name()), statusId.trim()));
			}
			else if (PagingVo.Column.origAddress.equals(col)) {
				predicates.addAll(RepositoryUtils.buildPredicateList(col, vo.getSearchCriteria(col), builder, (From<?,?>) joinEmail));
			}
			else {
				predicates.addAll(RepositoryUtils.buildPredicateList(col, vo.getSearchCriteria(col), builder, root));
			}
		}
		return predicates;
	}
}
