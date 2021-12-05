package jpa.service.rule;

import java.util.List;

import jpa.constant.RuleCriteria;
import jpa.constant.RuleDataName;
import jpa.constant.RuleType;
import jpa.message.MessageBean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuleComplex extends RuleBase {
	private static final long serialVersionUID = -3559383665410520477L;
	protected static final Logger logger = LogManager.getLogger(RuleComplex.class);
	
	private final List<RuleBase> ruleList;
	
	public RuleComplex(String _ruleName,
			RuleType _ruleType, 
			String _mailType, 
			List<RuleBase> _rule_list) {
		super(_ruleName, _ruleType, _mailType, "", null, RuleCriteria.EQUALS, false);
		this.ruleList = _rule_list;
		logger.info(">>>>> Complex-Rule initialized for " + ruleName);
	}

	public List<RuleBase> getRuleList() {
		return ruleList;
	}

	public String match(String mail_type, Object mail_obj) {
		if (mail_type==null || !mail_type.equals(mailType))
			return null;

		if (isDebugEnabled) {
			logger.debug("BEGIN - [" + getRuleName(20) + "] [" + mailType + "] rule_type: ["
					+ ruleType + "]");
		}
		if (RuleType.ALL.equals(ruleType)) {
			for (int i = 0; i < ruleList.size(); i++) {
				RuleBase rule = (RuleBase) ruleList.get(i);
				String match = match(mail_obj, rule, mail_type);
				if (match==null) {
					return returnToCaller(null);
				}
				else {
					dataName += rule.getDataName();
				}
			}
			return returnToCaller(ruleName);
		}
		else if (RuleType.ANY.equals(ruleType)) {
			for (int i = 0; i < ruleList.size(); i++) {
				RuleBase rule = (RuleBase) ruleList.get(i);
				String match = match(mail_obj, rule, mail_type);
				if (match!=null) {
					dataName += rule.getDataName();
					return returnToCaller(ruleName);
				}
			}
			return returnToCaller(null);
		}
		else if (RuleType.NONE.equals(ruleType)) {
			for (int i = 0; i < ruleList.size(); i++) {
				RuleBase rule = (RuleBase) ruleList.get(i);
				String match = match(mail_obj, rule, mail_type);
				if (match!=null) {
					return returnToCaller(null);
				}
				else {
					dataName += rule.getDataName();
				}
			}
			return returnToCaller(ruleName);
		}
		// unknown rule type
		logger.error("match() - unknown rule type: " + ruleType);
		return returnToCaller(null);
	}

	private String match(Object mail_obj, RuleBase rule, String mail_type) {
		
		String data = null;
		if (mail_obj instanceof MessageBean) {
			data = RuleMatcher.getFieldData(rule, (MessageBean) mail_obj);
		}
		String ruleName = rule.match(mail_type, rule.getDataName(), data);
		// now check attachment rules
		if (ruleName == null 
				&& (RuleDataName.MIME_TYPE.getValue().equals(rule.getDataName()) 
					|| RuleDataName.FILE_NAME.getValue().equals(rule.getDataName()))) {
			ruleName = RuleMatcher.matchMimeTypes((MessageBean)mail_obj, rule);
		}
		
		return ruleName;
	}
	
	private String returnToCaller(String rule_name) {
		if (isDebugEnabled) {
			logger.debug("END - [" + getRuleName(20) + "] ruleName [" + rule_name + "]");
		}
		return rule_name;
	}
	
	public String match(String mail_type, String data_type, String data) {
		// dummy implementation satisfying the super class
		return null;
	}
}