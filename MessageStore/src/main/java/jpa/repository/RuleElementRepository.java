package jpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.rule.RuleElement;

public interface RuleElementRepository extends JpaRepository<RuleElement, Integer> {

	public List<RuleElement> findAllByOrderByRuleElementPK_RuleLogic_RuleNameAscRuleElementPK_ElementSequence();
	
	@Query("select t from RuleElement t, RuleLogic rl where t.ruleElementPK.ruleLogic=rl "
			+ "and rl.ruleName = :ruleName and t.ruleElementPK.elementSequence=:elementSequence")
	public RuleElement findOneByPrimaryKey(@Param("ruleName") String ruleName, @Param("elementSequence") Integer elementSequence);
	
	// find one by primary key, replace findOneByPrimaryKey(ruleName, elementSequence)
	public RuleElement findOneByRuleElementPK_RuleLogic_RuleNameAndRuleElementPK_ElementSequence(String ruleName, Integer elementSequence);
	
	// find all by rule name
	public List<RuleElement> findAllByRuleElementPK_RuleLogic_RuleNameOrderByRuleElementPK_ElementSequence(String ruleName);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleElement t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from RuleElement re where re.ruleElementPK.elementSequence=:elementSequence "
			+ "and re.ruleElementPK.ruleLogic in (select rl from RuleLogic rl where rl.ruleName=:ruleName)")
	public int deleteElementByPrimaryKey(@Param("ruleName") String ruleName, @Param("elementSequence") Integer elementSequence);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from RuleElement re where re.ruleElementPK.ruleLogic in (select rl from RuleLogic rl where rl.ruleName = :ruleName)")
	public int deleteElementsByRuleName(@Param("ruleName") String ruleName);
}
