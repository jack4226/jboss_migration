package jpa.repository.msg;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import jpa.data.preload.FolderEnum;
import jpa.model.msg.MessageDeliveryStatus;
import jpa.model.msg.MessageInbox;
import jpa.msgui.vo.PagingVo;
import jpa.msgui.vo.SearchFieldsVo;
import jpa.repository.RepositoryUtils;

public class MessageInboxRepositoryImpl implements MessageInboxRepositoryCustom, java.io.Serializable {
	private static final long serialVersionUID = -5410307771103632876L;

	static Logger logger = LogManager.getLogger(MessageInboxRepositoryImpl.class);
	
	@Autowired
	private EntityManager em;
	
	/*
	 * XXX - NOT WORKING under Hibernate with following error:
	 * 
	 * org.springframework.dao.InvalidDataAccessApiUsageException:
	 * org.hibernate.loader.MultipleBagFetchException: cannot simultaneously
	 * fetch multiple bags:
	 * 
	 * [jpa.model.msg.MessageInbox.messageHeaderList,
	 * jpa.model.msg.MessageInbox.messageAddressList,
	 * jpa.model.msg.MessageInbox.messageRfcFieldList,
	 * jpa.model.msg.MessageInbox.messageAttachmentList,
	 * jpa.model.msg.MessageInbox.messageActionLogList,
	 * jpa.model.msg.MessageInbox.messageDeliveryStatusList];
	 * 
	 * nested exception is java.lang.IllegalArgumentException:
	 * org.hibernate.loader.MultipleBagFetchException: cannot simultaneously
	 * fetch multiple bags: [jpa.model.msg.MessageInbox.messageHeaderList,
	 * jpa.model.msg.MessageInbox.messageAddressList,
	 * jpa.model.msg.MessageInbox.messageRfcFieldList,
	 * jpa.model.msg.MessageInbox.messageAttachmentList,
	 * jpa.model.msg.MessageInbox.messageActionLogList,
	 * jpa.model.msg.MessageInbox.messageDeliveryStatusList]
	 *
	 */
	@Override
	public MessageInbox findOneByRowIdWithAllData(Integer rowId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<MessageInbox> query = builder.createQuery(MessageInbox.class);
	    Root<MessageInbox> root = query.from(MessageInbox.class);
		
	    Predicate predicatre = builder.equal(root.get("rowId"), rowId);
	    query.where(predicatre);
	    
//	    List<Selection<?>> selectionList = new ArrayList<Selection<?>>();
//	    selectionList.add(root);
//	    Join<?,?> joinHdr = root.join("messageHeaderList", JoinType.LEFT);
//	    Join<?,?> joinAddr = root.join("messageAddressList", JoinType.LEFT);
//	    Join<?,?> joinStream = root.join("messageStream", JoinType.LEFT);
//	    Join<?,?> joinRfc = root.join("messageRfcFieldList", JoinType.LEFT);
//	    Join<?,?> joinAtch = root.join("messageAttachmentList", JoinType.LEFT);
//	    Join<?,?> joinUnsub = root.join("messageUnsubComment", JoinType.LEFT);
//	    Join<?,?> joinAct = root.join("messageActionLogList", JoinType.LEFT);
//	    Join<?,?> joinDlvr = root.join("messageDeliveryStatusList", JoinType.LEFT);
//	    selectionList.add(joinHdr);
//	    selectionList.add(joinAddr);
//	    selectionList.add(joinStream);
//	    selectionList.add(joinRfc);
//	    selectionList.add(joinAtch);
//	    selectionList.add(joinUnsub);
//	    selectionList.add(joinAct);
//	    selectionList.add(joinDlvr);
//	    query.multiselect(selectionList);
	    
	    query.select(root);
	    
	    root.fetch("messageHeaderList", JoinType.LEFT);
	    root.fetch("messageAddressList", JoinType.LEFT);
	    root.fetch("messageStream", JoinType.LEFT);
	    root.fetch("messageRfcFieldList", JoinType.LEFT);
	    root.fetch("messageAttachmentList", JoinType.LEFT);
	    root.fetch("messageUnsubComment", JoinType.LEFT);
	    root.fetch("messageActionLogList", JoinType.LEFT);
	    root.fetch("messageDeliveryStatusList", JoinType.LEFT);
	    
	    TypedQuery<MessageInbox> sqlQuery = em.createQuery(query);
	    sqlQuery.setMaxResults(1);
	    try {
		    MessageInbox result = sqlQuery.getSingleResult();
	    	return result;
	    }
	    catch (NoResultException | EmptyResultDataAccessException  e) {
	    	return null;
	    }
	}
	
	@Override
	public Long findRowCountForWeb(SearchFieldsVo vo) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<Long> query = builder.createQuery(Long.class);
	    Root<MessageInbox> root = query.from(MessageInbox.class);
	    
	    List<Predicate> predicates = buildAllPredicates(builder, root, vo.getPagingVo());
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
	
	@Override
	public List<MessageInbox> findListForWeb(SearchFieldsVo vo, long rowCount) {
		int pageNumber = vo.getPagingVo().getPageNumber();
		int pageSize = vo.getPagingVo().getPageSize();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<MessageInbox> query = builder.createQuery(MessageInbox.class);
	    Root<MessageInbox> root = query.from(MessageInbox.class);

		List<Predicate> predicates = buildAllPredicates(builder, root, vo.getPagingVo());
		
		query.where(builder.and(predicates.toArray(new Predicate[] {})));

		List<Order> orderByList = RepositoryUtils.buildOrderByList(vo.getPagingVo(), builder, root);
		query.orderBy(orderByList);
		
		root.fetch("fromAddress", JoinType.LEFT);
		root.fetch("toAddress", JoinType.LEFT);
		root.fetch("senderData", JoinType.LEFT);
		root.fetch("subscriberData", JoinType.LEFT);
		root.fetch("ruleLogic", JoinType.LEFT);
		root.fetch("messageFolder", JoinType.LEFT);
		root.fetch("messageStream", JoinType.LEFT);
		root.fetch("messageUnsubComment", JoinType.LEFT);
		
		TypedQuery<MessageInbox> sqlQuery = em.createQuery(query);
		
		if (pageSize > 0) {
			sqlQuery.setMaxResults(pageSize);
			sqlQuery.setFirstResult(pageNumber * pageSize);
		}
		return sqlQuery.getResultList();
	}
	
	private List<Predicate> buildAllPredicates(CriteriaBuilder builder, Root<MessageInbox> root, PagingVo vo) {
	    Join<?,?> joinFromAddr = root.join("fromAddress", JoinType.INNER);
	    Join<?,?> joinToAddr = root.join("toAddress", JoinType.INNER);
	    Join<?,?> joinRule = root.join("ruleLogic", JoinType.LEFT);
	    
		List<Predicate> predicates = RepositoryUtils.buildPredicatesList(vo, PagingVo.Type.msginbox, builder, root);
		
		/*
		 * Build the rest of the search predicates
		 */
		// ruleName
		Object ruleName = vo.getSearchValue(PagingVo.Column.ruleName);
		if (StringUtils.isNotBlank((String) ruleName)) {
			if (!SearchFieldsVo.RuleName.All.name().equals((String) ruleName)) {
				PagingVo.Criteria criteria = vo.getSearchCriteria(PagingVo.Column.ruleName);
				predicates.addAll(RepositoryUtils.buildPredicateList(PagingVo.Column.ruleName, criteria, builder, joinRule));
			}
		}
		// fromAddress RowId
		Object fromAddrId = vo.getSearchValue(PagingVo.Column.fromAddrId);
		if (fromAddrId != null) {
			predicates.add(builder.equal(joinFromAddr.<Integer>get("rowId"), (Integer) fromAddrId));
		}
		// toAddress RowId
		Object toAddrId = vo.getSearchValue(PagingVo.Column.toAddrId);
		if (toAddrId != null) {
			predicates.add(builder.equal(joinToAddr.<Integer>get("rowId"), (Integer) toAddrId));
		}
		// from address
		Object fromAddr = vo.getSearchValue(PagingVo.Column.fromAddr);
		if (StringUtils.isNotBlank((String) fromAddr) && fromAddrId == null) {
			predicates.add(builder.equal(joinFromAddr.<Integer>get("address"), fromAddr));
		}
		// to address
		Object toAddr = vo.getSearchValue(PagingVo.Column.toAddr);
		if (StringUtils.isNotBlank((String) toAddr) && toAddrId == null) {
			predicates.add(builder.equal(joinToAddr.<Integer>get("address"), toAddr));
		}
		// message folder
		String msgFolder = (String) vo.getSearchValue(PagingVo.Column.messageFolder);
		if (StringUtils.isNotBlank(msgFolder) && !FolderEnum.All.name().equals(msgFolder)) {
			Join<?,?> joinFolder = root.join("messageFolder", JoinType.INNER);
			predicates.add(builder.equal(joinFolder.get("folderName"), msgFolder));
		}
		
		return predicates;
	}
	
	@Override
	public List<MessageInbox> findAllByNotExistsDeliveryStatus(PagingVo vo) {
		int pageNumber = vo.getPageNumber();
		int pageSize = vo.getPageSize();
		
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<MessageInbox> query = builder.createQuery(MessageInbox.class);
	    Root<MessageInbox> root = query.from(MessageInbox.class);
	    query.select(root);
	    
	    Subquery<MessageDeliveryStatus> subQuery = query.subquery(MessageDeliveryStatus.class);
	    Root<MessageDeliveryStatus> subRoot = subQuery.from(MessageDeliveryStatus.class);
	    subQuery.select(subRoot);
	    
	    subQuery.where(builder.equal(subRoot.get("messageDeliveryStatusPK").get("messageInbox"), root));
	    
	    List<Predicate> predicates = new ArrayList<Predicate>(); 
	    predicates.add(builder.not(builder.exists(subQuery)));
	    
	    query.where(builder.and(predicates.toArray(new Predicate[] {})));

		List<Order> orderByList = RepositoryUtils.buildOrderByList(vo, builder, root);
		query.orderBy(orderByList);
		
		TypedQuery<MessageInbox> sqlQuery = em.createQuery(query);
	    if (pageSize > 0) {
			sqlQuery.setMaxResults(pageSize);
			sqlQuery.setFirstResult(pageNumber * pageSize);
		}
		return sqlQuery.getResultList();
	}
}
