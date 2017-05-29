package jpa.repository;

import java.sql.Timestamp;

import jpa.model.rule.RuleAction;

public interface RuleActionRepositoryCustom {

	public RuleAction findOneByPrimaryKey(String ruleName, Integer actionSequence, String senderId,
			java.sql.Timestamp startTime);

	public RuleAction findFirstByMostCurrent(String ruleName, Integer actionSequence, String senderId, String statusId);

	public int deleteByPrimaryKey(String ruleName, Integer actionSequence, String senderId, Timestamp startTime);
}
