package jpa.data.preload;

import java.util.ArrayList;
import java.util.List;

public enum RuleSubruleMapEnum {
	HARD_BOUNCE_0(RuleNameEnum.HARD_BOUNCE,RuleNameEnum.HardBounce_Subj_Match,0),
	HARD_BOUNCE_1(RuleNameEnum.HARD_BOUNCE,RuleNameEnum.HardBounce_Body_Match,1),
	MAILBOX_FULL_0(RuleNameEnum.MAILBOX_FULL,RuleNameEnum.MailboxFull_Body_Match,0),
	SPAM_BLOCK_0(RuleNameEnum.SPAM_BLOCK,RuleNameEnum.SpamBlock_Body_Match,0),
	CHALLENGE_RESPONSE_0(RuleNameEnum.CHALLENGE_RESPONSE,RuleNameEnum.ChalResp_Body_Match,0),
	VIRUS_BLOCK_0(RuleNameEnum.VIRUS_BLOCK,RuleNameEnum.VirusBlock_Body_Match,0);
	
	private RuleNameEnum ruleName;
	private RuleNameEnum subruleName;
	private int sequence;
	private RuleSubruleMapEnum(RuleNameEnum rule, RuleNameEnum subrule, int sequence) {
		this.ruleName=rule;
		this.subruleName=subrule;
		this.sequence=sequence;
	}
	
	public static List<RuleSubruleMapEnum> getByRuleName(RuleNameEnum ruleNameEnum) {
		List<RuleSubruleMapEnum> list = new ArrayList<RuleSubruleMapEnum>();
		for (RuleSubruleMapEnum rule : RuleSubruleMapEnum.values()) {
			if (rule.getRuleName().equals(ruleNameEnum)) {
				list.add(rule);
			}
		}
		return list;
	}

	public RuleNameEnum getRuleName() {
		return ruleName;
	}
	public RuleNameEnum getSubruleName() {
		return subruleName;
	}
	public int getSequence() {
		return sequence;
	}
}
