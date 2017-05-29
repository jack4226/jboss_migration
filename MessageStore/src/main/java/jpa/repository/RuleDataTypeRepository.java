package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.rule.RuleDataType;

public interface RuleDataTypeRepository extends JpaRepository<RuleDataType, Integer> {
	
	public RuleDataType findOneByDataType(String dataType);
	
	@Query("select distinct(rd.dataType) as dataType from RuleDataType rd order by rd.dataType asc")
	public List<String> findAllDataTypes();
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleDataType t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleDataType t where t.dataType=?1", nativeQuery=false)
	public int deleteByDataType(String dataType);

}
