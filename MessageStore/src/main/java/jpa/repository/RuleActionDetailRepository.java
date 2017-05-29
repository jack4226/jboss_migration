package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.rule.RuleActionDetail;

public interface RuleActionDetailRepository extends JpaRepository<RuleActionDetail, Integer> {

	public RuleActionDetail findOneByActionId(String actionId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleActionDetail t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleActionDetail t where t.actionId=?1", nativeQuery=false)
	public int deleteByActionId(String actionId);
	
	@Query(value="select distinct(ra.actionId) as actionId from RuleActionDetail ra order by ra.actionId")
	public List<String> findAllActionIds();
}
