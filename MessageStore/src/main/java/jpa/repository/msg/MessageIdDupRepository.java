package jpa.repository.msg;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.msg.MessageIdDuplicate;

public interface MessageIdDupRepository extends JpaRepository<MessageIdDuplicate, String> {
	
	public MessageIdDuplicate findOneByMessageId(String messageId);
	
	@Modifying
	@Query("delete from MessageIdDuplicate p where p.addTime < :date")
	public int deleteByAddTimeBefore(@Param("date") java.util.Date date);
}
