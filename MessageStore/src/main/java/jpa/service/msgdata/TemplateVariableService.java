package jpa.service.msgdata;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.msg.TemplateVariable;
import jpa.model.msg.TemplateVariablePK;
import jpa.repository.msg.TemplateVariableRepository;

@Component("templateVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class TemplateVariableService implements java.io.Serializable {
	private static final long serialVersionUID = 9148726924654542371L;

	static Logger logger = Logger.getLogger(TemplateVariableService.class);
	
	@Autowired
	TemplateVariableRepository repository;
	
	public Optional<TemplateVariable> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public TemplateVariable getByPrimaryKey(TemplateVariablePK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()!=null) {
			return repository.findOneByPrimaryKey(pk.getSenderData().getSenderId(), pk.getVariableId(), pk.getVariableName(), pk.getStartTime());
		}
		else {
			return repository.findOneByPrimaryKey(pk.getSenderData().getSenderId(), pk.getVariableId(), pk.getVariableName());
		}
	}

	public TemplateVariable getByBestMatch(TemplateVariablePK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()==null) {
			pk.setStartTime(new Timestamp(System.currentTimeMillis()));
		}
		List<TemplateVariable> list = repository.findAllByBestMatch(
				pk.getSenderData().getSenderId(), pk.getVariableId(), pk.getVariableName(), pk.getStartTime());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public List<TemplateVariable> getByVariableId(String variableId) {
		return repository.findAllByVariableId(variableId);
	}

	public List<TemplateVariable> getCurrentByVariableId(String variableId) {
		return repository.findCurrentByVariableId(StatusId.ACTIVE.getValue(), new Timestamp(System.currentTimeMillis()), variableId);
	}

	public List<TemplateVariable> getCurrentBySenderId(String senderId) {
		return repository.findCurrentBySenderId(StatusId.ACTIVE.getValue(), new Timestamp(System.currentTimeMillis()), senderId);
	}

	public void delete(TemplateVariable variable) {
		if (variable == null) return;
		repository.delete(variable);;
	}

	public int deleteByPrimaryKey(TemplateVariablePK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getVariableId(), pk.getVariableName(), pk.getStartTime(), pk.getSenderData().getSenderId());
	}

	public int deleteByVariableId(String variableId) {
		return repository.deleteByVariableId(variableId);
	}

	public int deleteByVariableName(String variableName) {
		return repository.deleteByVariableName(variableName);
	}

	public int deleteBySenderId(String senderId) {
		return repository.deleteBySenderId(senderId);
	}

	public void update(TemplateVariable variable) {
		repository.saveAndFlush(variable);
	}

	public void insert(TemplateVariable variable) {
		repository.saveAndFlush(variable);
	}

}
