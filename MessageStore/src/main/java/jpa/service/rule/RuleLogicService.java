package jpa.service.rule;

import java.util.List;

import jpa.constant.RuleCategory;
import jpa.constant.StatusId;
import jpa.model.rule.RuleLogic;
import jpa.repository.RuleLogicRepository;
import jpa.service.common.ReloadFlagsService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleLogicService")
@Transactional(propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
public class RuleLogicService implements java.io.Serializable {
	private static final long serialVersionUID = 2402907648611630261L;

	static Logger logger = Logger.getLogger(RuleLogicService.class);
	
	//@PersistenceContext(unitName="MessageDB")
	//@Autowired
	//javax.persistence.EntityManager em;
	
	@Autowired
	private RuleLogicRepository repository;
	
	@Autowired
	ReloadFlagsService reloadFlagsService;
	
	public RuleLogic getByRuleName(String ruleName) {
		RuleLogic logic =  repository.findOneByRuleName(ruleName);
		return logic;
	}

	public List<RuleLogic> getAll(boolean builtinRules) {
		
		if (builtinRules == true) {
			return repository.findAllByIsBuiltinRuleTrueAndIsSubruleFalseOrderByEvalSequence();
		}
		else {
			return repository.findAllByIsBuiltinRuleOrderByEvalSequence(builtinRules);
		}
	}
	
	public List<RuleLogic> getActiveRules() {
		return repository.findAllByStatusIdAndStartTimeBeforeOrderByRowId(StatusId.ACTIVE.getValue(),
				new java.sql.Timestamp(System.currentTimeMillis()));
	}

	public List<RuleLogic> getSubrules(boolean excludeBuiltin) {
		if (excludeBuiltin) {
			return repository.findAllByIsSubruleTrueAndIsBuiltinRuleFalse();
		}
		else {
			return repository.findAllByIsSubruleTrue();
		}
	}

	public boolean getHasSubrules(String ruleName) {
		return (repository.findHasSubrules(ruleName) > 0);
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
		reloadFlagsService.updateRuleReloadFlag();
	}

	public int deleteByRuleName(String ruleName) {
		reloadFlagsService.updateRuleReloadFlag();
		int rows = repository.deleteByRuleName(ruleName);
		return rows;
	}

	public int deleteByRowId(int rowId) {
		int rows = repository.deleteByRowId(rowId);
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public void insert(RuleLogic logic) {
		repository.saveAndFlush(logic);
		reloadFlagsService.updateRuleReloadFlag();
	}

	public void update(RuleLogic logic) {
		repository.saveAndFlush(logic);
		reloadFlagsService.updateRuleReloadFlag();
	}

	public List<String> getBuiltinRuleNames4Web() {
		return repository.findBuiltinRuleNames4Web(RuleCategory.MAIN_RULE.getValue());
	}
	
	public List<String> getCustomRuleNames4Web() {
		return repository.findCustomRuleNames4Web(RuleCategory.MAIN_RULE.getValue());
	}

}
