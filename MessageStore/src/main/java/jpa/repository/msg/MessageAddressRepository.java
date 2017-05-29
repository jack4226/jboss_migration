package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.msg.MessageAddress;

public interface MessageAddressRepository extends JpaRepository<MessageAddress, Integer> {

	@Query("select t from MessageAddress t, MessageInbox mi, EmailAddress ea where " +
			" mi=t.messageInbox and mi.rowId=?1 and ea.rowId=t.emailAddrRowId and ea.address=?3 " +
			" and t.addressType=?2")
	public MessageAddress findOneByPrimaryKey(Integer rowId, String addrType, String address);
	
	public List<MessageAddress> findAllByMessageInbox_RowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageAddress t where t.rowId=?1")	
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageAddress ma where ma.messageInbox.rowId=?1 and ma.addressType=?2 and ma.emailAddrRowId in " +
			"(select ea.rowId from EmailAddress ea where ea.address=?3)")
	public int deleteByPrimaryKey(int msgId, String addrType, String addrValue);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageAddress t where t.messageInbox.rowId=:rowId ")	
	public int deleteByMessageInboxId(@Param("rowId") Integer rowId);
}
