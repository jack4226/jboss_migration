package jpa.service.rule;

import java.util.List;
import java.util.Optional;

import jpa.model.rule.RuleActionDetail;
import jpa.repository.RuleActionDetailRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleActionDetailService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleActionDetailService implements java.io.Serializable {
	private static final long serialVersionUID = -4107091257607270429L;

	static Logger logger = LogManager.getLogger(RuleActionDetailService.class);
	
	@Autowired
	RuleActionDetailRepository repository;

	public RuleActionDetail getByActionId(String actionId) {
		return repository.findOneByActionId(actionId);
	}
	
	public Optional<RuleActionDetail> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<RuleActionDetail> getAll() {
		return repository.findAll();
	}
	
	public void delete(RuleActionDetail detail) {
		if (detail==null) return;
		repository.delete(detail);;
	}

	public int deleteByActionId(String actionId) {
		return repository.deleteByActionId(actionId);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(RuleActionDetail detail) {
		repository.saveAndFlush(detail);
	}
	
	public void update(RuleActionDetail detail) {
		repository.saveAndFlush(detail);
	}
	
	public List<String> getActionIdList() {
		return repository.findAllActionIds();
	}

}
