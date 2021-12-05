package jpa.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;

import jpa.constant.Constants;
import jpa.constant.RuleCriteria;
import jpa.constant.StatusId;
import jpa.model.EmailAddress;
import jpa.msgui.vo.PagingVo;
import jpa.util.EmailAddrUtil;
import jpa.util.ExceptionUtil;
import jpa.util.JpaUtil;

public class EmailAddressRepositoryImpl implements EmailAddressRepositoryCustom, java.io.Serializable {
	private static final long serialVersionUID = 7017336555271334679L;

	static Logger logger = LogManager.getLogger(EmailAddressRepositoryImpl.class);

	@Autowired
	EmailAddressRepository repository;
	
	@Autowired
	EntityManager em;
	
	@Override
	public EmailAddress findSertAddress(String addr) {
		return findSertAddress(addr, 0);
	}

	private EmailAddress findSertAddress(String addr, int retries) {
		EmailAddress emailAddr = null;
		try {
			emailAddr = repository.findOneByAddress(EmailAddrUtil.removeDisplayName(addr));
		}
		catch (OptimisticLockException e) {
			logger.warn("OptimisticLockException caught, clear EntityManager and try again...");
			repository.flush();
			emailAddr = repository.findOneByAddress(EmailAddrUtil.removeDisplayName(addr));
		}
		if (emailAddr != null) {
			return emailAddr;
		}
		logger.debug("Email Address (" + addr + ") not found, insert...");
		emailAddr = new EmailAddress();
		emailAddr.setAddress(EmailAddrUtil.removeDisplayName(addr));
		emailAddr.setOrigAddress(addr);
		emailAddr.setAcceptHtml(true);
		emailAddr.setStatusId(StatusId.ACTIVE.getValue());
		//emailAddr.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
		//emailAddr.setStatusChangeTime(new java.sql.Timestamp(System.currentTimeMillis()));
		emailAddr.setUpdtUserId(Constants.DEFAULT_USER_ID);
		try {
			repository.saveAndFlush(emailAddr);
			return emailAddr; //getByAddress(addr);
		} catch (DuplicateKeyException dke) {
			logger.error("findSertAddress() - DuplicateKeyException caught", dke);
			if (retries < 0) {
				// retry once may overcome concurrency issue. (the retry
				// never worked and the reason might be that it is under
				// a same transaction). So no retry from now on.
				logger.info("findSertEmailAddr - duplicate key error, retry...");
				return findSertAddress(addr, retries + 1);
			}
			return repository.findOneByAddress(EmailAddrUtil.removeDisplayName(addr));
		}
	}
	
	/*
	 * build and run query using JPA Criteria.
	 * @see jpa.repository.EmailAddressRepositoryCustom#findByAddressWithCounts(java.lang.String)
	 */
	@Override
	public EmailAddress findByAddressWithCounts(String addr) {
		String addrStr = EmailAddrUtil.removeDisplayName(addr);
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Tuple> tupleQuery = builder.createTupleQuery();
	    Root<EmailAddress> root = tupleQuery.from(EmailAddress.class);
		
	    Predicate predicate = builder.equal(builder.lower(root.get("address")), builder.parameter(String.class, "addrStr"));
	    tupleQuery.where(predicate);
	    
		List<Selection<?>> selectionList = new ArrayList<Selection<?>>();
		List<Expression<?>> groupByList = new ArrayList<>();
		
	    selectionList.add(0, root);
	    groupByList.addAll(getGroupByList(root));
		
		Join<?,?> joinSub = root.join("subscriptionList", JoinType.LEFT);
		selectionList.add(1, builder.sum(joinSub.get("sentCount")));
		selectionList.add(2, builder.sum(joinSub.get("openCount")));
		selectionList.add(3, builder.sum(joinSub.get("clickCount")));
		
		tupleQuery.multiselect(selectionList).groupBy(groupByList);
		tupleQuery.distinct(true);
		
		TypedQuery<Tuple> sqlQuery = em.createQuery(tupleQuery);
		sqlQuery.setParameter("addrStr", StringUtils.lowerCase(addrStr));
		try {
			Tuple tuple = sqlQuery.getSingleResult();
			EmailAddress emailAddr  = tuple.get(0, EmailAddress.class);
			emailAddr.setSentCount(numberToInteger(tuple.get(1)));
			emailAddr.setOpenCount(numberToInteger(tuple.get(2)));
			emailAddr.setClickCount(numberToInteger(tuple.get(3)));
			return emailAddr;
		}
		catch (NoResultException | EmptyResultDataAccessException  e) {
			return null;
		}
	}
	
	private List<Expression<?>> getGroupByList(Root<EmailAddress> root) {
		List<Expression<?>> groupByList = new ArrayList<>();
		if (JpaUtil.isDerbyDatabase() || JpaUtil.isHibernate()) {
			groupByList.add(root.get("rowId"));
			groupByList.add(root.get("statusId"));
			groupByList.add(root.get("updtTime"));
			groupByList.add(root.get("updtUserId"));
			groupByList.add(root.get("address"));
			groupByList.add(root.get("statusChangeTime"));
			groupByList.add(root.get("statusChangeUserId"));
			groupByList.add(root.get("bounceCount"));
			groupByList.add(root.get("lastBounceTime"));
			groupByList.add(root.get("lastSentTime"));
			groupByList.add(root.get("lastRcptTime"));
			groupByList.add(root.get("isAcceptHtml"));
			groupByList.add(root.get("origAddress"));
			groupByList.add(root.get("optLock"));
		}
		else {
			groupByList.add(root);
		}
		return groupByList;
	}
	
	@Override
	public void updateBounceCount(EmailAddress emailAddr) {
		emailAddr.setBounceCount(emailAddr.getBounceCount()+1);
		if (emailAddr.getBounceCount() >= Constants.BOUNCE_SUSPEND_THRESHOLD) {
			if (!StatusId.SUSPENDED.getValue().equals(emailAddr.getStatusId())) {
				emailAddr.setStatusId(StatusId.SUSPENDED.getValue());
				if (StringUtils.isNotBlank(emailAddr.getUpdtUserId())) {
					emailAddr.setStatusChangeUserId(emailAddr.getUpdtUserId());
				} else {
					emailAddr.setStatusChangeUserId(Constants.DEFAULT_USER_ID);
				}
				emailAddr.setStatusChangeTime(new java.sql.Timestamp(System.currentTimeMillis()));
			}
		}
		try {
			repository.save(emailAddr);
		}
		catch (PersistenceException e) {
			Exception ex = ExceptionUtil.findException(e, java.sql.SQLException.class);
			if (ex != null && ex.getMessage().contains("Lock wait timeout exceeded")) {
				logger.error("in updateBounceCount() - update failed due to deadlock, ignored.");
			}
			else {
				throw e;
			}
		}
	}

	/*
	 * build and run query using JPA Criteria, for UI.
	 * @see jpa.repository.EmailAddressRepositoryCustom#findRowIdForPreview()
	 */
	@Override
	public int findRowIdForPreview() {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Object> query = builder.createQuery();
	    Root<EmailAddress> root = query.from(EmailAddress.class);
	    root.join("subscriberData", JoinType.INNER);
	    query.select(builder.min(root.get("rowId")));
	    TypedQuery<Object> sqlQuery = em.createQuery(query);
	    try {
		    Object obj = sqlQuery.getSingleResult();
		    if (numberToInteger(obj) == 0) {
		    	query = builder.createQuery();
			    root = query.from(EmailAddress.class);
			    query.select(builder.min(root.get("rowId")));
			    sqlQuery = em.createQuery(query);
			    try {
			    	obj = sqlQuery.getSingleResult();
			    }
			    catch (NoResultException | EmptyResultDataAccessException e0) {
			    	return 0;
			    }
		    }
		    return numberToInteger(obj);
		}
		catch (NoResultException | EmptyResultDataAccessException e) {
			return 0;
		}
	}

	/*
	 * build and run query using JPA Criteria.
	 * @see jpa.repository.EmailAddressRepositoryCustom#findEmailAddressCount(jpa.msgui.vo.PagingVo)
	 */
	@Override
	public int countAllByPagingVo(PagingVo vo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Object> query = builder.createQuery();
	    Root<EmailAddress> root = query.from(EmailAddress.class);
	    List<Predicate> predicates = buildPredicates(vo, builder, root);
	    query.select(builder.count(root));
	    query.where(builder.and(predicates.toArray(new Predicate[] {})));
	    TypedQuery<Object> sqlQuery = em.createQuery(query);
	    try {
		    Object obj = sqlQuery.getSingleResult();
		    return numberToInteger(obj);
		}
		catch (NoResultException | EmptyResultDataAccessException e) {
			return 0;
		}
	}


	/*
	 * build and run query using JPA Criteria.
	 * @see jpa.repository.EmailAddressRepositoryCustom#findAllByPagingVo(jpa.msgui.vo.PagingVo)
	 */
	@Override
	public List<EmailAddress> findAllByPagingVo(PagingVo vo) {
		
		int pageSize = vo.getPageSize();
	    int pageNum = vo.getPageNumber();
	    
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Tuple> tupleQuery = builder.createTupleQuery();
	    Root<EmailAddress> root = tupleQuery.from(EmailAddress.class);
	    
		List<Predicate> predicates = buildPredicates(vo, builder, root);
		
		tupleQuery.where(builder.and(predicates.toArray(new Predicate[] {})));
		
		List<Selection<?>> selectionList = new ArrayList<Selection<?>>();
		List<Order> orderByList = RepositoryUtils.buildOrderByList(vo, builder, root);
		List<Expression<?>> groupByList = new ArrayList<>();
		
	    selectionList.add(0, root);
	    groupByList.addAll(getGroupByList(root));
	    
		tupleQuery.orderBy(orderByList);
		
		Join<?,?> joinSub = root.join("subscriptionList", JoinType.LEFT);
		//selectionList.add(builder.count(joinSub));
		selectionList.add(1, builder.sum(joinSub.get("sentCount")));
		selectionList.add(2, builder.sum(joinSub.get("openCount")));
		selectionList.add(3, builder.sum(joinSub.get("clickCount")));
		
		tupleQuery.multiselect(selectionList).groupBy(groupByList);
		tupleQuery.distinct(true);
		
		TypedQuery<Tuple> sqlQuery = em.createQuery(tupleQuery);
		
		if (pageSize > 0) {
			sqlQuery.setMaxResults(pageSize);
			sqlQuery.setFirstResult(pageNum * pageSize);
		}
		
		List<EmailAddress> addrList = new ArrayList<>();
		
		List<Tuple> list = sqlQuery.getResultList();
		for (Tuple tuple : list) {
			EmailAddress addr  = tuple.get(0, EmailAddress.class);
			addr.setSentCount(numberToInteger(tuple.get(1)));
			addr.setOpenCount(numberToInteger(tuple.get(2)));
			addr.setClickCount(numberToInteger(tuple.get(3)));
			addrList.add(addr);
		}

		return addrList;
	}

	static List<Predicate> buildPredicates(PagingVo vo, CriteriaBuilder builder, Root<EmailAddress> root) {
		List<Predicate> predicates = new ArrayList<Predicate>();
		// status_id
		if (StringUtils.isNotBlank(vo.getStatusId())) {
			vo.setSearchCriteria(PagingVo.Column.statusId, new PagingVo.Criteria(RuleCriteria.EQUALS, StatusId.ACTIVE.getValue()));
		}
		List<PagingVo.Column> colList = PagingVo.Column.getColumnsByType(PagingVo.Type.emailaddr);
		for (PagingVo.Column col : colList) {
			if (vo.getSearchValue(col) == null) {
				continue;
			}
			PagingVo.Criteria criteria = vo.getSearchCriteria(col);
			predicates.addAll(RepositoryUtils.buildPredicateList(col, criteria, builder, root));
		}
		return predicates;
	}

	private Integer numberToInteger(Object number) {
		/*
		 * The abstract class Number is the superclass of classes BigDecimal,
		 * BigInteger, Byte, Double, Float, Integer, Long, and Short.
		 */
		if (number instanceof Number) {
			return ((Number)number).intValue();
		}
		else if (number != null) {
			throw new RuntimeException("SQL function returned unexpected class type: " + number.getClass().getName());
		}
		return 0;
	}

	/*
	 * Sample address regex patterns
	 * 1) find by domain name - '@test.com$' or '@yahoo.com'
	 * 2) find by email user name - '^myname@' or 'noreply@'
	 */
	@Override
	public List<EmailAddress> findByAddressPattern(String addressPattern) {
		String sql = "select t.* from Email_Address t where t.address REGEXP '" + addressPattern + "' ";
		if (JpaUtil.isPgSQLDatabase()) {
			sql = "select t.* from Email_Address t where t.address ~ '" + addressPattern + "' ";
		}
		else if (JpaUtil.isDerbyDatabase()) {
			String pattern = StringUtils.remove(addressPattern, "^");
			pattern = StringUtils.remove(pattern, "$");
			pattern = StringUtils.removeStart(pattern, "(");
			pattern = StringUtils.removeEnd(pattern, ")");
			sql = "select t.* from Email_Address t " ; //where address like '" + pattern + "' ";
			String whereClause = "";
			StringTokenizer st = new StringTokenizer(pattern, "|");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (addressPattern.startsWith("^")) {
					token = token + "%";
				}
				else if (addressPattern.endsWith("$")) {
					token = "%" + token;
				}
				if (StringUtils.isBlank(whereClause)) {
					whereClause = " where (t.address like '" + token + "') ";
				}
				else {
					whereClause += " or (t.address like '" + token + "') ";
				}
			}
			sql += whereClause;
		}
		try {
			Query query = em.createNativeQuery(sql, EmailAddress.MAPPING_EMAIL_ADDR_ENTITY);
			@SuppressWarnings("unchecked")
			List<EmailAddress> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

}


