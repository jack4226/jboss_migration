package jpa.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jpa.model.EmailAddress;

@Repository
public interface EmailAddressRepository extends JpaRepository<EmailAddress, Integer>, EmailAddressRepositoryCustom,
		JpaSpecificationExecutor<EmailAddress> {

	@Lock(LockModeType.READ) // same as OPTIMISTIC
	public EmailAddress findOneByAddress(String address);
	
	@Lock(LockModeType.OPTIMISTIC) // XXX use this instead of findOne for optimistic locking
	public EmailAddress findOneByRowId(Integer rowId);
	
	public List<EmailAddress> findByAddressEndsWith(String regex);
	
	// find the first record
	public EmailAddress findFirstByOrderByRowIdAsc();
	
	public int deleteByAddress(String addr);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from EmailAddress t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying
	@Query("update EmailAddress p set p.lastRcptTime = :time where p.rowId = :rowId")
	public int updateLastRcptTime(@Param("rowId") Integer rowId, @Param("time") java.sql.Timestamp time);
	
	@Modifying
	@Query("update EmailAddress p set p.lastSentTime = :time where p.rowId = :rowId")
	public int updateLastSentTime(@Param("rowId") Integer rowId, @Param("time") java.sql.Timestamp time);
	
	@Modifying(clearAutomatically = true)
	@Query("update EmailAddress p set p.isAcceptHtml = :acceptHtml where p.rowId = :rowId")
	public int updateAcceptHtml(@Param("rowId") Integer rowId, @Param("acceptHtml") Boolean acceptHtml);

	@Modifying(clearAutomatically = true)
	@Query(value="update Email_Address set IsAcceptHtml = ?2 where row_id = ?1", nativeQuery=true)
	public int updateAcceptHtmlNative(@Param("rowId") Integer rowId, @Param("acceptHtml") Boolean acceptHtml);

	@Modifying(clearAutomatically = true)
	@Query("update #{#entityName} p set p.statusId = :status where p.address = :address")
	public int updateStatus(@Param("address") String address, @Param("status") String status);
}
