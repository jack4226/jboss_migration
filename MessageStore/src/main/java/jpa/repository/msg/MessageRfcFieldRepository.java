package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageRfcField;

public interface MessageRfcFieldRepository extends JpaRepository<MessageRfcField, Integer> {

	@Query("select t from MessageRfcField t, MessageInbox mi where " +
			" mi=t.messageRfcFieldPK.messageInbox and mi.rowId=?1 and t.messageRfcFieldPK.rfcType=?2")
	public MessageRfcField findOneByPrimaryKey(Integer msgRowId, String rfcType);
	
	public List<MessageRfcField> findAllBymessageRfcFieldPK_MessageInbox_RowId(Integer msgRowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageRfcField t where t.rowId=?1")
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageRfcField mr where mr.messageRfcFieldPK.messageInbox.rowId=?1 and mr.messageRfcFieldPK.rfcType=?2")
	public int deleteByPrimaryKey(Integer msgRowId, String rfcType);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageRfcField t where t.messageRfcFieldPK.messageInbox.rowId=?1")
	public int deleteByMessageInboxId(Integer msgRowId);
}
