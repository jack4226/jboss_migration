package jpa.service.rule;

import java.util.List;

import jpa.model.rule.RuleElement;
import jpa.model.rule.RuleLogic;
import jpa.service.external.TargetTextProc;
import jpa.spring.util.SpringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleDataService")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleDataService implements java.io.Serializable {
	private static final long serialVersionUID = 143089225642571641L;
	static final Logger logger = Logger.getLogger(RuleDataService.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private RuleLogicService logicService;

	public RuleDataService() {
	}
	
	public List<RuleLogic> getCurrentRules() {
		List<RuleLogic> rules = logicService.getActiveRules();
		substituteTargetText(rules);
		substituteExclusions(rules);
		return rules;
	}
	
	public RuleLogic getRuleByRuleName(String ruleName) {
		RuleLogic ruleVo = (RuleLogic) logicService.getByRuleName(ruleName);
		substituteTargetText(ruleVo);
		substituteExclusions(ruleVo);
		return ruleVo;
	}
	
	private void substituteTargetText(List<RuleLogic> rules) {
		if (rules == null || rules.size() == 0) return;
		for (RuleLogic rule : rules) {
			substituteTargetText(rule);
		}
	}
	
	private void substituteTargetText(RuleLogic rule) {
		if (rule == null) return;
		List<RuleElement> elements = rule.getRuleElements();
		if (elements == null || elements.isEmpty()) return;
		for (RuleElement element : elements) {
			if (element.getTargetProcName() == null) continue;
			Object obj = null;
			try { // a TargetProc could be a class name or a bean id
				obj = Class.forName(element.getTargetProcName()).newInstance();
				logger.info("Loaded class " + element.getTargetProcName() + " for rule "
						+ rule.getRuleName());
			}
			catch (Exception e) { // not a class name, try load it as a Bean
				try {
					obj = SpringUtil.getAppContext().getBean(element.getTargetProcName());
					logger.info("Loaded bean " + element.getTargetProcName() + " for rule "
							+ rule.getRuleName());
				}
				catch (Exception e2) {
					logger.warn("Failed to load: " + element.getTargetProcName() + " for rule "
							+ rule.getRuleName());
				}
				if (obj == null) continue;
			}
			try {
				String text = null;
				if (obj instanceof TargetTextProc) {
					TargetTextProc bo = (TargetTextProc) obj;
					text = bo.process();
				}
				if (StringUtils.isNotBlank(text)) {
					logger.info("Changing Target Text for rule: " + rule.getRuleName());
					logger.info("  From: " + element.getTargetText());
					logger.info("    To: " + text);
					element.setTargetTextAll(text);
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				throw new RuntimeException(e.toString());
			}
		}
	}
	
	private void substituteExclusions(List<RuleLogic> rules) {
		if (rules == null || rules.isEmpty()) return;
		for (RuleLogic rule : rules) {
			substituteExclusions(rule);
		}
	}
	
	private void substituteExclusions(RuleLogic rule) {
		List<RuleElement> elements = rule.getRuleElements();
		if (elements == null || elements.isEmpty()) return;
		for (RuleElement element : elements) {
			if (element.getExclListProcName() == null) continue;
			Object obj = null;
			try {
				obj = SpringUtil.getAppContext().getBean(element.getExclListProcName());
			}
			catch (Exception e) {
				logger.error("Failed to load bean: " + element.getExclListProcName() + " for rule "
						+ rule.getRuleName());
			}
			try {
				String text = null;
				if (obj instanceof TargetTextProc) {
					TargetTextProc bo = (TargetTextProc) obj;
					text = bo.process();
				}
				if (StringUtils.isNotBlank(text)) {
					logger.info("Appending Exclusion list for rule: " + rule.getRuleName());
					logger.info("  Exclusion List: " + text);
					String delimiter = element.getDelimiter();
					if (delimiter == null || delimiter.length() == 0) {
						delimiter = ",";
					}
					String origText = element.getExclusions();
					if (StringUtils.isNotBlank(origText)) {
						origText = origText + delimiter;
					}
					else {
						origText = "";
					}
					element.setExclusionsAll(origText + text);
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
				throw new RuntimeException(e.toString());
			}
		}
	}
	
	public static void main(String[] args) {
		RuleDataService bo = SpringUtil.getAppContext().getBean(RuleDataService.class);
		bo.getCurrentRules();
	}
}
