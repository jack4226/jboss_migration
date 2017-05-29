package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageHeader;

public interface MessageHeaderRepository extends JpaRepository<MessageHeader, Integer> {

	@Query("select t from MessageHeader t, MessageInbox mi where " +
			" mi=t.messageHeaderPK.messageInbox and mi.rowId=?1 and t.messageHeaderPK.headerSequence=?2 ")
	public MessageHeader findOneByPrimaryKey(Integer msgRowId, Integer sequence);
	
	public List<MessageHeader> findAllByMessageHeaderPK_MessageInbox_RowId(Integer msgRowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageHeader t where t.rowId=?1")
	public int deleteByRowId(Integer rowId);

	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageHeader mh where mh.messageHeaderPK.messageInbox.rowId=?1 and mh.messageHeaderPK.headerSequence=?2")
	public int deleteByPrimaryKey(Integer msgRowId, Integer sequence);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageHeader t where t.messageHeaderPK.messageInbox.rowId=?1")
	public int deleteByMessageInboxId(Integer msgRowId);
}
