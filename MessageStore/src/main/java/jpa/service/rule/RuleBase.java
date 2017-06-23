package jpa.service.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jpa.constant.RuleCriteria;
import jpa.constant.RuleType;
import jpa.util.StringUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public abstract class RuleBase implements java.io.Serializable {
	private static final long serialVersionUID = -2619176738651938695L;
	protected static final Logger logger = Logger.getLogger(RuleBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();

	final static String LF = System.getProperty("line.separator", "\n");

	// store rule names found in rules.xml
	private final static Set<String> ruleNameList = Collections.synchronizedSet(new HashSet<String>());

	protected final String ruleName;
	protected final RuleType ruleType;
	protected String dataName;
	protected String headerName;
	protected final String mailType;
	protected final RuleCriteria criteria;
	protected final boolean isCaseSensitive;

	protected final List<String> subruleList = new ArrayList<String>();

	public RuleBase(String _ruleName, 
			RuleType _ruleType, 
			String _mailType, 
			String _dataName,
			String _headerName,
			RuleCriteria _criteria, 
			boolean _caseSensitive) {
		this.ruleName = _ruleName;
		this.ruleType = _ruleType;
		this.mailType = _mailType;
		this.dataName = _dataName;
		this.headerName = _headerName;
		this.criteria = _criteria;
		this.isCaseSensitive = _caseSensitive;
		if (this.ruleName != null && !ruleNameList.contains(this.ruleName))
			ruleNameList.add(this.ruleName);
	}
	
	public String getRuleName() {
		return ruleName;
	}

	protected String getRuleName(int len) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<len-ruleName.length(); i++)
			sb.append(" ");
		return ruleName + sb.toString();
	}
	
	public RuleType getRuleType() {
		return ruleType;
	}

	public String getDataName() {
		return dataName;
	}

	public String getHeaderName() {
		return headerName;
	}

	public List<String> getSubRules() {
		return subruleList;
	}

	public String getMailType() {
		return mailType;
	}

	public RuleCriteria getCriteria() {
		return criteria;
	}

	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	public String printRuleContent() {
		return printRuleContent(0);
	}

	public String printRuleContent(int level) {
		String dots = getBlanks(level) + StringUtil.getDots(level);
		StringBuffer sb = new StringBuffer();
		sb.append(LF + dots + "---- listing rule content for " + ruleName + " ----" + LF);
		if (dots.length()==0) {
			sb.append(dots + "Rule Name : " + ruleName + LF);
			sb.append(dots + "Rule Type : " + ruleType.getValue() + LF);
			sb.append(dots + "Mail Type : " + mailType + LF);
		}
		sb.append(dots + "Data Name : " + dataName + LF);
		if (headerName != null) {
			sb.append(dots + "Header Name: " + headerName + LF);
		}
		sb.append(dots + "Criteria  : " + criteria.getValue() + LF);
		sb.append(dots + "Case Sensitive : " + isCaseSensitive + LF);
		if (this instanceof RuleSimple) {
			if (StringUtils.isNotBlank(((RuleSimple)this).getTargetText())) {
				sb.append(dots + "Target Text : " + ((RuleSimple)this).getTargetText() + LF);
			}if (((RuleSimple)this).getStoredProcedure() != null) {
				sb.append(dots + "Stored Procedure: " + ((RuleSimple)this).getStoredProcedure() + LF);
			}
			if (((RuleSimple)this).getExclusionList() != null) {
				sb.append(dots + "Exclusion List:" + LF);
				for (int i = 0; i < ((RuleSimple)this).getExclusionList().size(); i++) {
					sb.append("     " + dots + ((RuleSimple)this).getExclusionList().get(i) + LF);
				}
			}
		}
		if (subruleList != null) {
			sb.append(dots + "SubRule List:" + LF);
			for (int i = 0; i < subruleList.size(); i++) {
				sb.append(dots + "     " + subruleList.get(i) + LF);
			}
		}
		if (this instanceof RuleComplex) {
			sb.append(dots + "Rule Category: Complex" + LF);
			for (RuleBase rule :((RuleComplex)this).getRuleList()) {
				sb.append(dots + rule.printRuleContent(level+1));
			}
		}
		else if (this instanceof RuleSimple) {
			sb.append(dots + "Rule Category: Simple" + LF);
		}

		return sb.toString();
	}
	
	private String getBlanks(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<level; i++) {
			sb.append("   ");
		}
		return sb.toString();
	}

	public abstract String match(String mail_type, String data_type, String data);
	
	public abstract String match(String mail_type, Object mail_obj);
}