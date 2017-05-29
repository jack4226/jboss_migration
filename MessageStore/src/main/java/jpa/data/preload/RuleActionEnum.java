package jpa.data.preload;

import java.util.ArrayList;
import java.util.List;

import jpa.constant.EmailAddrType;
import jpa.constant.TableColumnName;

public enum RuleActionEnum {
	HARD_BOUNCE_1(RuleNameEnum.HARD_BOUNCE,1,RuleActionDetailEnum.SAVE,null),
	HARD_BOUNCE_2(RuleNameEnum.HARD_BOUNCE,2,RuleActionDetailEnum.SUSPEND,
			"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue()),
	HARD_BOUNCE_3(RuleNameEnum.HARD_BOUNCE,3,RuleActionDetailEnum.MARK_DLVR_ERR,null),
	HARD_BOUNCE_4(RuleNameEnum.HARD_BOUNCE,4,RuleActionDetailEnum.CLOSE,null),
	SOFT_BOUNCE_1(RuleNameEnum.SOFT_BOUNCE,1,RuleActionDetailEnum.SAVE,null),
	SOFT_BOUNCE_2(RuleNameEnum.SOFT_BOUNCE,2,RuleActionDetailEnum.BOUNCE_UP,
			"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue()),
	SOFT_BOUNCE_3(RuleNameEnum.SOFT_BOUNCE,3,RuleActionDetailEnum.CLOSE,null),
	MAILBOX_FULL_1(RuleNameEnum.MAILBOX_FULL,1,RuleActionDetailEnum.SAVE,null),
	MAILBOX_FULL_2(RuleNameEnum.MAILBOX_FULL,2,RuleActionDetailEnum.BOUNCE_UP,
			"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue()),
	MAILBOX_FULL_3(RuleNameEnum.MAILBOX_FULL,3,RuleActionDetailEnum.CLOSE,null),
	SIZE_TOO_LARGE_1(RuleNameEnum.SIZE_TOO_LARGE,1,RuleActionDetailEnum.SAVE,null),
	SIZE_TOO_LARGE_2(RuleNameEnum.SIZE_TOO_LARGE,2,RuleActionDetailEnum.TO_CSR,"$" + QueueNameEnum.SUBSCRIBER_CARE_INPUT.name()),
	MAIL_BLOCK_1(RuleNameEnum.MAIL_BLOCK,1,RuleActionDetailEnum.SAVE,null),
	MAIL_BLOCK_2(RuleNameEnum.MAIL_BLOCK,2,RuleActionDetailEnum.FORWARD,"$" + TableColumnName.SPAM_CONTROL_ADDR.getValue()),
	SPAM_BLOCK_1(RuleNameEnum.SPAM_BLOCK,1,RuleActionDetailEnum.SAVE,null),
	SPAM_BLOCK_2(RuleNameEnum.SPAM_BLOCK,2,RuleActionDetailEnum.FORWARD,"$"+TableColumnName.SPAM_CONTROL_ADDR),
	VIRUS_BLOCK_1(RuleNameEnum.VIRUS_BLOCK,1,RuleActionDetailEnum.SAVE,null),
	VIRUS_BLOCK_2(RuleNameEnum.VIRUS_BLOCK,2,RuleActionDetailEnum.FORWARD,"$"+TableColumnName.VIRUS_CONTROL_ADDR),
	CHALLENGE_RESPONSE_1(RuleNameEnum.CHALLENGE_RESPONSE,1,RuleActionDetailEnum.FORWARD,"$"+TableColumnName.CHALLENGE_HANDLER_ADDR),
	AUTO_REPLY_1(RuleNameEnum.AUTO_REPLY,1,RuleActionDetailEnum.SAVE,null),
	AUTO_REPLY_2(RuleNameEnum.AUTO_REPLY,2,RuleActionDetailEnum.CLOSE,null),
	CC_USER_1(RuleNameEnum.CC_USER,1,RuleActionDetailEnum.DROP,null),
	MDN_RECEIPT_1(RuleNameEnum.MDN_RECEIPT,1,RuleActionDetailEnum.ACTIVATE,
			"$"+EmailAddrType.FINAL_RCPT_ADDR.getValue()+","+"$"+EmailAddrType.ORIG_RCPT_ADDR.getValue()),
	MDN_RECEIPT_2(RuleNameEnum.MDN_RECEIPT,2,RuleActionDetailEnum.DROP,null),
	CSR_REPLY_15(RuleNameEnum.CSR_REPLY,1,RuleActionDetailEnum.CSR_REPLY,null),
	SEND_MAIL_1(RuleNameEnum.SEND_MAIL,1,RuleActionDetailEnum.SENDMAIL,"$"+EmailAddrType.TO_ADDR.getValue()),
	RMA_REQUEST_1(RuleNameEnum.RMA_REQUEST,1,RuleActionDetailEnum.SAVE,null),
	RMA_REQUEST_2(RuleNameEnum.RMA_REQUEST,2,RuleActionDetailEnum.ACTIVATE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	RMA_REQUEST_3(RuleNameEnum.RMA_REQUEST,3,RuleActionDetailEnum.TO_CSR,"$" + RuleNameEnum.RMA_REQUEST.getValue()),
	UNSUBSCRIBE_1(RuleNameEnum.UNSUBSCRIBE,1,RuleActionDetailEnum.SAVE,null),
	UNSUBSCRIBE_2(RuleNameEnum.UNSUBSCRIBE,2,RuleActionDetailEnum.UNSUBSCRIBE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	UNSUBSCRIBE_3(RuleNameEnum.UNSUBSCRIBE,3,RuleActionDetailEnum.CLOSE,null),
	SUBSCRIBE_1(RuleNameEnum.SUBSCRIBE,1,RuleActionDetailEnum.SAVE,null),
	SUBSCRIBE_2(RuleNameEnum.SUBSCRIBE,2,RuleActionDetailEnum.SUBSCRIBE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	SUBSCRIBE_3(RuleNameEnum.SUBSCRIBE,3,RuleActionDetailEnum.ACTIVATE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	SUBSCRIBE_4(RuleNameEnum.SUBSCRIBE,4,RuleActionDetailEnum.AUTO_REPLY,EmailTemplateEnum.SubscribeByEmailReply.name()),
	SUBSCRIBE_5(RuleNameEnum.SUBSCRIBE,5,RuleActionDetailEnum.CLOSE,null),
	BROADCAST_1(RuleNameEnum.BROADCAST,1,RuleActionDetailEnum.SAVE,null),
	BROADCAST_2(RuleNameEnum.BROADCAST,2,RuleActionDetailEnum.BROADCAST,null),
	BROADCAST_3(RuleNameEnum.BROADCAST,3,RuleActionDetailEnum.CLOSE,null),
	GENERIC_1(RuleNameEnum.GENERIC,1,RuleActionDetailEnum.SAVE,null),
	GENERIC_2(RuleNameEnum.GENERIC,2,RuleActionDetailEnum.ACTIVATE,"$"+EmailAddrType.FROM_ADDR.getValue()),
	GENERIC_3(RuleNameEnum.GENERIC,3,RuleActionDetailEnum.TO_CSR,"custcare@localhost"),
	GENERIC_4(RuleNameEnum.GENERIC,4,RuleActionDetailEnum.OPEN,null),
	UNATTENDED_MAILBOX_1(RuleNameEnum.UNATTENDED_MAILBOX,1,RuleActionDetailEnum.DROP,null),
	OUF_OF_OFFICE_AUTO_REPLY_1(RuleNameEnum.OUF_OF_OFFICE_AUTO_REPLY,1,RuleActionDetailEnum.SAVE,null),
	OUF_OF_OFFICE_AUTO_REPLY_2(RuleNameEnum.OUF_OF_OFFICE_AUTO_REPLY,2,RuleActionDetailEnum.CLOSE,null),
	CONTACT_US_1(RuleNameEnum.CONTACT_US,1,RuleActionDetailEnum.SAVE,null),
	XHEADER_SPAM_SCORE_1(RuleNameEnum.XHEADER_SPAM_SCORE,1,RuleActionDetailEnum.SAVE,null),
	XHEADER_SPAM_SCORE_2(RuleNameEnum.XHEADER_SPAM_SCORE,2,RuleActionDetailEnum.CLOSE,null),
	EXECUTABLE_ATTACHMENT_1(RuleNameEnum.EXECUTABLE_ATTACHMENT,1,RuleActionDetailEnum.DROP,null),
	HARD_BOUNCE_WATCHED_MAILBOX_1(RuleNameEnum.HARD_BOUNCE_WATCHED_MAILBOX,1,RuleActionDetailEnum.SAVE,null),
	HARD_BOUNCE_WATCHED_MAILBOX_2(RuleNameEnum.HARD_BOUNCE_WATCHED_MAILBOX,2,RuleActionDetailEnum.OPEN,null),
	HARD_BOUNCE_NO_FINAL_RCPT_1(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT,1,RuleActionDetailEnum.SAVE,null),
	HARD_BOUNCE_NO_FINAL_RCPT_2(RuleNameEnum.HARD_BOUNCE_NO_FINAL_RCPT,2,RuleActionDetailEnum.OPEN,null);
	
	private RuleNameEnum ruleName;
	private int sequence;
	private RuleActionDetailEnum actionDetail;
	private String fieldValues;
	private RuleActionEnum(RuleNameEnum ruleName, int sequence,
			RuleActionDetailEnum actionDetail, String fieldValues) {
		this.ruleName = ruleName;
		this.sequence = sequence;
		this.actionDetail = actionDetail;
		this.fieldValues = fieldValues;
	}
	
	public static List<RuleActionEnum> getByRuleName(RuleNameEnum ruleNameEnum) {
		List<RuleActionEnum> list = new ArrayList<RuleActionEnum>();
		for (RuleActionEnum actionEnum : RuleActionEnum.values()) {
			if (actionEnum.getRuleName().equals(ruleNameEnum)) {
				list.add(actionEnum);
			}
		}
		return list;
	}

	public RuleNameEnum getRuleName() {
		return ruleName;
	}
	public int getSequence() {
		return sequence;
	}
	public RuleActionDetailEnum getActionDetail() {
		return actionDetail;
	}
	public String getFieldValues() {
		return fieldValues;
	}
}
