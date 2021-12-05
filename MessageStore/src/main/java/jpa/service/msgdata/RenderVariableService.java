package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.RenderVariable;
import jpa.model.msg.RenderVariablePK;
import jpa.repository.msg.RenderVariableRepository;

@Component("renderVariableService")
@Transactional(propagation=Propagation.REQUIRED)
public class RenderVariableService implements java.io.Serializable {
	private static final long serialVersionUID = -6918416726105688507L;

	static Logger logger = Logger.getLogger(RenderVariableService.class);
	
	@Autowired
	RenderVariableRepository repository;

	public Optional<RenderVariable> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public RenderVariable getByPrimaryKey(RenderVariablePK pk) {
		if (pk.getMessageRendered()==null) {
			throw new IllegalArgumentException("A MessageRendered instance must be provided in Primary Key object.");
		}
		return repository.findOneByPrimaryKey(pk.getMessageRendered().getRowId(), pk.getVariableName());
	}

	public List<RenderVariable> getByRenderId(int renderId) {
		return repository.findAllByRenderVariablePK_MessageRendered_RowId(renderId);
	}

	public void delete(RenderVariable variable) {
		if (variable == null) return;
		repository.delete(variable);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByPrimaryKey(RenderVariablePK pk) {
		if (pk.getMessageRendered()==null) {
			throw new IllegalArgumentException("A MessageRendered instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getMessageRendered().getRowId(), pk.getVariableName());
	}

	public int deleteByRenderId(int renderId) {
		return repository.deleteByMessageRenderedRowId(renderId);
	}

	public void update(RenderVariable variable) {
		repository.saveAndFlush(variable);
	}

	public void insert(RenderVariable variable) {
		repository.saveAndFlush(variable);
	}

}
