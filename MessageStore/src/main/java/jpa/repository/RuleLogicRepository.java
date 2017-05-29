package jpa.repository;

import java.util.List;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jpa.model.rule.RuleLogic;

public interface RuleLogicRepository extends JpaRepository<RuleLogic, Integer> {

	/*
	 *  javax.persistence.PersistenceException caught from EclipseLink with @Lock(LockModeType.OPTIMISTIC): 
	 *  Invalid lock mode type on for an Entity that does not have a version locking index. Only a PESSIMISTIC lock mode
	 *   type can be used when there is no version locking index.
	 */
	//@Lock(LockModeType.OPTIMISTIC) // XXX May cause hibernate to throw NullPointerException, remove when it happens
	public RuleLogic findOneByRowId(Integer rowId);

	//@Lock(LockModeType.OPTIMISTIC) // XXX Caused Hibernate to throw NullPointerException in EntityVerifyVersionProcess
	public RuleLogic findOneByRuleName(String ruleName);
	
	@Lock(LockModeType.NONE) // read only
	public List<RuleLogic> findAllByIsBuiltinRuleTrueOrderByEvalSequence();
	
	@Lock(LockModeType.NONE) // read only
	public List<RuleLogic> findAllByIsBuiltinRuleTrueAndIsSubruleFalseOrderByEvalSequence();
	
	@Lock(LockModeType.NONE) // read only
	public List<RuleLogic> findAllByIsBuiltinRuleOrderByEvalSequence(Boolean isBuiltinRule);
	
	@Lock(LockModeType.NONE) // read only
	public List<RuleLogic> findAllByStatusIdAndStartTimeBeforeOrderByRowId(String statusId, java.sql.Timestamp now);
	
	public List<RuleLogic> findAllByIsSubruleTrue();
	
	public List<RuleLogic> findAllByIsSubruleTrueAndIsBuiltinRuleFalse();
	
	@Query(value = "select max(rl.evalSequence) + 1 from RuleLogic rl", nativeQuery = false)
	public int findNextEvalSequence();
	
	@Query(value = "select rl.ruleName from RuleLogic rl where rl.isBuiltinRule=true and rl.isSubrule=false and rl.ruleCategory=?1 group by rl.ruleName order by rl.ruleName", nativeQuery = false)
	public List<String> findBuiltinRuleNames4Web(String ruleCategory);

	@Query(value = "select distinct(rl.ruleName) as ruleName from RuleLogic rl where rl.isBuiltinRule=false and rl.isSubrule=false and rl.ruleCategory=?1 order by rl.ruleName", nativeQuery = false)
	public List<String> findCustomRuleNames4Web(String ruleCategory);
	
	// PsotgreSQL works with: select count(rm.*) ...
	// MySQL must remove rm.: select count(*) ...
	@Query(value="select count(*) from rule_logic rl join rule_subrule_map rm on rm.rulelogicrowid=rl.row_id where rl.rulename=?1",
			nativeQuery=true)
	public int findHasSubrules(String ruleName); // a method with better performance for Web UI
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleLogic t where t.ruleName=?1", nativeQuery=false)
	public int deleteByRuleName(String ruleName);
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleLogic t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
}
