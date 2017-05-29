package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.RenderAttachment;

public interface RenderAttachmentRepository extends JpaRepository<RenderAttachment, Integer> {

	@Query("select t from RenderAttachment t, MessageRendered mi where " +
			" mi=t.renderAttachmentPK.messageRendered and mi.rowId=?1 and t.renderAttachmentPK.attachmentSequence=?2")
	public RenderAttachment findOneByPrimaryKey(Integer renderRowId, Integer sequence);
	
	public List<RenderAttachment> findAllByRenderAttachmentPK_MessageRendered_RowId(Integer renderRowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RenderAttachment t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);

	@Modifying(clearAutomatically = true)
	@Query("delete from RenderAttachment ra where " +
			" ra.renderAttachmentPK.messageRendered.rowId=?1 and ra.renderAttachmentPK.attachmentSequence=?2")
	public int deleteByPrimaryKey(Integer renderRowId, Integer sequence);

	@Modifying(clearAutomatically = true)
	@Query("delete from RenderAttachment t where t.renderAttachmentPK.messageRendered.rowId=?1 ")
	public int deleteByMessageRenderedRowId(Integer renderRowId);
}
