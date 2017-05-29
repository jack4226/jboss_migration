package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.MessageSource;

public interface MessageSourceRepository extends JpaRepository<MessageSource, Integer> {

	public MessageSource findOneByMsgSourceId(String msgSourceId);
	
	public List<MessageSource> findAllByFromAddress_Address(String address);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageSource t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);

	@Modifying(clearAutomatically = true)
	@Query(value="delete from MessageSource t where t.msgSourceId=?1", nativeQuery=false)
	public int deleteByMsgSourceId(String sourceId);
}
