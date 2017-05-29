package jpa.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jpa.model.rule.RuleAction;

public interface RuleActionRepository
		extends JpaRepository<RuleAction, Integer>, JpaSpecificationExecutor<RuleAction>, RuleActionRepositoryCustom {
	
	public RuleAction findOneByRowId(Integer rowId);
		
	@Query("select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
			"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al and r.ruleActionPK.senderData=c " +
			"order by r.ruleActionPK.actionSequence")
	public List<RuleAction> findAllOrderByActionSequence();
	
	// This JPQL Query only WORKS under Hibernate, NOT EclipseLink:
	@Query("select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
			"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al " +
			"and (r.ruleActionPK.senderData=c or r.ruleActionPK.senderData is null) " +
			"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
			//"group by r " + // to get rid of duplicates from result set
			"group by r.rowId, r.statusId, r.updtTime, r.updtUserId, r.fieldValues, "
			+ "r.ruleActionDetail.rowId, "
			+ "r.ruleActionPK.ruleLogic.rowId, "
			+ "r.ruleActionPK.actionSequence, "
			+ "r.ruleActionPK.senderData.rowId, "
			+ "r.ruleActionPK.startTime  " + 
			"order by r.ruleActionPK.actionSequence, r.ruleActionPK.startTime")
	public List<RuleAction> findAllByRuleName(@Param("ruleName") String ruleName);

	// PostgreSQL works with: select count(ra.*)
	// MySQL must remove ra.: select count(*) ...
	@Query(value="select count(*) from rule_logic rl join rule_action ra on ra.rulelogicrowid=rl.row_id where rl.rulename=?1",
			nativeQuery=true)
	public int findHasActions(String ruleName); // a method with better performance for Web UI
	
	/*
	 * XXX EclipseLink generated following invalid SQL statement from above JPQL for Derby::
	 * 
	SELECT t0.row_id, t0.FIELDVALUES, t0.statusId, t0.updtTime, t0.updtUserId, t0.ACTIONSEQUENCE, t0.STARTTIME, 
		t0.RuleLogicRowId, t0.SenderDataRowId, t0.RuleActionDetailRowId 
	FROM sender_data t6, rule_logic t5, rule_action_detail t4, sender_data t3, rule_action_detail t2, rule_logic t1, rule_action t0 
	WHERE (((((t0.RuleLogicRowId = t1.row_id) AND (t0.RuleActionDetailRowId = t2.row_id)) 
	AND ((t0.SenderDataRowId = t3.row_id) OR (t0.SenderDataRowId IS NULL))) 
	AND (t5.RULENAME = ?)) AND (((t5.row_id = t0.RuleLogicRowId) 
	AND (t4.row_id = t0.RuleActionDetailRowId)) 
	AND (t6.row_id = t0.SenderDataRowId))) 
	GROUP BY t0.row_id, t0.statusId, t0.updtTime, t0.updtUserId, t0.FIELDVALUES, t4.row_id, t5.row_id, t0.ACTIONSEQUENCE, t6.row_id,
	 	t0.STARTTIME 
	ORDER BY t0.ACTIONSEQUENCE, t0.STARTTIME
	
	With this Exception:
	Internal Exception: java.sql.SQLSyntaxErrorException: Column reference 'T0.RULELOGICROWID' is invalid, or is part of an invalid expression.
	For a SELECT list with a GROUP BY, the columns and expressions being selected may only contain valid grouping expressions and valid aggregate expressions.
	 */
	// Switch to native query to make EclipseLink happy:
	@Query(value="select r.* from Rule_Action r, Rule_Logic rl, Rule_Action_Detail al, Sender_Data c " +
			"where r.RuleLogicRowId=rl.row_id and r.RuleActionDetailRowId=al.row_id " +
			"and (r.SenderDataRowId=c.row_id or r.SenderDataRowId is null) " +
			"and rl.RuleName=?1 " +
			//"group by r " + // to get rid of duplicates from result set
			"group by r.row_id, r.status_id, r.updt_time, r.updt_user_id, r.FieldValues, "
			+ "r.RuleActionDetailRowId, "
			+ "r.RuleLogicRowId, "
			+ "r.ActionSequence, "
			+ "r.SenderDataRowId, "
			+ "r.StartTime  " + 
			"order by r.ActionSequence, r.StartTime", nativeQuery=true)
	public List<RuleAction> findAllByRuleNameNative(@Param("ruleName") String ruleName);

	@Query("select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
			"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al " +
			"and c.senderId=:senderId " +
			"and (r.ruleActionPK.senderData=c or r.ruleActionPK.senderData is null) " +
			"and r.ruleActionPK.ruleLogic.ruleName=:ruleName " +
			"group by r.rowId, r.statusId, r.updtTime, r.updtUserId, r.fieldValues, "
			+ "r.ruleActionDetail.rowId, "
			+ "r.ruleActionPK.ruleLogic.rowId, "
			+ "r.ruleActionPK.actionSequence, "
			+ "r.ruleActionPK.senderData.rowId, "
			+ "r.ruleActionPK.startTime, c.senderId " + // to get rid of duplicates from result set
			"order by r.ruleActionPK.actionSequence, c.senderId, r.ruleActionPK.startTime")
	public List<RuleAction> findAllByRuleName_v0(@Param("ruleName") String ruleName, @Param("senderId") String senderId);

	// TODO revisit when SenderDataRowId column is populated on RuleAction table
	@Query("select r from RuleAction r, RuleLogic rl, RuleActionDetail al, SenderData c " +
			"where r.ruleActionPK.ruleLogic=rl and r.ruleActionDetail=al " +
			"and (r.ruleActionPK.senderData=c or r.ruleActionPK.senderData is null)" +
			"and r.ruleActionPK.ruleLogic.ruleName=:ruleName and c.senderId=:senderId " +
			"and r.ruleActionPK.startTime<=:startTime and r.statusId=:statusId " +
			"order by r.ruleActionPK.actionSequence, c.senderId desc, r.ruleActionPK.startTime desc")
	public List<RuleAction> findAllByBestMatch(@Param("ruleName") String ruleName,
			@Param("startTime") Timestamp startTime, @Param("senderId") String senderId,
			@Param("statusId") String statusId);
	
	
	@Modifying(clearAutomatically = true)
	@Query(value="delete from RuleAction t where t.rowId=?1", nativeQuery=false)
	public int deleteByRowId(Integer rowId);
	
	@Modifying(clearAutomatically = true)
	@Query("delete from RuleAction ra where ra.ruleActionPK.ruleLogic in (select rl from RuleLogic rl where rl.ruleName=?1)")
	public int deleteByRuleName(String ruleName);
}
