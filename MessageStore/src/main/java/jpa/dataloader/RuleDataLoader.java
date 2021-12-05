package jpa.dataloader;

import java.sql.Timestamp;

import jpa.constant.Constants;
import jpa.constant.RuleCategory;
import jpa.constant.StatusId;
import jpa.data.preload.RuleElementEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.data.preload.RuleSubruleMapEnum;
import jpa.model.rule.RuleElement;
import jpa.model.rule.RuleElementPK;
import jpa.model.rule.RuleLogic;
import jpa.model.rule.RuleSubruleMap;
import jpa.model.rule.RuleSubruleMapPK;
import jpa.service.rule.RuleElementService;
import jpa.service.rule.RuleLogicService;
import jpa.service.rule.RuleSubruleMapService;
import jpa.spring.util.SpringUtil;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuleDataLoader extends AbstractDataLoader {
	static final Logger logger = LogManager.getLogger(RuleDataLoader.class);
	private RuleLogicService service;
	private RuleElementService elementService;
	private RuleSubruleMapService mapService;

	public static void main(String[] args) {
		RuleDataLoader loader = new RuleDataLoader();
		loader.loadData();
	}

	@Override
	public void loadData() {
		service = SpringUtil.getAppContext().getBean(RuleLogicService.class);
		elementService = SpringUtil.getAppContext().getBean(RuleElementService.class);
		mapService = SpringUtil.getAppContext().getBean(RuleSubruleMapService.class);
		startTransaction();
		try {
			loadBuiltInRules();
			loadCustomRules();
			loadSubrules();
			loadRuleElements();
			loadRuleSubruleMaps();
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			commitTransaction();
		}
	}

	private void loadBuiltInRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		// built-in rules
		int ruleSeq = 100;
		for (RuleNameEnum ruleName : RuleNameEnum.getBuiltinRules()) {
			RuleLogic data = new RuleLogic();
			data.setRuleName(ruleName.getValue());
			data.setEvalSequence(++ruleSeq);
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(RuleCategory.MAIN_RULE.getValue());
			data.setSubrule(false);
			data.setBuiltinRule(true);
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		// end of built-in rules

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadCustomRules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		// Custom Rules
		int ruleSeq = 200;
		for (RuleNameEnum ruleName : RuleNameEnum.getCustomRules()) {
			RuleLogic data = new RuleLogic();
			data.setRuleName(ruleName.getValue());
			if (RuleNameEnum.UNATTENDED_MAILBOX.equals(ruleName)) {
				data.setEvalSequence(0);
			}
			else {
				data.setEvalSequence(++ruleSeq);
			}
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(ruleName.getRuleCategory().getValue());
			data.setSubrule(false);
			data.setBuiltinRule(false);
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		logger.info("EntityManager persisted the record.");
	}

	private void loadSubrules() {
		Timestamp startTime = new Timestamp(System.currentTimeMillis());
		// Built-in Sub Rules
		int ruleSeq = 225;
		for (RuleNameEnum ruleName : RuleNameEnum.getSubRules()) {
			RuleLogic data = new RuleLogic();
			data.setRuleName(ruleName.getValue());
			if (RuleNameEnum.UNATTENDED_MAILBOX.equals(ruleName)) {
				data.setEvalSequence(0);
			}
			else {
				data.setEvalSequence(++ruleSeq);
			}
			data.setRuleType(ruleName.getRuleType().getValue());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setStartTime(startTime);
			data.setMailType(Constants.SMTP_MAIL);
			data.setRuleCategory(ruleName.getRuleCategory().getValue());
			data.setSubrule(true);
			data.setBuiltinRule(true);
			data.setDescription(ruleName.getDescription());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			service.insert(data);
		}
		
		logger.info("EntityManager persisted the record.");
	}
	
	private void loadRuleElements() {
		for (RuleElementEnum elm : RuleElementEnum.values()) {
			RuleLogic logic = service.getByRuleName(elm.getRuleName().getValue());
			RuleElement data = new RuleElement();
			RuleElementPK pk = new RuleElementPK(logic,elm.getRuleSequence());
			data.setRuleElementPK(pk);
			data.setDataName(elm.getRuleDataName().getValue());
			if (elm.getXheaderName() != null) {
				data.setHeaderName(elm.getXheaderName().value());
			}
			data.setCriteria(elm.getRuleCriteria().getValue());
			data.setCaseSensitive(elm.isCaseSensitive());
			data.setTargetText(elm.getTargetText());
			data.setTargetProcName(elm.getTargetProcName());
			data.setExclusions(elm.getExclusions());
			data.setExclListProcName(elm.getExclListProcName());
			data.setDelimiter(elm.getDelimiter());
			elementService.insert(data);
		}

		logger.info("EntityManager persisted the record.");
	}
	
	private void loadRuleSubruleMaps() {
		for (RuleSubruleMapEnum map : RuleSubruleMapEnum.values()) {
			RuleLogic rule = service.getByRuleName(map.getRuleName().getValue());
			RuleLogic subrule = service.getByRuleName(map.getSubruleName().getValue());
			RuleSubruleMap data = new RuleSubruleMap();
			RuleSubruleMapPK pk1 = new RuleSubruleMapPK(rule,subrule);
			data.setRuleSubruleMapPK(pk1);
			data.setSubruleSequence(map.getSequence());
			data.setStatusId(StatusId.ACTIVE.getValue());
			data.setUpdtUserId(Constants.DEFAULT_USER_ID);
			mapService.insert(data);
		}
		
		logger.info("EntityManager persisted the record.");
	}
}

