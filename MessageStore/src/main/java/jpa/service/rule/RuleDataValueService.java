package jpa.service.rule;

import java.util.List;

import jpa.model.rule.RuleDataValue;
import jpa.model.rule.RuleDataValuePK;
import jpa.repository.RuleDataValueRepository;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleDataValueService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataValueService implements java.io.Serializable {
	private static final long serialVersionUID = -4091890320222391000L;

	static Logger logger = Logger.getLogger(RuleDataValueService.class);
	
	@Autowired
	RuleDataValueRepository repository;

	public RuleDataValue getByPrimaryKey(RuleDataValuePK pk) {
		if (pk.getRuleDataType()==null) {
			throw new IllegalArgumentException("A RuleDataType instance must be provided in Primary Key object.");
		}
		return repository.findOneByPrimaryKey(pk.getRuleDataType().getDataType(), pk.getDataValue());
	}
	
	public RuleDataValue getByRowId(int rowId) {
		return repository.findOne(rowId);
	}
	
	public List<RuleDataValue> getByDataType(String dataType) {
		return repository.findAllByDataType(dataType);
	}
	
	public List<RuleDataValue> getAll() {
		return repository.findAll();
	}
	
	public void delete(RuleDataValue dataType) {
		if (dataType==null) return;
		repository.delete(dataType);
	}

	public int deleteByPrimaryKey(RuleDataValuePK pk) {
		if (pk.getRuleDataType()==null) {
			throw new IllegalArgumentException("A RuleDataType instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimarykey(pk.getRuleDataType().getDataType(), pk.getDataValue());
	}

	public int deleteByDataType(String dataType) {
		return repository.deleteByDataType(dataType);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(RuleDataValue dataType) {
		repository.saveAndFlush(dataType);
	}
	
	public void update(RuleDataValue dataType) {
		repository.saveAndFlush(dataType);
	}
	
}
