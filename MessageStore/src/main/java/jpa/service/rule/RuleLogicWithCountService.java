package jpa.service.rule;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import jpa.constant.StatusId;
import jpa.model.rule.RuleLogic;
import jpa.repository.RuleLogicRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleLogicWithCountService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleLogicWithCountService implements java.io.Serializable {
	private static final long serialVersionUID = 3967668920310431589L;

	static Logger logger = LogManager.getLogger(RuleLogicService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	RuleLogicRepository repository;
	
	/*
	 * Returns an array of following elements:
	 * 1) RuleLogic
	 * 2) BigDecimal for sub-rule count
	 */
	public Object[] getByRuleName(String ruleName) {
		String sql = 
				"select r.row_id, "
					+ "r.status_id, "
					+ "r.updt_time, "
					+ "r.updt_user_id, "
					+ "r.Description, "
					+ "r.EvalSequence, "
					+ "r.IsBuiltinRule, "
					+ "r.IsSubrule, "
					+ "r.MailType, "
					+ "r.RuleCategory, "
					+ "r.RuleName, "
					+ "r.RuleType, "
					+ "r.StartTime, "
					+ "count(s.RuleLogicRowId) as subruleCount " +
				" from Rule_Logic r " +
					" left outer join Rule_SubRule_Map s on r.row_id=s.RuleLogicRowId " +
				" where r.ruleName=?1 " +
				" group by r.row_id, "
					+ "r.status_id, "
					+ "r.updt_time, "
					+ "r.updt_user_id, "
					+ "r.Description, "
					+ "r.EvalSequence, "
					+ "r.IsBuiltinRule, "
					+ "r.IsSubrule, "
					+ "r.MailType, "
					+ "r.RuleCategory, "
					+ "r.RuleName, "
					+ "r.RuleType, "
					+ "r.StartTime";
		try {
			Query query = em.createNativeQuery(sql, RuleLogic.MAPPING_RULE_LOGIC_WITH_COUNT);
			query.setParameter(1, ruleName);
			Object[] logic = (Object[]) query.getSingleResult();
			return logic;
		}
		finally {
		}
	}

	public List<Object[]> getByActiveRules() {
		String sql = 
				"select r.row_id, "
						+ "r.status_id, "
						+ "r.updt_time, "
						+ "r.updt_user_id, "
						+ "r.Description, "
						+ "r.EvalSequence, "
						+ "r.IsBuiltinRule, "
						+ "r.IsSubrule, "
						+ "r.MailType, "
						+ "r.RuleCategory, "
						+ "r.RuleName, "
						+ "r.RuleType, "
						+ "r.StartTime, "
						+ "count(s.RuleLogicRowId) as subruleCount " +
				" from Rule_Logic r " +
					" left outer join Rule_SubRule_Map s on r.row_id=s.RuleLogicRowId " +
				" where r.status_id=?1 and r.startTime<=?2 " +
				" group by r.row_id, "
					+ "r.status_id, "
					+ "r.updt_time, "
					+ "r.updt_user_id, "
					+ "r.Description, "
					+ "r.EvalSequence, "
					+ "r.IsBuiltinRule, "
					+ "r.IsSubrule, "
					+ "r.MailType, "
					+ "r.RuleCategory, "
					+ "r.RuleName, "
					+ "r.RuleType, "
					+ "r.StartTime " +
				" order by r.EvalSequence ";
		try {
			Query query = em.createNativeQuery(sql, RuleLogic.MAPPING_RULE_LOGIC_WITH_COUNT);
			query.setParameter(1, StatusId.ACTIVE.getValue());
			query.setParameter(2, new java.sql.Timestamp(System.currentTimeMillis()));
			@SuppressWarnings("unchecked")
			List<Object[]> list = query.getResultList();
			return list;
		}
		finally {
		}
	}

	public List<Object[]> getAll(boolean builtinRules) {
		String sql = 
				"select r.row_id, "
						+ "r.status_id, "
						+ "r.updt_time, "
						+ "r.updt_user_id, "
						+ "r.Description, "
						+ "r.EvalSequence, "
						+ "r.IsBuiltinRule, "
						+ "r.IsSubrule, "
						+ "r.MailType, "
						+ "r.RuleCategory, "
						+ "r.RuleName, "
						+ "r.RuleType, "
						+ "r.StartTime, "
					+ "count(s.RuleLogicRowId) as subruleCount " +
				" from Rule_Logic r " +
					" left outer join Rule_SubRule_Map s on r.row_id=s.RuleLogicRowId " +
				" where r.IsBuiltinRule=?1 " +
				" group by r.row_id, "
					+ "r.status_id, "
					+ "r.updt_time, "
					+ "r.updt_user_id, "
					+ "r.Description, "
					+ "r.EvalSequence, "
					+ "r.IsBuiltinRule, "
					+ "r.IsSubrule, "
					+ "r.MailType, "
					+ "r.RuleCategory, "
					+ "r.RuleName, "
					+ "r.RuleType, "
					+ "r.StartTime " +
				" order by r.EvalSequence ";
		try {
			Query query = em.createNativeQuery(sql, RuleLogic.MAPPING_RULE_LOGIC_WITH_COUNT);
			query.setParameter(1, builtinRules);
			@SuppressWarnings("unchecked")
			List<Object[]> list = query.getResultList();
			return list;
		}
		finally {
		}
	}
	
	public RuleLogic getByRowId(int rowId) {
		return repository.findOneByRowId(rowId);
	}
	
	public int getNextEvalSequence() {
		return repository.findNextEvalSequence();
	}

	public void delete(RuleLogic logic) {
		if (logic == null) return;
		repository.delete(logic);
	}

	public int deleteByRuleName(String ruleName) {
		return repository.deleteByRuleName(ruleName);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(RuleLogic logic) {
		if (logic == null) return;
		repository.saveAndFlush(logic);
	}

	public void update(RuleLogic logic) {
		if (logic == null) return;
		repository.saveAndFlush(logic);
	}

}
