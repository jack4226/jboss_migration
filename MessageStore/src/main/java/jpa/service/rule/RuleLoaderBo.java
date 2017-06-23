package jpa.service.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpa.constant.Constants;
import jpa.constant.RuleCategory;
import jpa.constant.RuleCriteria;
import jpa.constant.RuleType;
import jpa.constant.XHeaderName;
import jpa.model.SenderData;
import jpa.model.MailingList;
import jpa.model.ReloadFlags;
import jpa.model.rule.RuleElement;
import jpa.model.rule.RuleLogic;
import jpa.model.rule.RuleSubruleMap;
import jpa.service.common.EntityManagerService;
import jpa.service.common.ReloadFlagsService;
import jpa.service.common.SenderDataService;
import jpa.service.maillist.MailingListService;
import jpa.spring.util.SpringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component("ruleLoaderBo")
@Transactional(propagation=Propagation.REQUIRED)
public class RuleLoaderBo implements java.io.Serializable {
	private static final long serialVersionUID = 5251082728950956779L;
	static final Logger logger = Logger.getLogger(RuleLoaderBo.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	final List<RuleBase>[] mainRules;
	final List<RuleBase>[] preRules;
	final List<RuleBase>[] postRules;
	final Map<String, List<RuleBase>>[] subRules;
	final Set<String>[] subRuleNames;
	
	private static boolean isPrintRuleContents = true;

	private int currIndex = 0;
	@Autowired
	private RuleDataService ruleDataService;
	@Autowired
	private ReloadFlagsService flagsService;
	@Autowired
	private SenderDataService senderService;
	@Autowired
	private MailingListService listService;
	@Autowired
	private EntityManagerService emService;
	
	private int currIndex2 = 0;

	private Map<String, Pattern>[] patternMaps;
	
	private ReloadFlags reloadFlags;
	private long lastTimeLoaded;
	public static int INTERVAL = 5 * 60 * 1000; // 5 minutes

	@SuppressWarnings("unchecked")
	public RuleLoaderBo() {
		/*
		 * define place holders for two sets of rules
		 */
		mainRules = new List[2];
		preRules = new List[2];
		postRules = new List[2];
		subRules = new Map[2];
		subRuleNames = new Set[2];
		
		mainRules[0] = new ArrayList<RuleBase>();
		mainRules[1] = new ArrayList<RuleBase>();
		preRules[0] = new ArrayList<RuleBase>();
		preRules[1] = new ArrayList<RuleBase>();
		postRules[0] = new ArrayList<RuleBase>();
		postRules[1] = new ArrayList<RuleBase>();
		subRules[0] = new LinkedHashMap<String, List<RuleBase>>();
		subRules[1] = new LinkedHashMap<String, List<RuleBase>>();
		subRuleNames[0] = new HashSet<>();
		subRuleNames[1] = new HashSet<>();
	}
	
	public static void main(String[] args) {
		RuleLoaderBo loader = SpringUtil.getAppContext().getBean(RuleLoaderBo.class);
		SpringUtil.beginTransaction();
		try {
			loader.loadRules();
			loader.listRuleNames();
			SpringUtil.commitTransaction();
		}
		finally {
		}
	}

	public int getCurrIndex() {
		return currIndex;
	}

	@SuppressWarnings("unchecked")
	public synchronized void loadRules() {
		/*
		 * Two sets of rules are used in turns. When one of the set becomes active set,
		 * another set becomes inactive set. The rules getters always return rules from
		 * the active set. <br>
		 * When this method is called, it reloads rules from database and stores them in
		 * the inactive set. It then switch the inactive set to active before it quits.
		 */
		List<RuleLogic> ruleVos = ruleDataService.getCurrentRules();
		int newIndex = (currIndex + 1) % 2;
		loadRuleSets(ruleVos, newIndex);
		currIndex = newIndex;
		
		/*
		 * define place holder for two sets of pattern maps
		 */
		patternMaps = new LinkedHashMap[2];
		patternMaps[0] = loadAddressPatterns();
		patternMaps[1] = loadAddressPatterns();
		
		reloadFlags = flagsService.select();
		emService.detach(reloadFlags);
		
		lastTimeLoaded = System.currentTimeMillis();
	}
	
	private void reloadAddressPatterns() {
		/*
		 * Two sets of patterns are used in turns. When one of the set becomes active 
		 * set, another set becomes inactive set. The patterns getter always return
		 * patterns from the active set. <br>
		 * When this method is called, it reloads patterns from database and stores
		 * them in the inactive set. It then switch the inactive set to active before
		 * it quits.
		 */
		int newIndex = (currIndex2 + 1) % 2;
		patternMaps[newIndex] = loadAddressPatterns();
		currIndex2 = newIndex;
	}
	
	private synchronized void checkChangesAndPerformReload() {
		long currTime = System.currentTimeMillis();
		if (currTime > (lastTimeLoaded + INTERVAL)) {
			// check change flags and reload rule and address patterns
			ReloadFlags vo = flagsService.select();
			if (reloadFlags != null && vo != null) {
				if (reloadFlags.getRules() < vo.getRules() || reloadFlags.getActions() < vo.getActions()
						|| reloadFlags.getTemplates() < vo.getTemplates()) {
					logger.info("====== Rules and/or Actions changed, reload Rules ======");
					reloadFlags.setRules(vo.getRules());
					reloadFlags.setActions(vo.getActions());
					logger.info("Reloading all rules...");
					loadRules();
				}
				if (reloadFlags.getSenders() < vo.getSenders() || reloadFlags.getTemplates() < vo.getTemplates()) {
					logger.info("====== Senders/Templates changed, reload Address Patterns ======");
					reloadFlags.setSenders(vo.getSenders());
					logger.info("Reloading address patterns...");
					reloadAddressPatterns();
				}
				reloadFlags.setTemplates(vo.getTemplates());
			}
			lastTimeLoaded = currTime;
		}
	}
	
	private void loadRuleSets(List<RuleLogic> ruleLogics, int index) {
		mainRules[index].clear();
		preRules[index].clear();
		postRules[index].clear();
		subRules[index].clear();
		subRuleNames[index].clear();
		
		for (int i = 0; i < ruleLogics.size(); i++) {
			RuleLogic logic = ruleLogics.get(i);
			List<RuleBase> rules = createRules(logic);
			if (rules.isEmpty()) {
				continue;
			}
			
			if (RuleCategory.PRE_RULE.getValue().equals(logic.getRuleCategory())) {
				preRules[index].addAll(rules);
			}
			else if (RuleCategory.POST_RULE.getValue().equals(logic.getRuleCategory())) {
				postRules[index].addAll(rules);
			}
			else if (!(logic.isSubrule())) {
				mainRules[index].addAll(rules);
			}
			else {
				subRuleNames[index].add(logic.getRuleName());
			}
			
			// a non sub-rule could also be used as a sub-rule
			subRules[index].put(logic.getRuleName(), rules);
		}
	}
	
	private List<RuleBase> createRules(RuleLogic ruleLogic) {
		List<RuleBase> rules = new ArrayList<RuleBase>();
		List<RuleElement> elements = ruleLogic.getRuleElements();
		List<RuleSubruleMap> subruleMaps = ruleLogic.getRuleSubruleMaps();
		
		// build rules
		if (RuleType.SIMPLE.getValue().equals(ruleLogic.getRuleType()))	{
			for (int i=0; i<elements.size(); i++) {
				RuleElement element = elements.get(i);
				try {
					XHeaderName.getByValue(element.getHeaderName());
				}
				catch (IllegalArgumentException e) {
					logger.warn("loading custom X-Header name: " + e.getMessage());
				}
				RuleSimple ruleSimple = new RuleSimple(
					ruleLogic.getRuleName(),
					RuleType.getByValue(ruleLogic.getRuleType()),
					ruleLogic.getMailType(),
					element.getDataName(),
					element.getHeaderName(),
					RuleCriteria.getByValue(element.getCriteria()),
					element.isCaseSensitive(),
					element.getTargetTextAll(),
					element.getExclusionsAll(),
					element.getExclListProcName(),
					element.getDelimiter()
					);
				
				for (int j=0; j<subruleMaps.size(); j++) {
					RuleSubruleMap subRuleLogic = subruleMaps.get(j);
					ruleSimple.subruleList.add(subRuleLogic.getRuleSubruleMapPK().getSubruleLogic().getRuleName());
				}
				
				rules.add(ruleSimple);
			}
		}
		else { // all/any/none rule
			List<RuleBase> ruleList = new ArrayList<RuleBase>();
			for (int i=0; i<elements.size(); i++) {
				RuleElement element = elements.get(i);
				try {
					XHeaderName.getByValue(element.getHeaderName());
				}
				catch (IllegalArgumentException e) {
					logger.warn("loading custom X-Header name: " + e.getMessage());
				}
				RuleSimple ruleSimple = new RuleSimple(
					ruleLogic.getRuleName(),
					RuleType.getByValue(ruleLogic.getRuleType()),
					ruleLogic.getMailType(),
					element.getDataName(),
					element.getHeaderName(),
					RuleCriteria.getByValue(element.getCriteria()),
					element.isCaseSensitive(),
					element.getTargetTextAll(),
					element.getExclusionsAll(),
					element.getExclListProcName(),
					element.getDelimiter()
					);
				ruleList.add(ruleSimple);
			}

			RuleComplex ruleComplex = new RuleComplex(
					ruleLogic.getRuleName(),
					RuleType.getByValue(ruleLogic.getRuleType()),
					ruleLogic.getMailType(),
					ruleList
					);
			
			for (int j=0; j<subruleMaps.size(); j++) {
				RuleSubruleMap subruleMap = subruleMaps.get(j);
				ruleComplex.subruleList.add(subruleMap.getRuleSubruleMapPK().getSubruleLogic().getRuleName());
			}

			rules.add(ruleComplex);
		}
		
		return rules;
	}

	public List<RuleBase> getPreRuleSet() {
		checkChangesAndPerformReload();
		return preRules[currIndex];
	}

	public List<RuleBase> getRuleSet() {
		return mainRules[currIndex];
	}

	public List<RuleBase> getPostRuleSet() {
		return postRules[currIndex];
	}

	public Map<String, List<RuleBase>> getSubRuleSet() {
		return subRules[currIndex];
	}

	public void listRuleNames() {
		listRuleNames(System.out);
	}

	public void listRuleNames(java.io.PrintStream prt) {
		try {
			listRuleNames("Pre  Rule", getPostRuleSet(), prt);
			listRuleNames("Main Rule", getRuleSet(), prt);
			listRuleNames("Post Rule", getPostRuleSet(), prt);
			listRuleNames("Sub  Rule", getSubRuleSet(), prt);
		}
		catch (Exception e) {
			logger.error("Exception caught during ListRuleNames", e);
		}
	}

	private void listRuleNames(String ruleLit, List<RuleBase> rules, java.io.PrintStream prt) {
		Iterator<RuleBase> it = rules.iterator();
		while (it.hasNext()) {
			RuleBase r = it.next();
			String ruleName = StringUtils.rightPad(r.getRuleName(), 28, " ");
			prt.print("RuleLoaderBo.1 - " + ruleLit + ": " + ruleName);
			if (isPrintRuleContents) {
				prt.print(r.printRuleContent());
				prt.println();
			}
			listSubRuleNames(r.getSubRules(), prt);
			prt.println();
		}
	}

	private void listRuleNames(String ruleLit, Map<String, List<RuleBase>> rules, java.io.PrintStream prt) {
		Set<String> keys = rules.keySet();
		for (Iterator<String> it = keys.iterator(); it.hasNext();) {
			String key = it.next();
			if (subRuleNames[currIndex].contains(key)) {
				List<RuleBase> list = (List<RuleBase>) rules.get(key);
				for (RuleBase r : list) {
					String ruleName = StringUtils.rightPad(r.getRuleName(), 28, " ");
					prt.print("RuleLoaderBo.2 - " + ruleLit + ": " + ruleName);
					if (isPrintRuleContents) {
						prt.print(r.printRuleContent());
						prt.println();
					}
					listSubRuleNames(r.getSubRules(), prt);
					prt.println();
				}
			}
		}
	}

	private void listSubRuleNames(List<String> subRuleNames, java.io.PrintStream prt) {
		return;
	}
	
	public String findSenderIdByAddr(String addr) {
		if (StringUtils.isBlank(addr)) {
			return null;
		}
		Map<String, Pattern> patterns = getPatterns();
		Set<String> set = patterns.keySet();
		for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
			String key = it.next();
			Pattern pattern = patterns.get(key);
			if (pattern == null) { // should never happen
				String error = "Threading Error, Contact Programming!!!";
				logger.fatal(error, new Exception(error));
				continue;
			}
			Matcher m = pattern.matcher(addr);
			if (m.find()) {
				return key;
			}
		}
		return null;
	}
	
	private Map<String, Pattern> getPatterns() {
		if (patternMaps == null) {
			throw new IllegalStateException("Rules have not been loaded, please execute loadRules() first.");
		}
		return patternMaps[currIndex2];
	}
	
	private Map<String, Pattern> loadAddressPatterns() {
		Map<String, String> map = new LinkedHashMap<String, String>();
		// make sure the default sender is the first on the list
		SenderData sender0 = senderService.getBySenderId(Constants.DEFAULT_SENDER_ID);
		if (sender0 != null) {
			String senderId = sender0.getSenderId();
			String returnPath = buildReturnPath(sender0);
			map.put(senderId, returnPath);
		}
		List<SenderData> senders = senderService.getAll();
		// now add all other senders' return path
		for (SenderData sender : senders) {
			String senderId = sender.getSenderId();
			if (Constants.DEFAULT_SENDER_ID.equalsIgnoreCase(senderId)) {
				continue; // skip the default sender
			}
			String returnPath = buildReturnPath(sender);
			if (map.containsKey(senderId)) {
				map.put(senderId, map.get(senderId) + "|" + returnPath);
			}
			else {
				map.put(senderId, returnPath);
			}
		}
		// add mailing list addresses
		List<MailingList> lists = listService.getAll(true);
		for (MailingList list : lists) {
			String senderId = list.getSenderData().getSenderId();
			String returnPath = list.getListEmailAddr();
			if (map.containsKey(senderId)) {
				map.put(senderId, map.get(senderId) + "|" + returnPath);
			}
			else {
				map.put(senderId, returnPath);
			}
		}
		// create regular expressions
		Map<String, Pattern> patterns = new LinkedHashMap<String, Pattern>();
		Set<String> set = map.keySet();
		for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
			String key = it.next();
			String regex = map.get(key);
			logger.info(">>>>> Address Pathern: " + StringUtils.rightPad(key, 10, " ") + " -> " + regex);
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			patterns.put(key, pattern);
		}
		return patterns;
	}
	
	private String buildReturnPath(SenderData sender) {
		String domainName = StringUtils.trim(sender.getDomainName());
		String returnPath = StringUtils.trim(sender.getReturnPathLeft()) + "@" + domainName;
		if (sender.isVerpEnabled()) {
			// if VERP is enabled, add VERP addresses to the pattern 
			String verpSub = sender.getVerpSubDomain();
			verpSub = (StringUtils.isBlank(verpSub) ? "" : verpSub.trim() + ".");
			if (StringUtils.isNotBlank(sender.getVerpInboxName())) {
				returnPath += "|" + sender.getVerpInboxName().trim() + "@" + verpSub + domainName;
			}
			if (StringUtils.isNotBlank(sender.getVerpRemoveInbox())) {
				returnPath += "|" + sender.getVerpRemoveInbox().trim() + "@" + verpSub + domainName;
			}
		}
		if (sender.isUseTestAddr()) {
			// if in test mode, add test address to the pattern
			if (StringUtils.isNotBlank(sender.getTestFromAddr())) {
				returnPath += "|" + sender.getTestFromAddr().trim();
			}
			if (StringUtils.isNotBlank(sender.getTestReplytoAddr())) {
				returnPath += "|" + sender.getTestReplytoAddr().trim();
			}
			if (StringUtils.isNotBlank(sender.getTestToAddr())) {
				returnPath += "|" + sender.getTestToAddr().trim();
			}
		}
		return returnPath;
	}
}
