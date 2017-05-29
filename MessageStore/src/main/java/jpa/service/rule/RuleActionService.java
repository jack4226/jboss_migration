package jpa.service.rule;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.model.rule.RuleAction;
import jpa.model.rule.RuleActionPK;
import jpa.repository.RuleActionRepository;
import jpa.service.common.ReloadFlagsService;
import jpa.util.JpaUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleActionService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleActionService implements java.io.Serializable {
	private static final long serialVersionUID = 2659198685298698218L;

	static Logger logger = Logger.getLogger(RuleActionService.class);
	
	@Autowired
	EntityManager em;
	
	@Autowired
	RuleActionRepository repository;
	
	@Autowired
	ReloadFlagsService reloadFlagsService;
	
	public List<RuleAction> getByRuleName(String ruleName) {
		if (JpaUtil.isHibernate()) {
			return repository.findAllByRuleName(ruleName);
		}
		else {
			return repository.findAllByRuleNameNative(ruleName);
		}
	}

	public boolean getHasActions(String ruleName) {
		return (repository.findHasActions(ruleName) > 0);
	}
	
	public List<RuleAction> getByRuleName_v0(String ruleName) {
		return repository.findAllByRuleName_v0(ruleName, Constants.DEFAULT_SENDER_ID);
	}

	public List<RuleAction> getByBestMatch(String ruleName, Timestamp startTime, String senderId) {
		if (startTime == null) {
			startTime = new Timestamp(System.currentTimeMillis());
		}
		if (StringUtils.isBlank(senderId)) {
			senderId = Constants.DEFAULT_SENDER_ID;
		}
		// TODO revisit when SenderDataRowId column is populated on RuleAction table
		return repository.findAllByBestMatch(ruleName, startTime, senderId, StatusId.ACTIVE.getValue());
	}

	public List<RuleAction> getAll() {
		return repository.findAllOrderByActionSequence();
	}
	
	public RuleAction getByPrimaryKey(RuleActionPK pk) {
		if (pk.getRuleLogic()==null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		String senderId = "";
		if (pk.getSenderData()!=null && pk.getSenderData().getSenderId()!=null) {
			senderId = pk.getSenderData().getSenderId();
		}
		return repository.findOneByPrimaryKey(pk.getRuleLogic().getRuleName(), pk.getActionSequence(), senderId, pk.getStartTime());
	}

	public RuleAction getMostCurrent(String ruleName, int actionSequence, String senderId) {
		return repository.findFirstByMostCurrent(ruleName, actionSequence, senderId, StatusId.ACTIVE.getValue());
	}

	public RuleAction getByRowId(int rowId) {
		return repository.findOneByRowId(rowId);
	}
	
	public void delete(RuleAction action) {
		if (action == null) return;
		repository.delete(action);
		reloadFlagsService.updateRuleReloadFlag();
	}

	public int deleteByRuleName(String ruleName) {
		int rows = repository.deleteByRuleName(ruleName);
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public int deleteByPrimaryKey(RuleActionPK pk) {
		if (pk.getRuleLogic()==null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		String senderId = null;
		if (pk.getSenderData()!=null && StringUtils.isNotBlank(pk.getSenderData().getSenderId())) {
			senderId = pk.getSenderData().getSenderId();
		}
		//repository.deleteByPrimaryKey(pk.getRuleLogic().getRuleName(), pk.getActionSequence(), senderId, pk.getStartTime());
		String sql =
				"delete from RuleAction ra where " +
				"ra.ruleActionPK.actionSequence=?1 and ra.ruleActionPK.startTime=?2 " +
				"and ra.ruleActionPK.ruleLogic in (select rl from RuleLogic rl where rl.ruleName=?3) ";
		if (StringUtils.isNotBlank(senderId)) {
			sql += "and ra.ruleActionPK.senderData in (select c from SenderData c where c.senderId=?4)";
			senderId = pk.getSenderData().getSenderId();
		}
		else {
			sql += "and ra.ruleActionPK.senderData is null";
		}
		try {
			Query query = em.createQuery(sql);
			query.setParameter(1, pk.getActionSequence());
			query.setParameter(2, pk.getStartTime());
			query.setParameter(3, pk.getRuleLogic().getRuleName());
			if (StringUtils.isNotBlank(senderId)) {
				query.setParameter(4, senderId);
			}
			reloadFlagsService.updateRuleReloadFlag();
			int rows = query.executeUpdate();
			return rows;
		}
		finally {
		}
	}

	public int deleteByRowId(int rowId) {
		int rows = repository.deleteByRowId(rowId);
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public void insert(RuleAction action) {
		repository.saveAndFlush(action);
		reloadFlagsService.updateRuleReloadFlag();
	}

	public void update(RuleAction action) {
		repository.saveAndFlush(action);
		reloadFlagsService.updateRuleReloadFlag();
	}
}
