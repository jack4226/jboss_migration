package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageUnsubComment;

public interface MessageUnsubCommentRepository extends JpaRepository<MessageUnsubComment, Integer> {

	public MessageUnsubComment findOneByMessageInbox_RowId(Integer msgRowId);
	
	public List<MessageUnsubComment> findAllByEmailAddr_Address(String address);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageUnsubComment t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);

	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageUnsubComment t where t.messageInbox.rowId=?1")
	public int deleteByMessageInboxId(Integer msgRowId);
}
