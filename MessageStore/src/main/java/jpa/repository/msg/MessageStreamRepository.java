package jpa.repository.msg;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageStream;

public interface MessageStreamRepository extends JpaRepository<MessageStream, Integer> {

	@Lock(LockModeType.NONE)
	@Query("select t from MessageStream t, EmailAddress ea where ea.rowId=t.fromAddrRowId and ea.address=?1 order by t.rowId desc")
	public List<MessageStream> findAllByFromAddress(String address);
	
	public MessageStream findOneByMessageInbox_RowId(Integer msgRowId);
	
	// find last record
	public MessageStream findFirstByOrderByRowIdDesc();
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageStream t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);

	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageStream t where t.messageInbox.rowId=?1")
	public int deleteByMessageInboxId(Integer msgId);
}
