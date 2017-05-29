package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.msg.MessageAttachment;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, Integer> {

	@Query("select t from MessageAttachment t, MessageInbox mi where " +
			" mi=t.messageAttachmentPK.messageInbox and mi.rowId=:msgId " +
			" and t.messageAttachmentPK.attachmentDepth=:depth " +
			" and t.messageAttachmentPK.attachmentSequence=:sequence")
	public MessageAttachment findOneByPrimaryKey(@Param("msgId")Integer rowId, @Param("depth")Integer depth, @Param("sequence")Integer sequence);
	
	public List<MessageAttachment> findAllByMessageAttachmentPK_MessageInbox_RowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageAttachment t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageAttachment t where t.messageAttachmentPK.messageInbox.rowId=?1 "
			+ "and t.messageAttachmentPK.attachmentDepth=?2 and t.messageAttachmentPK.attachmentSequence=?3", nativeQuery=false)
	public int deleteByPrimaryKey(Integer rowId, Integer depth, Integer sequence);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageAttachment t where t.messageAttachmentPK.messageInbox.rowId=:msgId ", nativeQuery=false)
	public int deleteByMessageInboxId(@Param("msgId")Integer rowId);
	
}
