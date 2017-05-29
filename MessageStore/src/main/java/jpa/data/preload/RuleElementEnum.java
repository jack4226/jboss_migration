package jpa.data.preload;

import java.util.ArrayList;
import java.util.List;

import jpa.constant.Constants;
import jpa.constant.RuleCriteria;
import jpa.constant.RuleDataName;
import jpa.constant.XHeaderName;

public enum RuleElementEnum {
	/*
	 * for Builtin rules
	 */
	HARD_BOUNCE_1(RuleNameEnum.HARD_BOUNCE,1,RuleDataName.FROM_ADDR, null, RuleCriteria.REG_EX,false,
			"^(?:postmaster|mailmaster|mailadmin|administrator)\\S*\\@", null,
			"postmaster@legacytojava.com,postmaster@" + Constants.VENDER_DOMAIN_NAME,"postmasterTargetText",","),
	HARD_BOUNCE_2(RuleNameEnum.HARD_BOUNCE,2,RuleDataName.FROM_ADDR, null, RuleCriteria.REG_EX,false,
			"^(?:mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@", null,
			"mailer-daemon@legacytojave.com,mailer-daemon@" + Constants.VENDER_DOMAIN_NAME,null,","),

	MAILBOX_FULL_1(RuleNameEnum.MAILBOX_FULL,1,RuleDataName.FROM_ADDR, null, RuleCriteria.REG_EX,false,
			"^(?:postmaster|mailmaster|mailadmin|administrator" +
					"|mailer-(?:daemon|deamon)|smtp.gateway|majordomo)\\S*\\@", null,
			"postmaster@legacytojava.com,postmaster@" + Constants.VENDER_DOMAIN_NAME,null,","),

	SPAM_BLOCK_1(RuleNameEnum.SPAM_BLOCK,1,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"^Spam rapport \\/ Spam report \\S+ -\\s+\\(\\S+\\)$" +
					"|^GWAVA Sender Notification .(?:RBL block|Spam|Content filter).$" +
					"|^\\[MailServer Notification\\]" +
					"|^MailMarshal has detected possible spam in your message", null,
			null,null,null),
	SPAM_BLOCK_2(RuleNameEnum.SPAM_BLOCK,2,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"EarthLink\\b.*(?:spamBlocker|spamArrest)", null,
			null,null,null),
	SPAM_BLOCK_3(RuleNameEnum.SPAM_BLOCK,3,RuleDataName.FROM_ADDR, null, RuleCriteria.REG_EX,false,
			"(?:^surfcontrol|.*You_Got_Spammed)\\S*\\@", null,
			null,null,null),
	SPAM_BLOCK_4(RuleNameEnum.SPAM_BLOCK,4,RuleDataName.X_HEADER, XHeaderName.RETURN_PATH, RuleCriteria.REG_EX,false,
			"^(?:pleaseforward|quotaagent)\\S*\\@", null,
			null,null,null),
	SPAM_BLOCK_5(RuleNameEnum.SPAM_BLOCK,5,RuleDataName.X_HEADER, XHeaderName.PRECEDENCE, RuleCriteria.REG_EX,false,
			"^(?:spam)$", null,
			null,null,null),

	CHALLENGE_RESPONSE_1(RuleNameEnum.CHALLENGE_RESPONSE,1,RuleDataName.X_HEADER, XHeaderName.RETURN_PATH, RuleCriteria.REG_EX,false,
			"(?:spamblocker-challenge|spamhippo|devnull-quarantine)\\@" +
					"|\\@(?:spamstomp\\.com|ipermitmail\\.com)", null,
			null,null,null),
	CHALLENGE_RESPONSE_2(RuleNameEnum.CHALLENGE_RESPONSE,2,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"^(?:Your email requires verification verify:" +
					"|Please Verify Your Email Address" +
					"|Unverified email to " +
					"|Your mail to .* requires confirmation)" +
					"|\\[Qurb .\\d+\\]$", null,
			null,null,null),
	CHALLENGE_RESPONSE_3(RuleNameEnum.CHALLENGE_RESPONSE,3,RuleDataName.FROM_ADDR, null, RuleCriteria.REG_EX,false,
			"confirm-\\S+\\@spamguard\\.vanquish\\.com", null,
			null,null,null),

	AUTO_REPLY_1(RuleNameEnum.AUTO_REPLY,1,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"(?:Exception.*(?:Out\\b.*of\\b.*Office|Autoreply:)|\\(Auto Response\\))" +
				 	"|^(?:Automatically Generated Response from|Auto-Respond E-?Mail from" +
				 	"|AutoResponse - Email Returned|automated response|Yahoo! Auto Response)", null,
			null,null,null),
	AUTO_REPLY_2(RuleNameEnum.AUTO_REPLY,2,RuleDataName.FROM_ADDR, null, RuleCriteria.REG_EX,false,
			"^(?:automated-response|autoresponder|autoresponse-\\S+)\\S*\\@", null,
			null,null,null),
	AUTO_REPLY_3(RuleNameEnum.AUTO_REPLY,3,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"^This messages was created automatically by mail delivery software" +
					"|(?:\\bThis is an autoresponder. I'll never see your message\\b" +
					"|(?:\\bI(?:.m|\\s+am|\\s+will\\s+be|.ll\\s+be)\\s+(?:(?:out\\s+of|away\\s+from)\\s+the\\s+office|on\\s+vacation)\\s+(?:from|to|until|after)\\b)" +
					"|\\bI\\s+am\\s+currently\\s+out\\s+of\\s+the\\s+office\\b" +
					"|\\bI ?.m\\s+away\\s+until\\s+.{10,20}\\s+and\\s+am\\s+unable\\s+to\\s+read\\s+your\\s+message\\b)", null,
			null,null,null),

	VIRUS_BLOCK_1(RuleNameEnum.VIRUS_BLOCK,1,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"^(?:Disallowed attachment type found" +
					"|Norton Anti.?Virus failed to scan an attachment in a message you sent" +
					"|Norton Anti.?Virus detected and quarantined" +
					"|Warning - You sent a Virus Infected Email to " +
					"|Warning:\\s*E-?mail virus(es)? detected" +
					"|MailMarshal has detected a Virus in your message" +
					"|Banned or potentially offensive material" +
					"|Failed to clean virus\\b" +
					"|Virus Alert\\b" +
					"|Virus detected " +
					"|Virus to sender" +
					"|NAV detected a virus in a document " +
					"|InterScan MSS for SMTP has delivered a message" +
					"|InterScan NT Alert" +
					"|Antigen found\\b" +
					"|MMS Notification" +
					"|VIRUS IN YOUR MAIL " +
					"|Scan.?Mail Message: ?.{0,30} virus found " +
					"|McAfee GroupShield Alert)", null,
			null,null,null),
	VIRUS_BLOCK_2(RuleNameEnum.VIRUS_BLOCK,2,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"^(?:Undeliverable mail, invalid characters in header" +
					"|Delivery (?:warning|error) report id=" +
					"|The MIME information you requested" +
					"|Content violation" +
					"|Report to Sender" +
					"|RAV Anti.?Virus scan results" +
					"|Symantec AVF detected " +
					"|Symantec E-Mail-Proxy " +
					"|Virus Found in message" +
					"|Inflex scan report \\[" +
					"|\\[Mail Delivery .{10,100} infected attachment.*removed)" +
				"|(?:(Re: ?)+Wicked screensaver\\b" +
					"|\\bmailsweeper\\b" +
					"|\\bFile type Forbidden\\b" +
					"|AntiVirus scan results" +
					"|Security.?Scan Anti.?Virus" +
					"|Norton\\sAntiVirus\\b.*detected)" +
				"|^(?:Message Undeliverable: Possible Junk\\/Spam Mail Identified" +
					"|EMAIL REJECTED" +
					"|Virusmelding)$", null,
			null,null,null),

	MAIL_BLOCK_1(RuleNameEnum.MAIL_BLOCK,1,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"Message\\b.*blocked\\b.*bulk email filter", null,
			null,null,null),
	MAIL_BLOCK_2(RuleNameEnum.MAIL_BLOCK,2,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"blocked by\\b.*Spam Firewall", null,
			null,null,null),

	BROADCAST_1(RuleNameEnum.BROADCAST,1,RuleDataName.RULE_NAME, null, RuleCriteria.EQUALS,true,
			RuleNameEnum.BROADCAST.getValue(), null,
			null,null,null),

	UNSUBSCRIBE_1(RuleNameEnum.UNSUBSCRIBE,1,RuleDataName.TO_ADDR, null, RuleCriteria.REG_EX,false,
			"^mailinglist@.*|^jwang@localhost$", "mailingListTargetText",
			null,null,null),
	UNSUBSCRIBE_2(RuleNameEnum.UNSUBSCRIBE,2,RuleDataName.SUBJECT, null, RuleCriteria.EQUALS,false,
			"unsubscribe", null,
			null,null,null),

	SUBSCRIBE_1(RuleNameEnum.SUBSCRIBE,1,RuleDataName.TO_ADDR, null, RuleCriteria.REG_EX,false,
			"^mailinglist@.*|^jwang@localhost$", "mailingListTargetText",
			null,null,null),
	SUBSCRIBE_2(RuleNameEnum.SUBSCRIBE,2,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"\\s*subscribe\\s*", null,
			null,null,null),

	RMA_REQUEST_1(RuleNameEnum.RMA_REQUEST,1,RuleDataName.RULE_NAME, null, RuleCriteria.EQUALS,true,
			RuleNameEnum.RMA_REQUEST.getValue(), null,
			null,null,null),
	
	/*
	 * for Custom rules
	 */
	UNATTENDED_MAILBOX_1(RuleNameEnum.UNATTENDED_MAILBOX,1,RuleDataName.MAILBOX_USER, null, RuleCriteria.EQUALS,false,
			"noreply", null,
			null, null, null),
	UNATTENDED_MAILBOX_2(RuleNameEnum.UNATTENDED_MAILBOX,2,RuleDataName.RETURN_PATH, null, RuleCriteria.REG_EX,false,
			"^<?.+@.+>?$", null, // make sure the return path is not blank or <>
			null, null, null),

	OUF_OF_OFFICE_AUTO_REPLY_1(RuleNameEnum.OUF_OF_OFFICE_AUTO_REPLY,1,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"(?:out\\s+of\\s+.*office|\\(away from the office\\)$)", null,
			null, null, null),
	OUF_OF_OFFICE_AUTO_REPLY_2(RuleNameEnum.OUF_OF_OFFICE_AUTO_REPLY,2,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"^.{0,100}\\bwill\\b.{0,50}return|^.{4,100}\\breturning\\b|^.{2,100}\\bvacation\\b", null,
			null, null, null),

	CONTACT_US_1(RuleNameEnum.CONTACT_US,1,RuleDataName.MAILBOX_USER, null, RuleCriteria.EQUALS,false,
			"support", null,
			null, null, null),
	CONTACT_US_2(RuleNameEnum.CONTACT_US,2,RuleDataName.SUBJECT, null, RuleCriteria.STARTS_WITH,false,
			"Inquiry About:", null,
			null, null, null),

	EXECUTABLE_ATTACHMENT_1(RuleNameEnum.EXECUTABLE_ATTACHMENT,1,RuleDataName.SUBJECT, null, RuleCriteria.IS_NOT_BLANK,false,
			"dummy", null,
			null, null, null),
	EXECUTABLE_ATTACHMENT_2(RuleNameEnum.EXECUTABLE_ATTACHMENT,2,RuleDataName.FILE_NAME, null, RuleCriteria.REG_EX,false,
			".*\\.(?:exe|bat|cmd|com|msi|ocx)", null,
			null, null, null),

	XHEADER_SPAM_SCORE_1(RuleNameEnum.XHEADER_SPAM_SCORE,1,RuleDataName.X_HEADER, XHeaderName.SPAM_SCORE, RuleCriteria.GREATER_THAN,false,
			"100", null,
			null, null, null),

	HARD_BOUNCE_WATCHED_MAILBOX_1(RuleNameEnum.HARD_BOUNCE_WATCHED_MAILBOX,1,RuleDataName.RULE_NAME, null, RuleCriteria.EQUALS,true,
			RuleNameEnum.HARD_BOUNCE.getValue(), null,
			null, null, null),
	HARD_BOUNCE_WATCHED_MAILBOX_2(RuleNameEnum.HARD_BOUNCE_WATCHED_MAILBOX,2,RuleDataName.TO_ADDR, null, RuleCriteria.STARTS_WITH,false,
			"watched_maibox@", null,
			null, null, null),

	HARD_BOUNCE_NO_FINAL_RCPT_1(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT,1,RuleDataName.RULE_NAME, null, RuleCriteria.EQUALS,true,
			RuleNameEnum.HARD_BOUNCE.getValue(), null,
			null, null, null),
	HARD_BOUNCE_NO_FINAL_RCPT_2(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT,2,RuleDataName.FINAL_RCPT_ADDR, null, RuleCriteria.IS_BLANK,false,
			"", null,
			null, null, null),
	HARD_BOUNCE_NO_FINAL_RCPT_3(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT,3,RuleDataName.ORIG_RCPT_ADDR, null, RuleCriteria.IS_BLANK,false,
			"", null,
			null, null, null),

	/*
	 * for Sub rules
	 */
	HardBounce_Subj_Match_1(RuleNameEnum.HardBounce_Subj_Match,1,RuleDataName.SUBJECT, null, RuleCriteria.REG_EX,false,
			"^(?:Returned mail:\\s(?:User unknown|Data format error)" +
					"|Undeliverable: |Undeliver(?:able|ed) Mail\\b|Undeliverable Message" +
					"|Returned mail.{0,5}(?:Error During Delivery|see transcript for details)" +
					"|e-?mail addressing error \\(|No valid recipient in )" +
				"|(?:User.*unknown|failed.*delivery|delivery.*(?:failed|failure|problem)" +
					"|Returned mail:.*(?:failed|failure|error)|\\(Failure\\)|failure notice" +
					"|not.*delivered)", null,
			null, null, null),
	HardBounce_Body_Match_1(RuleNameEnum.HardBounce_Body_Match,1,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"(?:\\bYou(?:.ve| have) reached a non.?working address\\.\\s+Please check\\b" +
					"|eTrust Secure Content Manager SMTPMAIL could not deliver the e-?mail" +
					"|\\bPlease do not resend your original message\\." +
					"|\\s[45]\\.\\d{1,3}\\.\\d{1,3}\\s)", null,
			null, null, null),
	MailboxFull_Body_Match_1(RuleNameEnum.MailboxFull_Body_Match,1,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"(?:mailbox|inbox|account).{1,50}(?:exceed|is|was).{1,40}(?:storage|full|limit|size|quota)", null,
			null, null, null),
	MailboxFull_Body_Match_2(RuleNameEnum.MailboxFull_Body_Match,2,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"(?:storage|full|limit|size|quota)", null,
			null, null, null),
	SpamBlock_Body_Match_1(RuleNameEnum.SpamBlock_Body_Match,1,RuleDataName.MSG_REF_ID, null, RuleCriteria.IS_BLANK,false,
			"dummy", null,
			null, null, null),
	ChalResp_Body_Match_1(RuleNameEnum.ChalResp_Body_Match,1,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"(?:Your mail .* requires your confirmation" +
					"|Your message .* anti-spam system.* iPermitMail" +
					"|apologize .* automatic reply.* control spam.* approved senders" +
					"|Vanquish to avoid spam.* automated message" +
					"|automated message.* apologize .* approved senders)", null,
			null, null, null),
	VirusBlock_Body_Match_1(RuleNameEnum.VirusBlock_Body_Match,1,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"(?:a potentially executable attachment " +
					"|\\bhas stripped one or more attachments from the following message\\b" +
					"|message contains file attachments that are not permitted" +
					"|host \\S+ said: 5\\d\\d\\s+Error: Message content rejected" +
					"|TRANSACTION FAILED - Unrepairable Virus Detected. " +
					"|Mail.?Marshal Rule: Inbound Messages : Block Dangerous Attachments" +
					"|The mail message \\S+ \\S+ you sent to \\S+ contains the virus" +
					"|mailsweeper has found that a \\S+ \\S+ \\S+ \\S+ one or more virus" +
					"|Attachment.{0,40}was Deleted" +
					"|Virus.{1,40}was found" +
					"|\\bblocked by Mailsweeper\\b" +
					"|\\bvirus scanner deleted your message\\b" +
					"|\\bThe attachment was quarantined\\b" +
					"|\\bGROUP securiQ.Wall\\b)", null,
			null, null, null),
	VirusBlock_Body_Match_2(RuleNameEnum.VirusBlock_Body_Match,2,RuleDataName.BODY, null, RuleCriteria.REG_EX,false,
			"(?:Reason: Rejected by filter" +
					"|antivirus system report" +
					"|the antivirus module has" +
					"|the infected attachment" +
					"|illegal attachment" +
					"|Unrepairable Virus Detected" +
					"|Reporting-MTA: Norton Anti.?Virus Gateway" +
					"|\\bV I R U S\\b)" +
				"|^(?:Found virus \\S+ in file \\S+" +
					"|Incident Information:)", null,
			null, null, null);
	
	private RuleNameEnum ruleName;
	private int ruleSequence;
	private RuleDataName ruleDataName;
	private XHeaderName xheaderName;
	private RuleCriteria ruleCriteria;
	private boolean isCaseSensitive;
	private String targetText;
	private String targetProcName;
	private String exclusions;
	private String exclListProcName;
	private String delimiter;
	private RuleElementEnum(RuleNameEnum ruleName, int ruleSeq,
			RuleDataName dataName, XHeaderName headerName, RuleCriteria criteria,
			boolean isCaseSensitive, String targetText, String targetProcName, 
			String exclusions, String exclListProcName, String delimiter) {
		this.ruleName=ruleName;
		this.ruleSequence=ruleSeq;
		this.ruleDataName=dataName;
		this.xheaderName=headerName;
		this.ruleCriteria=criteria;
		this.isCaseSensitive=isCaseSensitive;
		this.targetText=targetText;
		this.targetProcName=targetProcName;
		this.exclusions=exclusions;
		this.exclListProcName=exclListProcName;
		this.delimiter=delimiter;
	}

	public static List<RuleElementEnum> getByRuleName(RuleNameEnum ruleNameEnum) {
		List<RuleElementEnum> list = new ArrayList<RuleElementEnum>();
		for (RuleElementEnum elemEnum : RuleElementEnum.values()) {
			if (elemEnum.getRuleName().equals(ruleNameEnum)) {
				list.add(elemEnum);
			}
		}
		return list;
	}

	public RuleNameEnum getRuleName() {
		return ruleName;
	}
	public int getRuleSequence() {
		return ruleSequence;
	}
	public RuleDataName getRuleDataName() {
		return ruleDataName;
	}
	public XHeaderName getXheaderName() {
		return xheaderName;
	}
	public RuleCriteria getRuleCriteria() {
		return ruleCriteria;
	}
	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}
	public String getTargetText() {
		return targetText;
	}
	public String getTargetProcName() {
		return targetProcName;
	}
	public String getExclusions() {
		return exclusions;
	}
	public String getExclListProcName() {
		return exclListProcName;
	}
	public String getDelimiter() {
		return delimiter;
	}
}
