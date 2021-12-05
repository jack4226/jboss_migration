package jpa.service.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.GlobalVariable;
import jpa.model.GlobalVariablePK;
import jpa.repository.GlobalVariableRepository;

@Component("globalVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class GlobalVariableService implements java.io.Serializable {
	private static final long serialVersionUID = 6628495287347386534L;

	static Logger logger = Logger.getLogger(GlobalVariableService.class);
	
	@Autowired
	GlobalVariableRepository repository;

	public Optional<GlobalVariable> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	/**
	 * find the record by variable name and start time.
	 * @param variableName
	 * @param startTime
	 * @return record found or null
	 */
	public GlobalVariable getByPrimaryKey(GlobalVariablePK pk) {
		if (pk.getStartTime() != null) {
			return repository.findOneByGlobalVariablePK_VariableNameAndGlobalVariablePK_StartTime(pk.getVariableName(), pk.getStartTime());
		}
		else {
			return repository.findOneByGlobalVariablePK_VariableNameAndGlobalVariablePK_StartTimeIsNull(pk.getVariableName());
		}
	}

	/**
	 * find the best matched record by variable name and start time.
	 * @param variableName
	 * @param startTime
	 * @return the record best matched or null if not found
	 */
	public GlobalVariable getByBestMatch(GlobalVariablePK pk) {
		if (pk.getStartTime()==null) {
			pk.setStartTime(new java.sql.Timestamp(System.currentTimeMillis()));
		}
		List<GlobalVariable> list = repository.findAllByBestMatch(pk.getVariableName(), pk.getStartTime());
		if (!list.isEmpty()) {
			GlobalVariable record = list.get(0);
			return record;
		}
		return null;
	}
	
	public List<GlobalVariable> getByVariableName(String variableName) {
		return repository.findByGlobalVariablePK_VariableNameOrderByGlobalVariablePK_StartTime(variableName);
	}

	public List<GlobalVariable> getCurrent() {
		return repository.findAllCurrentVariables(StatusId.ACTIVE.getValue(), new java.sql.Timestamp(System.currentTimeMillis()));
	}

	public List<GlobalVariable> getByStatusId(String statusId) {
		List<GlobalVariable> list = repository.findAllByStatusIdAndStartTime(statusId,  new java.sql.Timestamp(System.currentTimeMillis()));
		List<GlobalVariable> list2 = new ArrayList<GlobalVariable>();
		String varName = null;
		// remove duplicates, the list is sorted by variable name
		for (GlobalVariable var : list) {
			if (!var.getGlobalVariablePK().getVariableName().equals(varName)) {
				list2.add(var);
				varName = var.getGlobalVariablePK().getVariableName();
			}
		}
		return list2;
	}

	public void delete(GlobalVariable var) {
		if (var == null) return;
		repository.delete(var);
	}

	public int deleteByPrimaryKey(GlobalVariablePK pk) {
		return repository.deleteByGlobalVariablePK_VariableNameAndGlobalVariablePK_StartTime(pk.getVariableName(), pk.getStartTime());
	}

	public int deleteByVariableName(String variableName) {
		return repository.deleteByGlobalVariablePK_VariableName(variableName);
	}

	public void update(GlobalVariable var) {
		if (var == null) return;
		repository.saveAndFlush(var);
	}

	public void insert(GlobalVariable var) {
		if (var == null) return;
		repository.saveAndFlush(var);
	}

}
