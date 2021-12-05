package jpa.service.rule;

import java.util.List;
import java.util.Optional;

import jpa.model.rule.RuleDataType;
import jpa.repository.RuleDataTypeRepository;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleDataTypeService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataTypeService implements java.io.Serializable {
	private static final long serialVersionUID = 7713274333671397066L;

	static Logger logger = LogManager.getLogger(RuleDataTypeService.class);
	
	@Autowired
	RuleDataTypeRepository repository;
	
	public RuleDataType getByDataType(String dataType) {
		return repository.findOneByDataType(dataType);
	}
	
	public Optional<RuleDataType> getByRowId(int rowId) {
		return repository.findById(rowId);
	}
	
	public List<RuleDataType> getAll() {
		return repository.findAll();
	}
	
	public void delete(RuleDataType dataName) {
		if (dataName==null) return;
		repository.delete(dataName);;
	}

	public int deleteByDataType(String dataType) {
		return repository.deleteByDataType(dataType);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public void insert(RuleDataType dataName) {
		repository.saveAndFlush(dataName);
	}
	
	public void update(RuleDataType dataName) {
		repository.saveAndFlush(dataName);
	}
	
	public List<String> getDataTypeList() {
		return repository.findAllDataTypes();
	}
	
}
