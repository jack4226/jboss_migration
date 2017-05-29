package jpa.repository.msg;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageDeliveryStatus;

public interface MessageDeliveryStatusRepository extends JpaRepository<MessageDeliveryStatus, Integer> {
	
	@Query("select t from MessageDeliveryStatus t, MessageInbox mi where " +
			" mi=t.messageDeliveryStatusPK.messageInbox " +
			" and mi.rowId=?1 and t.messageDeliveryStatusPK.finalRcptAddrRowId=?2")
	public MessageDeliveryStatus findOneByPrimaryKey(Integer msgRowid, Integer finalRcptAddrRowId);
	
	public List<MessageDeliveryStatus> findAllByMessageDeliveryStatusPK_MessageInbox_RowId(Integer rowId);
	
	public Page<MessageDeliveryStatus> findTop20ByMessageDeliveryStatusPK_FinalRcptAddrRowId(Integer rcptAddrRowId, Pageable pageable);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageDeliveryStatus t where t.rowId=?1")
	public int deleteByRowid(Integer rowId);

	@Modifying(clearAutomatically = true)
	@Query("delete from MessageDeliveryStatus md where md.messageDeliveryStatusPK.messageInbox.rowId=?1 " +
			" and md.messageDeliveryStatusPK.finalRcptAddrRowId=?2")
	public int deleteByPrimaryKey(Integer msgRowid, Integer finalRcptAddrRowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageDeliveryStatus md where md.messageDeliveryStatusPK.messageInbox.rowId=?1")
	public int deleteByMsgInboxId(int msgRowId);
}
