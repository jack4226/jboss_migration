package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.BroadcastTracking;

public interface BroadcastTrackingRepository extends JpaRepository<BroadcastTracking, Integer> {

	public BroadcastTracking findOneByEmailAddress_RowIdAndBroadcastMessage_RowId(Integer emailAddrRowId, Integer broadcastMsgRowId);
	
	public List<BroadcastTracking> findAllByEmailAddress_Address(String address);
	
	public List<BroadcastTracking> findAllByEmailAddress_RowId(Integer rowId);
	
	public List<BroadcastTracking> findAllByBroadcastMessage_RowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from BroadcastTracking t where t.rowId = ?1")
	public int deleteByRowId(Integer rowId);
	
	@Modifying
	@Query("update BroadcastTracking t set t.sentCount = (t.sentCount + ?2), t.updtTime = ?3 where t.rowId = ?1")
	public int updateSentCount(Integer rowId, Integer count, java.sql.Timestamp time);
	
	@Modifying
	@Query("update BroadcastTracking t set t.openCount = (t.openCount + 1), t.lastOpenTime = :time, "
			+ "t.updtTime = :time where t.rowId = :rowId")
	public int updateOpenCount(@Param("rowId") Integer rowId, @Param("time") java.sql.Timestamp time);
	
	@Modifying
	@Query("update BroadcastTracking t set t.clickCount = (t.clickCount + 1), t.lastClickTime = :time, "
			+ "t.updtTime = :time where t.rowId = :rowId")
	public int updateClickCount(@Param("rowId") Integer rowId, @Param("time") java.sql.Timestamp time);
	
}
