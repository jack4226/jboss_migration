package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.SenderVariable;

public interface SenderVariableRepository extends JpaRepository<SenderVariable, Integer> {

	public SenderVariable findOneBySenderVariablePK_VariableNameAndSenderVariablePK_SenderData_SenderIdAndSenderVariablePK_StartTime(
			String variableName, String senderId, java.sql.Timestamp startTime);

	public SenderVariable findOneBySenderVariablePK_VariableNameAndSenderVariablePK_SenderData_SenderIdAndSenderVariablePK_StartTimeIsNull(
			String variableName, String senderId);	
	
	@Query("select t from SenderVariable t, SenderData c " +
			" where c=t.senderVariablePK.senderData and c.senderId=:senderId " +
			" and t.senderVariablePK.variableName=:variableName " +
			" and (t.senderVariablePK.startTime<=:startTime or t.senderVariablePK.startTime is null) " +
			" order by t.senderVariablePK.startTime desc")
	public List<SenderVariable> findAllByBestMatchOrderByStartTimeDesc(@Param("variableName") String variableName,
			@Param("senderId") String senderId, @Param("startTime") java.sql.Timestamp startTime);
	
	@Query("select t from SenderVariable t, SenderData c " +
			" where c=t.senderVariablePK.senderData and t.senderVariablePK.variableName=:variableName " +
			" order by c.senderId, t.senderVariablePK.startTime asc")
	public List<SenderVariable> findAllByVariableNameOrderByStartTime(@Param("variableName") String variableName);
	
	// replace findAllByVariableNameOrderByStartTime
	public List<SenderVariable> findAllBySenderVariablePK_VariableNameOrderBySenderVariablePK_SenderData_SenderIdAscSenderVariablePK_StartTime(String variableName);
	
	@Query(value="select a.* from sender_variable a " +
			" inner join ( " +
			"  select b.senderDataRowId as senderDataRowId, b.variableName as variableName, max(b.startTime) as maxTime " +
			"   from sender_variable b, sender_data cd " +
			"   where b.status_id = ?1 and b.startTime<=?2 and b.senderDataRowId=cd.row_Id and cd.senderId=?3 " +
			"   group by b.senderDataRowId, b.variableName " +
			" ) as c " +
			"  on a.variableName=c.variableName and a.startTime=c.maxTime and a.senderDataRowId=c.senderDataRowId " +
			" order by a.row_id asc ", nativeQuery=true)
	public List<SenderVariable> findCurrentBySenderIdOrderByRowId(String statusId, java.sql.Timestamp startTime, String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SenderVariable sv where sv.senderVariablePK.senderData in (select sd from SenderData sd where sd.senderId=?1)")
	public int deleteBySenderVariablePK_SenderData_SenderId(String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from sender_variable where senderDataRowId in (select row_id from sender_data cd where cd.senderId=?1)", nativeQuery=true)
	public int deleteBySenderVariablePK_SenderData_SenderIdNative(String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SenderVariable t where t.senderVariablePK.variableName=:variableName")
	public int deleteBySenderVariablePK_VariableName(@Param("variableName") String variableName);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from SenderVariable sv where sv.senderVariablePK.variableName=?1 and sv.senderVariablePK.startTime=?2 " +
			"and sv.senderVariablePK.senderData in (select sd from SenderData sd where sd.senderId=?3)")
	public int deleteByPrimaryKey(String variableName, java.sql.Timestamp startTime, String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from sender_variable where variableName=?1 and startTime=?2 and senderDataRowId in " +
			"(select row_id from sender_data cd where cd.senderId=?3)", nativeQuery=true)
	public int deleteByPrimaryKeyNative(String variableName, java.sql.Timestamp startTime, String senderId);

}
