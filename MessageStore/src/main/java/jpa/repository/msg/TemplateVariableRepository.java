package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.TemplateVariable;

public interface TemplateVariableRepository extends JpaRepository<TemplateVariable, Integer> {

	@Query("select t from TemplateVariable t, SenderData c " +
			"where c=t.templateVariablePK.senderData and c.senderId=?1 and t.templateVariablePK.variableId=?2 " +
			"and t.templateVariablePK.variableName=?3 and t.templateVariablePK.startTime=?4")
	public TemplateVariable findOneByPrimaryKey(String senderId, String variableId, String variableName, java.sql.Timestamp startTime);
	
	@Query("select t from TemplateVariable t, SenderData c " +
			"where c=t.templateVariablePK.senderData and c.senderId=?1 and t.templateVariablePK.variableId=?2 " +
			"and t.templateVariablePK.variableName=?3 and t.templateVariablePK.startTime is null")
	public TemplateVariable findOneByPrimaryKey(String senderId, String variableId, String variableName);

	@Query("select t from TemplateVariable t,SenderData c " +
			" where c=t.templateVariablePK.senderData and c.senderId=?1 and t.templateVariablePK.variableId=?2 " +
			" and t.templateVariablePK.variableName=?3 " +
			" and (t.templateVariablePK.startTime<=?4 or t.templateVariablePK.startTime is null) " +
			" order by t.templateVariablePK.startTime desc ")
	public List<TemplateVariable> findAllByBestMatch(String senderId, String variableId, String variableName, java.sql.Timestamp startTime);
	
	@Query("select t from TemplateVariable t, SenderData c " +
			" where c=t.templateVariablePK.senderData and t.templateVariablePK.variableId=?1 " +
			" order by c.senderId, t.templateVariablePK.startTime asc ")
	public List<TemplateVariable> findAllByVariableId(String variableId);
	
	@Query(value="select a.* from template_variable a " +
			" inner join ( " +
			"  select b.variableId as variableId, b.variableName as variableName, max(b.startTime) as maxTime " +
			"   from template_variable b " +
			"   where b.status_id = ?1 and b.startTime<=?2 and b.variableId=?3 " +
			"   group by b.variableId, b.variableName " +
			" ) as c " +
			"  on a.variableName=c.variableName and a.startTime=c.maxTime and a.variableId=c.variableId " +
			" order by a.row_id asc ", nativeQuery=true)
	public List<TemplateVariable> findCurrentByVariableId(String statusId, java.sql.Timestamp startTime, String variableId);
	
	@Query(value="select a.* from template_variable a " +
			" inner join ( " +
			"  select b.senderDataRowId as senderDataRowId, b.variableId as variableId, b.variableName as variableName, max(b.startTime) as maxTime " +
			"   from template_variable b, sender_data cd " +
			"   where b.status_id = ?1 and b.startTime<=?2 and b.senderDataRowId=cd.row_Id and cd.senderId=?3 " +
			"   group by b.senderDataRowId, b.variableId, b.variableName " +
			" ) as c " +
			"  on a.variableId=c.variableId and a.variableName=c.variableName and a.startTime=c.maxTime and a.senderDataRowId=c.senderDataRowId " +
			" order by a.row_id asc ", nativeQuery=true)
	public List<TemplateVariable> findCurrentBySenderId(String statusId, java.sql.Timestamp startTime, String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from TemplateVariable t " +
			" where t.templateVariablePK.variableId=?1 and t.templateVariablePK.variableName=?2 and t.templateVariablePK.startTime=?3 " +
			" and t.templateVariablePK.senderData in " +
			" (select cd from SenderData cd where cd.senderId=?4) ", nativeQuery=false)
	public int deleteByPrimaryKey(String variableId, String variableName, java.sql.Timestamp startTime, String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from TemplateVariable t where t.templateVariablePK.variableId=?1")
	public int deleteByVariableId(String variableId);

	@Modifying(clearAutomatically = true)
	@Query("delete from TemplateVariable t where t.templateVariablePK.variableName=?1")
	public int deleteByVariableName(String variableName);

	@Modifying(clearAutomatically = true)
	@Query("delete from TemplateVariable t where t.templateVariablePK.senderData in (select cd from SenderData cd where cd.senderId=?1)")
	public int deleteBySenderId(String senderId);

}
