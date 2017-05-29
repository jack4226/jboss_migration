package jpa.test.rule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import jpa.constant.RuleCategory;
import jpa.constant.RuleCriteria;
import jpa.constant.RuleType;
import jpa.data.preload.RuleElementEnum;
import jpa.data.preload.RuleNameEnum;
import jpa.data.preload.RuleSubruleMapEnum;
import jpa.service.common.ReloadFlagsService;
import jpa.service.external.MailingListTargetText;
import jpa.service.external.PostmasterTargetText;
import jpa.service.rule.RuleBase;
import jpa.service.rule.RuleComplex;
import jpa.service.rule.RuleLoaderBo;
import jpa.service.rule.RuleSimple;
import jpa.spring.util.BoTestBase;

public class RuleLoaderBoTest extends BoTestBase {
	final static String LF = System.getProperty("line.separator", "\n");
	static final Logger logger = Logger.getLogger(RuleLoaderBoTest.class);
	
	@Resource
	private RuleLoaderBo loader;
	@Resource
	PostmasterTargetText targetTextProc;
	@Resource
	MailingListTargetText mailingListProc;
	@Resource
	ReloadFlagsService flagService;
	
	@BeforeClass
	public static void RuleLoaderPrepare() {
	}

	@Test
	public void testRuleLoaderBo() {
		loader.loadRules();
		loader.listRuleNames();
		
		loadAndVerifyRules();
		int index1 = loader.getCurrIndex();
		
		flagService.updateRuleReloadFlag();
		flagService.updateTemplateReloadFlag();

		RuleLoaderBo.INTERVAL = 1000;
		try {
			Thread.sleep(RuleLoaderBo.INTERVAL+100);
		}
		catch (InterruptedException e) {
			logger.warn("InterruptedException caught: " + e.getMessage());
		}

		loadAndVerifyRules(); // should trigger rule reload
		int index2 = loader.getCurrIndex();
		assertFalse(index1 == index2);
	}
	
	private void loadAndVerifyRules() {
		List<RuleBase> preRules = loader.getPreRuleSet();
		assertEquals(RuleNameEnum.getPreRules().size(), preRules.size());

		List<RuleBase> mainRules = loader.getRuleSet();

		List<RuleBase> postRules = loader.getPostRuleSet();
		assertEquals(RuleNameEnum.getPostRules().size(), postRules.size());

		Map<String, List<RuleBase>> subRules = loader.getSubRuleSet();
		assertTrue(RuleNameEnum.getSubRules().size() <= subRules.size());
		
		// verify preRules
		assertTrue(preRules.size() > 0);
		for (RuleBase pre_rule : preRules) {
			verifyRule(pre_rule, RuleCategory.PRE_RULE, subRules);
		}
		// verify postRules
		assertTrue(postRules.size() > 0);
		for (RuleBase post_rule : postRules) {
			verifyRule(post_rule, RuleCategory.POST_RULE, subRules);
		}
		// verify mainRules
		assertTrue(mainRules.size() > 0);
		for (RuleBase main_rule : mainRules) {
			verifyRule(main_rule, RuleCategory.MAIN_RULE, subRules);
		}
	}

	private void verifyRule(RuleBase rule, RuleCategory category, Map<String, List<RuleBase>> subRules) {
		RuleNameEnum ruleEnum = RuleNameEnum.getByValue(rule.getRuleName());
		logger.info("ruleEnum: " + ruleEnum.getRuleCategory().name() + "/" + ruleEnum.getRuleCategory().getValue());
		logger.info("Category: " + category.name() + "/" + category.getValue());
		assertEquals(ruleEnum.getRuleCategory(), category);
		List<RuleElementEnum> elemsEnum = RuleElementEnum.getByRuleName(ruleEnum);
		if (!RuleType.SIMPLE.equals(ruleEnum.getRuleType())) {
			assertTrue(rule instanceof RuleComplex);
			verifyRuleComplex((RuleComplex)rule);
		}
		else {
			assertTrue(rule instanceof RuleSimple);
			for (RuleElementEnum elemEnum : elemsEnum) {
				verifyRuleSimple((RuleSimple) rule, elemEnum);
			}
		}
		// verify sub-rules
		List<RuleSubruleMapEnum> ruleSubRules = RuleSubruleMapEnum.getByRuleName(ruleEnum);
		assertEquals(ruleSubRules.size(), rule.getSubRules().size());
		for (RuleSubruleMapEnum subrule : ruleSubRules) {
			String subruleName = rule.getSubRules().get(subrule.getSequence());
			logger.info("Subrule Name: " + subruleName);
			assertEquals(subrule.getSubruleName().getValue(), subruleName);
			assertTrue(subRules.containsKey(subruleName));
			List<RuleBase> rule_sub_rules = subRules.get(subruleName);
			List<RuleElementEnum> enum_sub_rules = RuleElementEnum.getByRuleName(RuleNameEnum.getByValue(subruleName));
			int rule_sub_rules_size = 0;
			for (RuleBase rule_sub_rule : rule_sub_rules) {
				if (rule_sub_rule instanceof RuleComplex) {
					rule_sub_rules_size += ((RuleComplex)rule_sub_rule).getRuleList().size();
					verifyRuleComplex((RuleComplex)rule_sub_rule);
				}
				else {
					rule_sub_rules_size++;
					for (RuleElementEnum enum_sub_rule : enum_sub_rules) {
						verifyRuleSimple((RuleSimple)rule_sub_rule, enum_sub_rule);
					}
				}
			}
			assertEquals(enum_sub_rules.size(), rule_sub_rules_size);
		}
	}

	private void verifyRuleComplex(RuleComplex ruleComplex) {
		String ruleName = ruleComplex.getRuleName();
		RuleNameEnum ruleEnum = RuleNameEnum.getByValue(ruleName);
		RuleType type = ruleComplex.getRuleType();
		assertEquals(ruleEnum.getRuleType(), type);

		List<RuleElementEnum> elemsEnum = RuleElementEnum.getByRuleName(ruleEnum);
		assertFalse(elemsEnum.isEmpty());
		List<RuleBase> elements = ruleComplex.getRuleList();
		assertEquals(elemsEnum.size(), elements.size());
		
		for (RuleElementEnum elemEnum : elemsEnum) {
			RuleBase element = elements.get(elemEnum.getRuleSequence()-1);
			assertTrue(element instanceof RuleSimple);
			verifyRuleSimple((RuleSimple) element, elemEnum);
		}
	}
	
	private void verifyRuleSimple(RuleSimple ruleSimple, RuleElementEnum elemEnum) {
		RuleCriteria criteria = ruleSimple.getCriteria();
		assertEquals(elemEnum.getRuleCriteria(), (criteria));
		assertEquals(ruleSimple.getDataName(), (elemEnum.getRuleDataName().getValue()));
		if (elemEnum.getXheaderName() != null) {
			assertEquals(ruleSimple.getHeaderName(), (elemEnum.getXheaderName().value()));
		}
		assertEquals(ruleSimple.isCaseSensitive(), elemEnum.isCaseSensitive());
		if (StringUtils.isNotBlank(elemEnum.getTargetText())) {
			logger.info("Rule TargetText: " + ruleSimple.getTargetText());
			logger.info("Enum TargetText: " + elemEnum.getTargetText());
			if (StringUtils.isBlank(elemEnum.getTargetProcName())) {
				if (ruleSimple.getCriteria().equals(RuleCriteria.REG_EX)) {
					assertTrue(ruleSimple.getTargetText().equals(elemEnum.getTargetText()));
				}
				else {
					if (ruleSimple.isCaseSensitive()) {
						assertEquals(ruleSimple.getTargetText(), (elemEnum.getTargetText()));
					}
					else {
						assertEquals(ruleSimple.getTargetText(), (elemEnum.getTargetText().toLowerCase()));
					}
				}
			}
			else {
				String targetText = mailingListProc.process();
				logger.info("Proc TargetText: " + targetText);
				assertEquals(ruleSimple.getTargetText(), (targetText));
			}
		}
		if (StringUtils.isNotBlank(elemEnum.getExclListProcName())) {
			assertEquals(ruleSimple.getStoredProcedure(), elemEnum.getExclListProcName());
		}
		if (StringUtils.isNotBlank(elemEnum.getExclusions())) {
			StringBuffer sb = new StringBuffer();
			for (String exclusion : ruleSimple.getExclusionList()) {
				if (sb.length()>0) {
					sb.append(elemEnum.getDelimiter());
				}
				sb.append(exclusion);
			}
			logger.info("Rule Exclusions: " + sb.toString());
			logger.info("Enum Exclusions: " + elemEnum.getExclusions());
			String enumExcl = elemEnum.getExclusions();
			if (StringUtils.isNotBlank(elemEnum.getExclListProcName())) {
				String exclusionText = targetTextProc.process();
				logger.info("Proc Exclusions: " + exclusionText);
				enumExcl += elemEnum.getDelimiter() + exclusionText;
			}
			if (elemEnum.isCaseSensitive()==false) {
				enumExcl = enumExcl.toLowerCase();
			}
			assertEquals(enumExcl, sb.toString());
		}
	}
}
