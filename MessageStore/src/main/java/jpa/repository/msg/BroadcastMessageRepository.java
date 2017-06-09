package jpa.repository.msg;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.BroadcastMessage;

public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, Integer> {

	public List<BroadcastMessage> findTop100ByOrderByRowIdDesc();
	
	public List<BroadcastMessage> findAllByMailingList_ListId(String listId);
	
	public List<BroadcastMessage> findAllByEmailTemplate_TemplateId(String templateId);
	
	public List<BroadcastMessage> findAllByMailingList_ListIdAndEmailTemplate_TemplateId(String listId, String templateId);
	
	@Modifying
	@Query("update BroadcastMessage t set t.sentCount = (t.sentCount + ?2), t.updtTime = ?3 where t.rowId = ?1")
	public int updateSentCount(Integer rowId, Integer count, java.sql.Timestamp time);
	
	@Modifying
	@Query("update BroadcastMessage t set t.openCount = (t.openCount + 1), t.lastOpenTime = ?3, "
			+ " t.updtTime = ?3, t.updtUserId = ?2 where t.rowId = ?1")
	public int updateOpenCount(Integer rowId, String user, java.sql.Timestamp time);
	
	@Modifying
	@Query("update BroadcastMessage t set t.clickCount = (t.clickCount + 1), t.lastOpenTime = ?3, "
			+ " t.updtTime = ?3, t.updtUserId = ?2 where t.rowId = ?1")
	public int updateClickCount(Integer rowId, String user, java.sql.Timestamp time);
	
	@Modifying
	@Query("update BroadcastMessage t set t.referralCount = (t.referralCount + 1), t.updtTime = :time where t.rowId = :rowId")
	public int updateReferalCount(@Param("rowId") Integer rowId, @Param("time") java.sql.Timestamp time);
	
	@Modifying
	@Query("update BroadcastMessage t set t.unsubscribeCount = (t.unsubscribeCount + 1), t.updtTime = :time where t.rowId = :rowId")	
	public int updateUnsubscribeCount(@Param("rowId") Integer rowId, @Param("time") java.sql.Timestamp time);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from BroadcastMessage t where t.rowId = ?1")	
	public int deleteByRowId(Integer rowId);
	
	public int countBySentCountGreaterThanAndStartTimeNotNull(Integer count);
	
	public Page<BroadcastMessage> findAllBySentCountGreaterThanAndStartTimeNotNull(Integer count, Pageable paging);
}
