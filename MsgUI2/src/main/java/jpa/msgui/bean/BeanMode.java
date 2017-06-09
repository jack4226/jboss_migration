package jpa.msgui.bean;

public enum BeanMode {
	list, edit, insert, // for most beans
	viewList, // for MailingListBean
	schedule, // for EmailTemplateBean
	subrules, actions, elements, // for RuleLogicBean
	preview, // for MailinListComposeBean
	send, // for MessageInboxBean
	recipients; // for BroadcastMsgBean
}
