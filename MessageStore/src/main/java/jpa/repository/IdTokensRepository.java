package jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.IdTokens;

public interface IdTokensRepository extends JpaRepository<IdTokens, Integer> {

	public IdTokens findOneBySenderData_SenderId(String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from IdTokens t where t.senderData in (select sd from SenderData sd where sd.senderId=?1)")
	public int deleteBySenderId(String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from IdTokens t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
}
