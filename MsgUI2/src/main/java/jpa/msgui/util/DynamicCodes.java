package jpa.msgui.util;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import jpa.constant.Constants;
import jpa.constant.VariableName;
import jpa.model.EmailTemplate;
import jpa.model.EmailVariable;
import jpa.model.MailingList;
import jpa.model.SenderData;
import jpa.model.SenderVariable;
import jpa.model.rule.RuleLogic;
import jpa.service.common.EmailTemplateService;
import jpa.service.common.EmailVariableService;
import jpa.service.common.SenderDataService;
import jpa.service.common.SenderVariableService;
import jpa.service.maillist.MailingListService;
import jpa.service.rule.RuleActionDetailService;
import jpa.service.rule.RuleDataTypeService;
import jpa.service.rule.RuleLogicService;

import org.springframework.web.context.WebApplicationContext;

@javax.inject.Named(value = "dynacodes")
@javax.enterprise.context.ApplicationScoped
public class DynamicCodes implements java.io.Serializable {
	private static final long serialVersionUID = 2326564001920612689L;

	static transient WebApplicationContext webContext = null;
	
	private static RuleLogicService ruleLogicDao = null;
	public static RuleLogicService getRuleLogicService() {
		if (ruleLogicDao == null) {
			ruleLogicDao = (RuleLogicService) SpringUtil.getWebAppContext().getBean("ruleLogicService");
		}
		return ruleLogicDao;
	}
	
	private static RuleActionDetailService msgActionDetailDao = null;
	public static RuleActionDetailService getRuleActionDetailService() {
		if (msgActionDetailDao == null) {
			msgActionDetailDao = (RuleActionDetailService) SpringUtil.getWebAppContext().getBean(
					"ruleActionDetailService");
		}
		return msgActionDetailDao;
	}
	
	private static RuleDataTypeService msgDataTypeDao = null;
	public static RuleDataTypeService getRuleDataTypeService() {
		if (msgDataTypeDao == null) {
			msgDataTypeDao = (RuleDataTypeService) SpringUtil.getWebAppContext().getBean(
					"ruleDataTypeService");
		}
		return msgDataTypeDao;
	}
	
	private static SenderDataService clientDao = null;
	public static SenderDataService getSenderDataService() {
		if (clientDao == null) {
			clientDao = (SenderDataService) SpringUtil.getWebAppContext().getBean("senderDataService");
		}
		return clientDao;
	}
	
	private static MailingListService mailingListDao = null;
	public static MailingListService getMailingListService() {
		if (mailingListDao == null) {
			mailingListDao = (MailingListService) SpringUtil.getWebAppContext().getBean("mailingListService");
		}
		return mailingListDao;
	}
	
	private static EmailVariableService emailVariableDao = null;
	public static EmailVariableService getEmailVariableService() {
		if (emailVariableDao == null) {
			emailVariableDao = (EmailVariableService) SpringUtil.getWebAppContext().getBean("emailVariableService");
		}
		return emailVariableDao;
	}
	
	private static EmailTemplateService emailTemplateDao = null;
	public static EmailTemplateService getEmailTemplateService() {
		if (emailTemplateDao == null) {
			emailTemplateDao = (EmailTemplateService) SpringUtil.getWebAppContext().getBean("emailTemplateService");
		}
		return emailTemplateDao;
	}
	
	private static SenderVariableService clientVariableDao = null;
	public static SenderVariableService getSenderVariableService() {
		if (clientVariableDao == null) {
			clientVariableDao = (SenderVariableService) SpringUtil.getWebAppContext().getBean("senderVariableService");
		}
		return clientVariableDao;
	}
	
	// PROPERTY: SubRule Items 
	public SelectItem[] getSubruleItems() {
		List<RuleLogic> list = getRuleLogicService().getSubrules(false); // XXX true);
		SelectItem[] subRules = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			subRules[i] = new SelectItem(list.get(i).getRuleName());
		}
		return subRules;
	}
	
	// PROPERTY: Rule Name Items 
	public SelectItem[] getBuiltinRuleNameItems() {
		List<String> list = getRuleLogicService().getBuiltinRuleNames4Web();
		SelectItem[] ruleNames = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			ruleNames[i] = new SelectItem(list.get(i));
		}
		return ruleNames;
	}
	
	// PROPERTY: Custom Rule Name Items 
	public SelectItem[] getCustomRuleNameItems() {
		List<String> list = getRuleLogicService().getCustomRuleNames4Web();
		SelectItem[] ruleNames = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			ruleNames[i] = new SelectItem(list.get(i));
		}
		return ruleNames;
	}
	
	// PROPERTY: ActionId Items 
	public SelectItem[] getActionIdItems() {
		List<String> list = getRuleActionDetailService().getActionIdList();
		//list.add("-- New Action");
		SelectItem[] actionIds = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			actionIds[i] = new SelectItem(list.get(i));
		}
		return actionIds;
	}
	
	// PROPERTY: DataType Items 
	public SelectItem[] getMsgDataTypeItems() {
		List<String> list = getRuleDataTypeService().getDataTypeList();
		SelectItem[] dataTypes = new SelectItem[list.size() + 1];
		dataTypes[0] = new SelectItem(null, "");
		for (int i=0; i<list.size(); i++) {
			dataTypes[i + 1] = new SelectItem(list.get(i), list.get(i));
		}
		return dataTypes;
	}

	// PROPERTY: SenderId Items 
	public SelectItem[] getSenderIdItems() {
		List<SenderData> list = getSenderDataService().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String senderId = list.get(i).getSenderId();
			dataTypes[i] = new SelectItem(senderId, senderId);
		}
		return dataTypes;
	}
	
	// PROPERTY: Mailing List listId Items 
	public SelectItem[] getMailingListIdItems() {
		List<MailingList> list = getMailingListService().getAll(false);
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String listId = list.get(i).getListId();
			String display = listId + " - " + list.get(i).getListEmailAddr();
			dataTypes[i] = new SelectItem(listId, display);
		}
		return dataTypes;
	}
	
	// PROPERTY: Email Variable Name Items 
	public SelectItem[] getEmailVariableNameItems() {
		// 1) custom variables
		List<EmailVariable> list = getEmailVariableService().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		// custom variable names
		for (int i=0; i<list.size(); i++) {
			String variableName = list.get(i).getVariableName();
			dataTypes[i] = new SelectItem(variableName);
		}
		return dataTypes;
	}
	
	// PROPERTY: Global Email Variable Name Items 
	public SelectItem[] getGlobalVariableNameItems() {
		// 2) Mailing List built-in variables
		VariableName.LIST_VARIABLE_NAME[] listVarNames = VariableName.LIST_VARIABLE_NAME.values();
		// 3) Client built-in variables
		List<SenderVariable> senderVars = getSenderVariableService().getCurrentBySenderId(
				Constants.DEFAULT_SENDER_ID);
		List<String> clientVarNames = new ArrayList<String>();
		for (SenderVariable senderVar : senderVars) {
			if (!"SenderId".equalsIgnoreCase(senderVar.getSenderVariablePK().getVariableName())) {
				clientVarNames.add(senderVar.getSenderVariablePK().getVariableName());
			}
		}
		SelectItem[] dataTypes = new SelectItem[listVarNames.length
				+ clientVarNames.size()];
		// include mailing list variable names
		for (int i = 0; i < listVarNames.length; i++) {
			String variableName = listVarNames[i].toString();
			dataTypes[i] = new SelectItem(variableName);
		}
		// include client variable names
		for (int i = 0; i < clientVarNames.size(); i++) {
			String variableName = clientVarNames.get(i);
			dataTypes[i + listVarNames.length] = new SelectItem(variableName);
		}
		return dataTypes;
	}
	
	// PROPERTY: Email Template Id Items 
	public SelectItem[] getEmailTemplateIdItems() {
		List<EmailTemplate> list = getEmailTemplateService().getAll();
		SelectItem[] dataTypes = new SelectItem[list.size()];
		for (int i=0; i<list.size(); i++) {
			String templateId = list.get(i).getTemplateId();
			dataTypes[i] = new SelectItem(templateId);
		}
		return dataTypes;
	}
}
