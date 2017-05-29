package jpa.service.rule;

import java.util.List;

import jpa.model.rule.RuleElement;
import jpa.model.rule.RuleElementPK;
import jpa.repository.RuleElementRepository;
import jpa.service.common.ReloadFlagsService;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleElementService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleElementService implements java.io.Serializable {
	private static final long serialVersionUID = 627370172349152701L;

	static Logger logger = Logger.getLogger(RuleElementService.class);
	
	//@Autowired
	//javax.persistence.EntityManager em;
	
	@Autowired
	private ReloadFlagsService reloadFlagsService;
	
	@Autowired
	RuleElementRepository repository;

	public RuleElement getByPrimaryKey(RuleElementPK pk) {
		if (pk.getRuleLogic() == null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		return repository.findOneByRuleElementPK_RuleLogic_RuleNameAndRuleElementPK_ElementSequence(
				pk.getRuleLogic().getRuleName(), pk.getElementSequence());
	}

	public List<RuleElement> getByRuleName(String ruleName) {
		return repository.findAllByRuleElementPK_RuleLogic_RuleNameOrderByRuleElementPK_ElementSequence(ruleName);
	}
	
	public RuleElement getByRowId(int rowId) {
		return repository.findOne(rowId);
	}
	
	public List<RuleElement> getAll() {
		return repository.findAllByOrderByRuleElementPK_RuleLogic_RuleNameAscRuleElementPK_ElementSequence();
	}
	
	public void delete(RuleElement element) {
		if (element == null) return;
		repository.delete(element);
	}

	public int deleteByRuleName(String ruleName) {
		int rows = repository.deleteElementsByRuleName(ruleName);
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public int deleteByPrimaryKey(RuleElementPK pk) {
		if (pk.getRuleLogic() == null) {
			throw new IllegalArgumentException("A RuleLogic instance must be provided in Primary Key object.");
		}
		int rows = repository.deleteElementByPrimaryKey(pk.getRuleLogic().getRuleName(), pk.getElementSequence());
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public int deleteByRowId(int rowId) {
		int rows = repository.deleteByRowId(rowId);
		reloadFlagsService.updateRuleReloadFlag();
		return rows;
	}

	public void insert(RuleElement element) {
		repository.saveAndFlush(element);
		reloadFlagsService.updateRuleReloadFlag();
	}

	public void update(RuleElement element) {
		repository.saveAndFlush(element);
		reloadFlagsService.updateRuleReloadFlag();
	}
}
