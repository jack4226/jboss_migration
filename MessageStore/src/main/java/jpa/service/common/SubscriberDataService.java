package jpa.service.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.MobileCarrierEnum;
import jpa.exception.DataValidationException;
import jpa.model.SubscriberData;
import jpa.msgui.vo.PagingSubscriberData;
import jpa.msgui.vo.PagingVo;
import jpa.repository.RepositoryUtils;
import jpa.repository.SubscriberDataRepository;
import jpa.util.EmailSender;
import jpa.util.PhoneNumberUtil;

@Component("subscriberDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class SubscriberDataService implements java.io.Serializable {
	private static final long serialVersionUID = 744183660636136777L;

	static Logger logger = LogManager.getLogger(SubscriberDataService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	SubscriberDataRepository repository;
	
	public SubscriberData getBySubscriberId(String subscriberId) {
		return repository.findOneBySubscriberId(subscriberId);
	}
	
	public Optional<SubscriberData> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public SubscriberData getByEmailAddress(String address) {
		return repository.findOneByEmailAddress_Address(address);
	}
	
	public List<SubscriberData> getAll() {
		return repository.findAllByOrderBySubscriberId();
	}
	
	public void delete(SubscriberData subscriber) {
		if (subscriber==null) return;
		repository.delete(subscriber);;
 	}

	public int deleteBySubscriberId(String subscriberId) {
		return repository.deleteBySubscriberId(subscriberId);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(SubscriberData subscriber) {
		verifySubscriberData(subscriber);
		repository.saveAndFlush(subscriber);
	}
	
	public void update(SubscriberData subscriber) {
		verifySubscriberData(subscriber);
		repository.saveAndFlush(subscriber);
	}
	
	private void verifySubscriberData(SubscriberData subscriber) throws DataValidationException {
		if (StringUtils.isNotBlank(subscriber.getMobilePhone())) {
			if (!PhoneNumberUtil.isValidPhoneNumber(subscriber.getMobilePhone())) {
				throw new DataValidationException("Invalid Mobile phone number passed in: " + subscriber.getMobilePhone());
			}
			try {
				MobileCarrierEnum.getByValue(subscriber.getMobileCarrier());
			}
			catch (IllegalArgumentException e) {
				logger.warn("IllegalArgumentException caught: " + e.getMessage());
				// could be a new carrier not yet entered in system, notify programming
				String msg = "Invalid Mobile carrier passed in: " + subscriber.getMobileCarrier();
				String subj = "(" + subscriber.getMobileCarrier() + ") need to be added to the system - {0}";
				EmailSender.sendEmail(subj, msg, ExceptionUtils.getStackTrace(e), EmailSender.EmailList.ToDevelopers);
			}
		}
		if (subscriber.getEmailAddress()==null) {
			throw new IllegalArgumentException("An EmailAddress instance must be provided in the entity.");
		}
	}

	public long getSubscriberCount(PagingSubscriberData vo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Long> query = builder.createQuery(Long.class);
	    Root<SubscriberData> root = query.from(SubscriberData.class);
	    
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
	
	public List<SubscriberData> getSubscribersWithPaging(PagingSubscriberData vo) {
		int pageNumber = vo.getPagingVo().getPageNumber();
		int pageSize = vo.getPagingVo().getPageSize();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<SubscriberData> query = builder.createQuery(SubscriberData.class);
	    Root<SubscriberData> root = query.from(SubscriberData.class);
	    
	    List<Predicate> predicates = buildPredicates(vo, builder, root);
	    
		query.where(builder.and(predicates.toArray(new Predicate[] {})));
		
		List<Order> orderByList = RepositoryUtils.buildOrderByList(vo.getPagingVo(), builder, root);
		query.orderBy(orderByList);
		
		TypedQuery<SubscriberData> sqlQuery = em.createQuery(query);
		
		if (pageSize > 0) {
			sqlQuery.setMaxResults(pageSize);
			sqlQuery.setFirstResult(pageNumber * pageSize);
		}
		
		return sqlQuery.getResultList();
	}
	
	private List<Predicate> buildPredicates(PagingSubscriberData vo, CriteriaBuilder builder, Root<SubscriberData> root) {
	    Join<?,?> joinSender = root.join("senderData", JoinType.INNER);
	    Join<?,?> joinEmail = root.join("emailAddress", JoinType.LEFT);
	    
	    List<Predicate> predicates = new ArrayList<Predicate>();
	    
		List<PagingVo.Column> colList = PagingVo.Column.getColumnsByType(PagingVo.Type.subrdata);
		for (PagingVo.Column col : colList) {
			if (vo.getPagingVo().getSearchValue(col) == null) {
				continue;
			}
			if (PagingVo.Column.address.equals(col)) {
				predicates.addAll(RepositoryUtils.buildPredicateList(col, vo.getPagingVo().getSearchCriteria(col), builder, (From<?, ?>)joinEmail));
			}
			else if (PagingVo.Column.senderId.equals(col)) {
				String senderId = vo.getPagingVo().getSearchValue(col).toString();
				predicates.add(builder.equal(joinSender.<String>get(col.name()), senderId.trim()));
			}
			else {
				predicates.addAll(RepositoryUtils.buildPredicateList(col, vo.getPagingVo().getSearchCriteria(col), builder, root));
			}
		}
		return predicates;
	}
	
}
