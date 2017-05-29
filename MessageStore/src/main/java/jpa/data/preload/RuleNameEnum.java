package jpa.data.preload;

import java.util.ArrayList;
import java.util.List;

import jpa.constant.RuleCategory;
import jpa.constant.RuleType;

//
// define SMTP Built-in Rule Types
//
public enum RuleNameEnum {
	/*
	 * From MessageParserBo, when no rules were matched
	 */
	GENERIC("Generic", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "Non bounce or system could not recognize it"), // default rule name for SMTP Email
	/* 
	 * From RFC Scan routine, rule reassignment, or custom routine
	 */
	HARD_BOUNCE("Hard Bounce", RuleType.ANY, RuleCategory.MAIN_RULE, true, false, "From RFC Scan Routine, or from postmaster with sub-rules"), // Hard bounce - suspend,notify,close
	SOFT_BOUNCE("Soft Bounce", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "Soft bounce, from RFC scan routine"), // Soft bounce - bounce++,close
	MAILBOX_FULL("Mailbox Full", RuleType.ANY, RuleCategory.MAIN_RULE, true, false, "Mailbox full from postmaster with sub-rules"), // treated as Soft Bounce
	SIZE_TOO_LARGE("Size Too Large", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "Message size too large"), // message length exceeded administrative limit, treat as Soft Bounce
	MAIL_BLOCK("Mail Block", RuleType.ALL, RuleCategory.MAIN_RULE, true, false, "Bounced from Bulk Email Filter"), // message content rejected, treat as Soft Bounce
	SPAM_BLOCK("Spam Block", RuleType.ANY, RuleCategory.MAIN_RULE, true, false, "Bounced from Spam blocker"), // blocked by Spam filter, 
	VIRUS_BLOCK("Virus Block", RuleType.ANY, RuleCategory.MAIN_RULE, true, false, "Bounced from Virus blocker"), // blocked by Virus Scan,
	CHALLENGE_RESPONSE("Challenge Response", RuleType.ANY, RuleCategory.MAIN_RULE, true, false, "Bounced from Challenge Response"), // human response needed
	AUTO_REPLY("Auto Reply", RuleType.ANY, RuleCategory.MAIN_RULE, true, false, "Auto reply from email sender software"), // automatic response from mail sender
	CC_USER("Carbon Copies", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "From scan routine, message received as recipient of CC or BCC"), // Mail received from a CC address, drop
	MDN_RECEIPT("MDN Receipt", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "From RFC scan, Message Delivery Notification, a positive receipt"), // MDN - read receipt, drop
	UNSUBSCRIBE("Unsubscribe", RuleType.ALL, RuleCategory.MAIN_RULE, true, false, "Remove from a mailing list"), // remove from mailing list
	SUBSCRIBE("Subscribe", RuleType.ALL, RuleCategory.MAIN_RULE, true, false, "Subscribe to a mailing list"), // add to mailing list
	/*
	 * From rule reassignment or custom routine
	 */
	CSR_REPLY("CSR Reply", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "Called from internal program"), // internal only, reply message from CSR
	RMA_REQUEST("RMA Request", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "RMA request, internal only"), // internal only
	BROADCAST("Broadcast", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "Called from internal program"), // internal only
	SEND_MAIL("Send Mail", RuleType.SIMPLE, RuleCategory.MAIN_RULE, true, false, "Called from internal program"), // internal only
	
	/*
	 * Custom rules
	 */
	UNATTENDED_MAILBOX("Unattended_Mailbox", RuleType.ALL, RuleCategory.PRE_RULE, false, false, "Simply get rid of the messages from the mailbox."),
	OUF_OF_OFFICE_AUTO_REPLY("OutOfOffice_AutoReply", RuleType.ALL, RuleCategory.MAIN_RULE, false, false, "Ouf of the office auto reply"),
	CONTACT_US("Contact_Us", RuleType.ALL, RuleCategory.MAIN_RULE, false, false, "Contact Us Form submitted from web site"),
	XHEADER_SPAM_SCORE("XHeader_SpamScore", RuleType.SIMPLE, RuleCategory.MAIN_RULE, false, false, "Examine x-headers for Spam score."),
	EXECUTABLE_ATTACHMENT("Executable_Attachment", RuleType.ALL, RuleCategory.MAIN_RULE, false, false, "Emails with executable attachment file(s)"),
	HARD_BOUNCE_WATCHED_MAILBOX("HardBouce_WatchedMailbox", RuleType.ALL, RuleCategory.POST_RULE, false, false, "Post rule for hard bounced emails."),
	HARD_BOUNCE_NO_FINAL_RCPT("HardBounce_NoFinalRcpt", RuleType.ALL, RuleCategory.POST_RULE, false, false, "Post rule for hard bounces without final recipient."),

	/*
	 * Sub rules
	 */
	HardBounce_Subj_Match("HardBounce_Subj_Match", RuleType.ANY, RuleCategory.MAIN_RULE, true, true, "Sub rule for hard bounces from postmaster"),
	HardBounce_Body_Match("HardBounce_Body_Match", RuleType.ANY, RuleCategory.MAIN_RULE, true, true, "Sub rule for hard bounces from postmaster"),
	MailboxFull_Body_Match("MailboxFull_Body_Match", RuleType.ALL, RuleCategory.MAIN_RULE, true, true, "Sub rule for mailbox full"),
	SpamBlock_Body_Match("SpamBlock_Body_Match", RuleType.ANY, RuleCategory.MAIN_RULE, true, true, "Sub rule for spam block"),
	VirusBlock_Body_Match("VirusBlock_Body_Match", RuleType.ANY, RuleCategory.MAIN_RULE, true, true, "Sub rule for virus block"),
	ChalResp_Body_Match("ChalResp_Body_Match", RuleType.ANY, RuleCategory.MAIN_RULE, true, true, "Sub rule for challenge response");

	private final String value;
	private final RuleType ruleType;
	private RuleCategory ruleCategory;
	private boolean isBuiltin;
	private boolean isSubrule;
	private final String description;

	private RuleNameEnum(String value, RuleType ruleType,
			RuleCategory ruleCategory, boolean isBuiltin, boolean isSubrule,
			String description) {
		this.value = value;
		this.ruleType = ruleType;
		this.ruleCategory = ruleCategory;
		this.isBuiltin = isBuiltin;
		this.isSubrule = isSubrule;
		this.description = description;
	}
	
	public static RuleNameEnum getByValue(String value) {
		for (RuleNameEnum rule : RuleNameEnum.values()) {
			if (rule.getValue().equalsIgnoreCase(value)) {
				return rule;
			}
		}
		throw new IllegalArgumentException("No enum const found by value (" + value + ")");
	}

	private static final List<RuleNameEnum> builtinRules = new ArrayList<RuleNameEnum>();
	public static List<RuleNameEnum> getBuiltinRules() {
		if (builtinRules.isEmpty()) {
			synchronized (builtinRules) {
				for (RuleNameEnum rn : RuleNameEnum.values()) {
					if (rn.isBuiltin && !rn.isSubrule) {
						builtinRules.add(rn);
					}
				}
			}
		}
		return builtinRules;
	}

	private static final List<RuleNameEnum> customRules = new ArrayList<RuleNameEnum>();
	public static List<RuleNameEnum> getCustomRules() {
		if (customRules.isEmpty()) {
			synchronized (customRules) {
				for (RuleNameEnum rn : RuleNameEnum.values()) {
					if (!rn.isBuiltin && !rn.isSubrule) {
						customRules.add(rn);
					}
				}
			}
		}
		return customRules;
	}

	private static final List<RuleNameEnum> subRules = new ArrayList<RuleNameEnum>();
	public static List<RuleNameEnum> getSubRules() {
		if (subRules.isEmpty()) {
			synchronized (subRules) {
				for (RuleNameEnum rn : RuleNameEnum.values()) {
					if (rn.isBuiltin && rn.isSubrule) {
						subRules.add(rn);
					}
				}
			}
		}
		return subRules;
	}

	private static final List<RuleNameEnum> preRules = new ArrayList<RuleNameEnum>();
	public static List<RuleNameEnum> getPreRules() {
		if (preRules.isEmpty()) {
			synchronized (preRules) {
				for (RuleNameEnum rn : RuleNameEnum.values()) {
					if (rn.ruleCategory.equals(RuleCategory.PRE_RULE)) {
						preRules.add(rn);
					}
				}
			}
		}
		return preRules;
	}

	private static final List<RuleNameEnum> postRules = new ArrayList<RuleNameEnum>();
	public static List<RuleNameEnum> getPostRules() {
		if (postRules.isEmpty()) {
			synchronized (postRules) {
				for (RuleNameEnum rn : RuleNameEnum.values()) {
					if (rn.ruleCategory.equals(RuleCategory.POST_RULE)) {
						postRules.add(rn);
					}
				}
			}
		}
		return postRules;
	}

	public String getValue() { 
		return value;
	}
	
	public RuleType getRuleType() {
		return ruleType;
	}
	
	public RuleCategory getRuleCategory() {
		return ruleCategory;
	}
	
	public boolean isBuiltin() {
		return isBuiltin;
	}

	public boolean isSubrule() {
		return isSubrule;
	}

	public String getDescription() {
		return description;
	}
}