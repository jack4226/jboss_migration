package jpa.repository.msg;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.msg.TemplateData;

public interface TemplateDataRepository extends JpaRepository<TemplateData, Integer> {

	@Query("select t from TemplateData t, SenderData c " +
			"where c=t.templateDataPK.senderData and c.senderId=?1 " +
			"and t.templateDataPK.templateId=?2 and t.templateDataPK.startTime=?3 ")
	public TemplateData findOneByPrimaryKey(String senderId, String templateId, java.sql.Timestamp startTime);
	
	@Query("select t from TemplateData t, SenderData c " +
			"where c=t.templateDataPK.senderData and c.senderId=?1 " +
			"and t.templateDataPK.templateId=?2 and t.templateDataPK.startTime is null ")
	public TemplateData findOneByPrimaryKey(String senderId, String templateId);
	
	@Query("select t from TemplateData t, SenderData c " +
			" where c=t.templateDataPK.senderData and c.senderId=?1 " +
			" and t.templateDataPK.templateId=?2 " +
			" and (t.templateDataPK.startTime<=?3 or t.templateDataPK.startTime is null) " +
			" order by t.templateDataPK.startTime desc ")
	public List<TemplateData> findAllByBestMatch(String senderId, String templateId, java.sql.Timestamp startTime);
	
	@Query("select t from TemplateData t, SenderData c " +
			" where c=t.templateDataPK.senderData and t.templateDataPK.templateId=?1 " +
			" order by c.senderId, t.templateDataPK.startTime asc ")
	public List<TemplateData> findAllByTemplateId(String templateId);
	
	@Query(value="select a.* from template_data a " +
			" inner join ( " +
			"  select b.senderDataRowId as senderDataRowId, b.templateId as templateId, max(b.startTime) as maxTime " +
			"   from template_data b, sender_data cd " +
			"   where b.status_id = ?1 and b.startTime<=?2 and b.senderDataRowId=cd.row_Id and cd.senderId=?3 " +
			"   group by b.senderDataRowId, b.templateId " +
			" ) as c " +
			"  on a.templateId=c.templateId and a.startTime=c.maxTime and a.senderDataRowId=c.senderDataRowId " +
			" order by a.row_id asc ", nativeQuery=true)
	public List<TemplateData> findAllCurrentBySenderId(String statusId, java.sql.Timestamp startTime, String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from Template_Data where templateId=?1 and startTime=?2 " +
				" and senderDataRowId in (select row_id from sender_data cd where cd.senderId=?3)", nativeQuery=true)
	public int deleteByPrimaryKey(String templateId, java.sql.Timestamp startTime, String senderId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from TemplateData t where t.templateDataPK.templateId=?1")
	public int deleteByTemplateId(String templateId);

	@Modifying(clearAutomatically = true)
	@Query("delete from TemplateData t where t.templateDataPK.senderData in (select cd from SenderData cd where cd.senderId=?1)")
	public int deleteBySenderId(String senderId);

}
