package jpa.repository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.model.rule.RuleAction;

public class RuleActionRepositoryImpl implements RuleActionRepositoryCustom, java.io.Serializable {
	private static final long serialVersionUID = -2496994919462279218L;

	static Logger logger = Logger.getLogger(RuleActionRepositoryImpl.class);
	
	@Autowired
	private EntityManager em;

	@Override
	public RuleAction findOneByPrimaryKey(String ruleName, Integer actionSequence, String senderId,
			Timestamp startTime) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<RuleAction> query = builder.createQuery(RuleAction.class);
	    Root<RuleAction> root = query.from(RuleAction.class);
	    query.select(root);
	    List<Predicate> predicates = new ArrayList<Predicate>();
	    Join<?,?> joinPK = root.join("ruleActionPK");
	    Join<?,?> joinRuleLogic = joinPK.join("ruleLogic", JoinType.INNER);
	    Join<?,?> joinSender = joinPK.join("senderData", JoinType.LEFT);
	    
	    predicates.add(builder.equal(joinRuleLogic.get("ruleName"), ruleName));
	    predicates.add(builder.equal(joinPK.get("actionSequence"), actionSequence));
	    predicates.add(builder.lessThanOrEqualTo(joinPK.<java.sql.Timestamp>get("startTime"), startTime));
	    predicates.add(builder.equal(joinSender.get("senderId"), senderId));
	    
	    query.where(builder.and(predicates.toArray(new Predicate[] {})));
	    
	    List<Order> orderByList = new ArrayList<Order>();
	    orderByList.add(builder.asc(joinPK.get("actionSequence")));
	    orderByList.add(builder.desc(joinSender.get("senderId")));
	    orderByList.add(builder.desc(joinPK.get("startTime")));
	    query.orderBy(orderByList);
	    
	    TypedQuery<RuleAction> sqlQuery = em.createQuery(query);
	    sqlQuery.setFirstResult(0);
	    sqlQuery.setMaxResults(1);
	    try {
	    	RuleAction result = sqlQuery.getSingleResult();
	    	return result;
	    }
	    catch (NoResultException e) {
	    	return null;
	    }
	}

	@Override
	public RuleAction findFirstByMostCurrent(String ruleName, Integer actionSequence, String senderId,
			String statusId) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
	    CriteriaQuery<RuleAction> query = builder.createQuery(RuleAction.class);
	    Root<RuleAction> root = query.from(RuleAction.class);
	    query.select(root);
	    List<Predicate> predicates = new ArrayList<Predicate>();
	    Join<?,?> joinPK = root.join("ruleActionPK");
	    Join<?,?> joinRuleLogic = joinPK.join("ruleLogic", JoinType.INNER);
	    Join<?,?> joinSender = joinPK.join("senderData", JoinType.LEFT);
	    
	    predicates.add(builder.equal(joinRuleLogic.get("ruleName"), ruleName));
	    predicates.add(builder.equal(joinPK.get("actionSequence"), actionSequence));
	    predicates.add(builder.equal(root.get("statusId"), statusId));
	    predicates.add(builder.equal(joinSender.get("senderId"), senderId));
	    
	    query.where(builder.and(predicates.toArray(new Predicate[] {})));
	    
	    List<Order> orderByList = new ArrayList<Order>();
	    orderByList.add(builder.desc(joinSender.get("senderId")));
	    orderByList.add(builder.desc(joinPK.get("startTime")));
	    query.orderBy(orderByList);
	    
	    TypedQuery<RuleAction> sqlQuery = em.createQuery(query);
	    sqlQuery.setFirstResult(0);
	    sqlQuery.setMaxResults(1);
	    try {
	    	RuleAction result = sqlQuery.getSingleResult();
	    	return result;
	    }
	    catch (NoResultException e) {
	    	return null;
	    }

	}

	/*
	 * XXX - NOT WORKING - with this error:
	 * UPDATE/DELETE criteria queries cannot define joins
	 */
	@Override
	public int deleteByPrimaryKey(String ruleName, Integer actionSequence, String senderId,
			Timestamp startTime) {
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaDelete<RuleAction> query = builder.createCriteriaDelete(RuleAction.class);
		Root<RuleAction> root = query.from(RuleAction.class);
		
	    List<Predicate> predicates = new ArrayList<Predicate>();
	    
	    Join<?,?> joinPK = root.join("ruleActionPK");
	    Join<?,?> joinRuleLogic = joinPK.join("ruleLogic", JoinType.INNER);
	    
	    predicates.add(builder.equal(joinRuleLogic.get("ruleName"), ruleName));
	    predicates.add(builder.equal(joinPK.get("actionSequence"), actionSequence));
	    predicates.add(builder.lessThanOrEqualTo(joinPK.<java.sql.Timestamp>get("startTime"), startTime));
	    if (StringUtils.isNotBlank(senderId)) {
		    Join<?,?> joinSender = joinPK.join("senderData", JoinType.LEFT);
		    predicates.add(builder.equal(joinSender.get("senderId"), senderId));
	    }
	    
	    Query sqlQuery = em.createQuery(query);
	    int rows = sqlQuery.executeUpdate();
		return rows;
	}
}
