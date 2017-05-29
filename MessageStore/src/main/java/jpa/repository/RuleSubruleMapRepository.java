package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.rule.RuleSubruleMap;

public interface RuleSubruleMapRepository extends JpaRepository<RuleSubruleMap, Integer> {

	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleSubruleMap t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Query("select t from RuleSubruleMap t, RuleLogic rl where t.ruleSubruleMapPK.ruleLogic=rl and rl.ruleName = :ruleName order by t.subruleSequence asc")
	public List<RuleSubruleMap> findAllByRuleNameOrderBySubruleSequence(@Param("ruleName") String ruleName);
	
	@Query("select t from RuleSubruleMap t, RuleLogic rl1, RuleLogic rl2 " +
			"where t.ruleSubruleMapPK.ruleLogic = rl1 and t.ruleSubruleMapPK.subruleLogic = rl2 " +
			"and rl1.ruleName=:ruleName and rl2.ruleName=:subruleName ")
	public RuleSubruleMap findOneByPrimaryKey(@Param("ruleName") String ruleName, @Param("subruleName") String subruleName);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from RuleSubruleMap rs where rs.ruleSubruleMapPK.ruleLogic in (select rl from RuleLogic rl where rl.ruleName=:ruleName)")
	public int deleteByRuleName(@Param("ruleName") String ruleName);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from RuleSubruleMap rs where rs.ruleSubruleMapPK.ruleLogic in (select r1 from RuleLogic r1 where r1.ruleName=:ruleName)" +
			" and rs.ruleSubruleMapPK.subruleLogic in (select r2 from RuleLogic r2 where r2.ruleName=:subruleName)")
	public int deleteByPrimaryKey(@Param("ruleName") String ruleName, @Param("subruleName") String subruleName);
}
