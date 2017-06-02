package jpa.repository.msg;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jpa.model.msg.MessageInbox;

@Repository
public interface MessageInboxRepository extends JpaRepository<MessageInbox, Integer>,
		JpaSpecificationExecutor<MessageInbox>, MessageInboxRepositoryCustom {

	@Lock(LockModeType.READ)
	public MessageInbox findOneByRowId(Integer rowId);
	
	@Query("select m from MessageInbox m where m.rowId=:rowId ")
	@Lock(LockModeType.NONE)
	public MessageInbox findOneByRowIdNoLock(@Param("rowId") Integer rowId);
	
	public List<MessageInbox> findByLeadMessageRowIdOrderByRowId(Integer leadMsgRowId);
	
	public List<MessageInbox> findByReferringMessageRowIdOrderByRowId(Integer refMsgRowId);
	
	// find the first record
	public MessageInbox findFirstByOrderByRowId();

	// find the last record
	public MessageInbox findFirstByOrderByRowIdDesc();
	
	// find next record
	public MessageInbox findTop1ByRowIdGreaterThanOrderByRowIdAsc(Integer rowId);

	// find previous record
	public MessageInbox findTop1ByRowIdLessThanOrderByRowIdDesc(Integer rowId);
	
	// find recent records by received date
	public List<MessageInbox> findTop100ByReceivedTimeGreaterThanEqualOrderByReceivedTimeDesc(java.sql.Date receivedTime);
	
	// TODO resolve EclipseLink error: (Is this still happening?)
	// Aggregated objects cannot be written/deleted/queried independently from their owners.
	@Modifying(clearAutomatically = true)
	@Query("delete from MessageInbox m where m.rowId=:rowId ")
	public int deleteByRowId(@Param("rowId") Integer rowId);
	
	public List<MessageInbox> findAllByFromAddress_AddressOrderByRowId(String address);

	public List<MessageInbox> findAllByToAddress_AddressOrderByRowId(String address);
	
	public int countByMessageFolder_FolderNameAndReadCountEquals(String folderName, Integer readCount);
	
	public int countByMessageFolder_FolderNameAndReadCountGreaterThan(String folderName, Integer readCount);
	
	public int countByMessageFolder_FolderNameEquals(String folderName);
	
	public int countByReadCountAndStatusIdNot(Integer readCount, String statusId);

	public int countByReadCountAndMsgDirectionAndStatusIdNot(Integer readCount, String msgDirection, String statusId);
	
	@Modifying(clearAutomatically = true)
	@Query("update MessageInbox p set p.statusId = :statusId, p.updtUserId = :updtUserId, p.updtTime = :time where p.leadMessageRowId = :leadMsgId")
	public int updateStatusIdByLeadMsgId(@Param("statusId") String statusId, @Param("updtUserId") String updtUserId,
			@Param("leadMsgId") Integer leadMsgId, @Param("time") java.sql.Timestamp time);

	@Modifying(clearAutomatically = true)
	@Query("update MessageInbox p set p.readCount = :readCount, p.updtUserId = :updtUserId, p.updtTime = :time where p.rowId = :msgId")
	public int updateReadCount(@Param("readCount") Integer readCount, @Param("updtUserId") String updtUserId,
			@Param("msgId") Integer msgId, @Param("time") java.sql.Timestamp time);

	@Modifying(clearAutomatically = true)
	@Query("update MessageInbox p set p.isFlagged = :isFlagged, p.updtUserId = :updtUserId, p.updtTime = :time where p.rowId = :msgId")
	public int updateIsFlagged(@Param("isFlagged") Boolean isFlagged, @Param("updtUserId") String updtUserId,
			@Param("msgId") Integer msgId, @Param("time") java.sql.Timestamp time);

	@Modifying(clearAutomatically = true)
	@Query(nativeQuery=true, value="update message_inbox set updt_user_id = ?3, updt_time = ?4, rulelogicrowid = ("
			+ "select row_id from rule_logic where rulename = ?2) where row_id = ?1")
	public int updateRuleNameByMsgId(Integer msgId, String ruleName, String updtUserId, java.sql.Timestamp time);

	@Override
	// XXX not necessary, can be removed
	public long count(Specification<MessageInbox> spec);
}
