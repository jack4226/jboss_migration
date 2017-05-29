package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.SenderData;

public interface SenderDataRepository extends JpaRepository<SenderData, Integer> {

	//@Lock(LockModeType.OPTIMISTIC)
	public SenderData findOneBySenderId(String senderId);
	
	public SenderData findOneByDomainName(String domainName);
	
	public List<SenderData> findAllByOrderBySenderId();
	
	@Query("select t.systemId from SenderData t where t.senderId = :senderId")
	public String findSystemIdBySenderId(@Param("senderId") String senderId);
	
	@Query("select t.systemKey from SenderData t where t.senderId = :senderId")
	public String findSystemKeyBySenderId(@Param("senderId") String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from SenderData t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from SenderData t where t.senderId=?1", nativeQuery=false)
	public int deleteBySenderId(String senderId);
}
