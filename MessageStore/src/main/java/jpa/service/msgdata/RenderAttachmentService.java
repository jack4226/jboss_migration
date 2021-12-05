package jpa.service.msgdata;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.RenderAttachment;
import jpa.model.msg.RenderAttachmentPK;
import jpa.repository.msg.RenderAttachmentRepository;

@Component("renderAttachmentService")
@Transactional(propagation=Propagation.REQUIRED)
public class RenderAttachmentService implements java.io.Serializable {
	private static final long serialVersionUID = -4386433389528041498L;

	static Logger logger = LogManager.getLogger(RenderAttachmentService.class);
	
	@Autowired
	RenderAttachmentRepository repository;

	public Optional<RenderAttachment> getByRowId(int rowId) {
		return repository.findById(rowId);
	}

	public RenderAttachment getByPrimaryKey(RenderAttachmentPK pk) {
		if (pk.getMessageRendered()==null) {
			throw new IllegalArgumentException("A MessageRendered instance must be provided in Primary Key object.");
		}
		return repository.findOneByPrimaryKey(pk.getMessageRendered().getRowId(), pk.getAttachmentSequence());
	}

	public List<RenderAttachment> getByRenderId(int renderId) {
		return repository.findAllByRenderAttachmentPK_MessageRendered_RowId(renderId);
	}

	public void delete(RenderAttachment attchmnt) {
		if (attchmnt == null) return;
		repository.delete(attchmnt);
	}

	public int deleteByRowId(int rowId) {
		return repository.deleteByRowId(rowId);
	}

	public int deleteByPrimaryKey(RenderAttachmentPK pk) {
		if (pk.getMessageRendered()==null) {
			throw new IllegalArgumentException("A MessageRendered instance must be provided in Primary Key object.");
		}
		return repository.deleteByPrimaryKey(pk.getMessageRendered().getRowId(), pk.getAttachmentSequence());
	}

	public int deleteByRenderId(int renderId) {
		return repository.deleteByMessageRenderedRowId(renderId);
	}

	public void update(RenderAttachment attchmnt) {
		repository.saveAndFlush(attchmnt);
	}

	public void insert(RenderAttachment attchmnt) {
		repository.saveAndFlush(attchmnt);
	}

}
