package jpa.service.common;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.constant.StatusId;
import jpa.model.SenderVariable;
import jpa.model.SenderVariablePK;
import jpa.repository.SenderVariableRepository;

@Component("senderVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class SenderVariableService implements java.io.Serializable {
	private static final long serialVersionUID = -2936730931671540437L;

	static Logger logger = Logger.getLogger(SenderVariableService.class);
	
	@Autowired
	SenderVariableRepository repository;

	public SenderVariable getByRowId(int rowId) {
		return repository.findOne(rowId);
	}

	public SenderVariable getByPrimaryKey(SenderVariablePK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime() != null) {
			return repository
					.findOneBySenderVariablePK_VariableNameAndSenderVariablePK_SenderData_SenderIdAndSenderVariablePK_StartTime(
							pk.getVariableName(), pk.getSenderData().getSenderId(), pk.getStartTime());
		}
		else {
			return repository
					.findOneBySenderVariablePK_VariableNameAndSenderVariablePK_SenderData_SenderIdAndSenderVariablePK_StartTimeIsNull(
							pk.getVariableName(), pk.getSenderData().getSenderId());
		}
	}

	public SenderVariable getByBestMatch(SenderVariablePK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		if (pk.getStartTime()==null) {
			pk.setStartTime(new Timestamp(System.currentTimeMillis()));
		}
		List<SenderVariable> list = repository.findAllByBestMatchOrderByStartTimeDesc(pk.getVariableName(),
				pk.getSenderData().getSenderId(), pk.getStartTime());
		if (!list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

	public List<SenderVariable> getByVariableName(String variableName) {
		return repository
				.findAllBySenderVariablePK_VariableNameOrderBySenderVariablePK_SenderData_SenderIdAscSenderVariablePK_StartTime(
						variableName);
	}

	public List<SenderVariable> getCurrentBySenderId(String senderId) {
		return repository.findCurrentBySenderIdOrderByRowId(StatusId.ACTIVE.getValue(),
				new Timestamp(System.currentTimeMillis()), senderId);
	}

	public void delete(SenderVariable var) {
		if (var == null) return;
		repository.delete(var);
	}

	public int deleteByPrimaryKey(SenderVariablePK pk) {
		if (pk.getSenderData()==null) {
			throw new IllegalArgumentException("A SenderData instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getVariableName(), pk.getStartTime(), pk.getSenderData().getSenderId());
	}

	public int deleteByVariableName(String variableName) {
		return repository.deleteBySenderVariablePK_VariableName(variableName);
	}

	public int deleteBySenderId(String senderId) {
		return repository.deleteBySenderVariablePK_SenderData_SenderId(senderId);
	}

	public void update(SenderVariable var) {
		if (var == null) return;
		repository.saveAndFlush(var);
	}

	public void insert(SenderVariable var) {
		if (var == null) return;
		repository.saveAndFlush(var);
	}

}
