package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.GlobalVariable;

public interface GlobalVariableRepository extends JpaRepository<GlobalVariable, Integer> {

	public List<GlobalVariable> findByGlobalVariablePK_VariableNameOrderByGlobalVariablePK_StartTime(String variableName);
	
	public GlobalVariable findOneByGlobalVariablePK_VariableNameAndGlobalVariablePK_StartTime(String variableName, java.sql.Timestamp startTime);
	
	public GlobalVariable findOneByGlobalVariablePK_VariableNameAndGlobalVariablePK_StartTimeIsNull(String variableName);
	
	@Query("select t from GlobalVariable t where t.globalVariablePK.variableName=:variableName " +
			" and (t.globalVariablePK.startTime<=:startTime or t.globalVariablePK.startTime is null) " +
			" order by t.globalVariablePK.startTime desc")
	public List<GlobalVariable> findAllByBestMatch(@Param("variableName") String variableName, @Param("startTime") java.sql.Timestamp startTime);
	
	@Query(value="select a.* from Global_Variable a " + 
			" inner join (select b.variableName as variableName, max(b.startTime) as maxTime " +
			" from Global_Variable b where b.status_id = ?1 and b.startTime<=?2  group by b.variableName) as c " +
			" on a.variableName=c.variableName and a.startTime=c.maxTime order by a.variableName asc", nativeQuery=true)
	public List<GlobalVariable> findAllCurrentVariables(String statusId, java.sql.Timestamp startTime);
	
	@Query("select t from GlobalVariable t where t.statusId=:statusId and t.globalVariablePK.startTime<=:startTime" +
			" order by t.globalVariablePK.variableName asc, t.globalVariablePK.startTime desc")
	public List<GlobalVariable> findAllByStatusIdAndStartTime(@Param("statusId") String statusId, @Param("startTime") java.sql.Timestamp startTime);
	
	public int deleteByGlobalVariablePK_VariableNameAndGlobalVariablePK_StartTime(String variableName, java.sql.Timestamp startTime);
	
	public int deleteByGlobalVariablePK_VariableName(String variableName);
}
