package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.rule.RuleDataValue;

public interface RuleDataValueRepository extends JpaRepository<RuleDataValue, Integer> {

	@Query("select t from RuleDataValue t where t.ruleDataValuePK.ruleDataType.dataType=:dataType and t.ruleDataValuePK.dataValue=:dataValue")
	public RuleDataValue findOneByPrimaryKey(@Param("dataType") String dataType, @Param("dataValue") String dataValue);
	
	@Query("select t from RuleDataValue t where t.ruleDataValuePK.ruleDataType.dataType = :dataType")
	public List<RuleDataValue> findAllByDataType(@Param("dataType") String dataType);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleDataValue t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from RuleDataValue rd where rd.ruleDataValuePK.ruleDataType in (select rt from RuleDataType rt where rt.dataType = :dataType)")
	public int deleteByDataType(@Param("dataType") String dataType);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from RuleDataValue rd where rd.ruleDataValuePK.ruleDataType in (select rt from RuleDataType rt where rt.dataType=:dataType) " +
			" and rd.ruleDataValuePK.dataValue=:dataValue")
	public int deleteByPrimarykey(@Param("dataType") String dataType, @Param("dataValue") String dataValue);
}
