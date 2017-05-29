package jpa.service.rule;

import java.util.List;

import jpa.model.rule.RuleSubruleMap;
import jpa.model.rule.RuleSubruleMapPK;
import jpa.repository.RuleSubruleMapRepository;
import jpa.service.common.ReloadFlagsService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleSubruleMapService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleSubruleMapService implements java.io.Serializable {
	private static final long serialVersionUID = -7637699907075271606L;

	static Logger logger = Logger.getLogger(RuleSubruleMapService.class);
	
	//@Autowired
	//javax.persistence.EntityManager em;
	
	@Autowired
	RuleSubruleMapRepository repository;
	
	@Autowired
	private ReloadFlagsService reloadFlagsService;

	public RuleSubruleMap getByPrimaryKey(RuleSubruleMapPK pk) {
		if (pk.getRuleLogic()==null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		if (pk.getSubruleLogic()==null) {
			throw new IllegalArgumentException("A SubruleLogic instance must be provided in Primary Key object.");
		}
		return repository.findOneByPrimaryKey(pk.getRuleLogic().getRuleName(), pk.getSubruleLogic().getRuleName());
	}

	public List<RuleSubruleMap> getByRuleName(String ruleName) {
		return repository.findAllByRuleNameOrderBySubruleSequence(ruleName);
	}
	
	public RuleSubruleMap getByRowId(int rowId) {
		return repository.findOne(rowId);
	}
	
	public void delete(RuleSubruleMap rsmap) {
		if (rsmap == null) return;
		repository.delete(rsmap);
		reloadFlagsService.updateRuleReloadFlag();
	}

	public int deleteByRuleName(String ruleName) {
		int rows = repository.deleteByRuleName(ruleName);
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public int deleteByPrimaryKey(RuleSubruleMapPK pk) {
		if (pk.getRuleLogic()==null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		if (pk.getSubruleLogic()==null) {
			throw new IllegalArgumentException("A SubruleLogic instance must be provided in Primary Key object.");
		}
		int rows = repository.deleteByPrimaryKey(pk.getRuleLogic().getRuleName(), pk.getSubruleLogic().getRuleName());
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public int deleteByRowId(int rowId) {
		int rows = repository.deleteByRowId(rowId);
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public void insert(RuleSubruleMap rsmap) {
		repository.saveAndFlush(rsmap);
		reloadFlagsService.updateRuleReloadFlag();
	}

	public void update(RuleSubruleMap rsmap) {
		repository.saveAndFlush(rsmap);
		reloadFlagsService.updateRuleReloadFlag();
	}
}
