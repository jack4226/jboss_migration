package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageActionLog;

public interface MessageActionLogRepository extends JpaRepository<MessageActionLog, Integer> {

	public MessageActionLog findOneByMessageActionLogPK_MessageInbox_RowIdAndMessageActionLogPK_LeadMessageRowId(Integer rowId, Integer leadMsgId);
	
	public List<MessageActionLog> findAllByMessageActionLogPK_MessageInbox_RowId(Integer rowId);
	
	public List<MessageActionLog> findAllByMessageActionLogPK_LeadMessageRowId(Integer leadMsgId);
	
	@Modifying
	@Query("delete from MessageActionLog t where t.rowId=?1")
	public int deleteByRowId(Integer rowId);
	
	@Modifying
	@Query("delete from MessageActionLog ma where ma.messageActionLogPK.messageInbox.rowId=?1 and ma.messageActionLogPK.leadMessageRowId=?2")
	public int deleteByPrimaryKey(Integer rowId, Integer leadMsgId);
	
	@Modifying
	@Query("delete from MessageActionLog t where t.messageActionLogPK.messageInbox.rowId=?1")
	public int deleteByMessageInboxId(Integer rowId);
	
	@Modifying
	@Query("delete from MessageActionLog t where t.messageActionLogPK.leadMessageRowId=?1")
	public int deleteByLeadMessageId(Integer rowId);
}
