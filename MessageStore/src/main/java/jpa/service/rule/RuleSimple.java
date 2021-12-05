package jpa.service.rule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jpa.constant.RuleCriteria;
import jpa.constant.RuleType;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RuleSimple extends RuleBase {
	private static final long serialVersionUID = -1386955504774162841L;
	protected static final Logger logger = LogManager.getLogger(RuleSimple.class);
	
	final String targetText;
	
	private final String storedProcedure;
	private List<String> exclusionList = null;
	private Set<String> exclusionSet = null;
	private final Pattern pattern;
	
	public RuleSimple(String _ruleName, 
			RuleType _ruleType, 
			String _mailType, 
			String _dataName,
			String _headerName,
			RuleCriteria _criteria, 
			boolean _is_case_sensitive,
			String _targetText, 
			String _exclusion_list,
			String _stored_procedure,
			String _delimiter) {
		super(_ruleName, _ruleType, _mailType, _dataName, _headerName, _criteria, _is_case_sensitive);
		if (RuleCriteria.REG_EX.equals(_criteria)) {
			if (isCaseSensitive) {
				// enables dotall mode
				pattern = Pattern.compile(_targetText, Pattern.DOTALL);
			}
			else {
				// enables case-insensitive and dotall mode
				pattern = Pattern.compile(_targetText, Pattern.CASE_INSENSITIVE
						| Pattern.DOTALL);
			}
		}
		else {
			pattern = null;
			if (!isCaseSensitive) {
				_targetText = StringUtils.lowerCase(_targetText);
			}
		}
		this.targetText = _targetText;
		this.storedProcedure = _stored_procedure;
		setExclusionList(_exclusion_list, _delimiter);
		logger.info(">>>>> Simple-Rule initialized for " + ruleName);
	}

	public String getTargetText() {
		return targetText;
	}

	public String getStoredProcedure() {
		return storedProcedure;
	}

	public List<String> getExclusionList() {
		return exclusionList;
	}

	private void setExclusionList(String _exclusionList, String _delimiter) {
		if (_exclusionList != null && _delimiter!=null) {
			exclusionList = new ArrayList<String>();
			StringTokenizer st = new StringTokenizer(_exclusionList, _delimiter);
			while (st.hasMoreTokens()) {
				exclusionList.add(st.nextToken());
			}
			
			if (!isCaseSensitive) {
				// convert the list to lower case
				for (int i = 0; i < this.exclusionList.size(); i++) {
					String str = (String) this.exclusionList.get(i);
					if (str != null) {// just for safety
						this.exclusionList.set(i, str.toLowerCase());
					}
				}
			}
			
			logger.info("----- Exclusion List for Rule: " + ruleName + ", type: " + mailType);
			for (int i = 0; i < this.exclusionList.size(); i++) {
				logger.info("      " + this.exclusionList.get(i));
			}
			
			//this.exclusionSet = Collections.synchronizedSet(new HashSet(this.exclusionList));
			this.exclusionSet = new HashSet<String>(this.exclusionList);
		}
	}

	public String match(String mail_type, String data_type, String data) {
		if (mail_type==null || !mail_type.equals(mailType)) {
			return null;
		}
		if (data_type==null || !data_type.equals(dataName)) {
			return null;
		}
		
		if (data == null) {
			data = ""; // just for safety
		}
		if (!isCaseSensitive) {
			data = data.toLowerCase();
		}
		if (isDebugEnabled) {
			logger.debug("[" + getRuleName(20) + "] [" + mailType + "] [" + data_type + "] data: ["
					+ data + "] targetText: [" + targetText + "]");
		}
		boolean criteria_met = false;

		if (criteria.equals(RuleCriteria.STARTS_WITH)) {
			if (data.startsWith(targetText)) {
				criteria_met = true;
			}
		}
		else if (criteria.equals(RuleCriteria.ENDS_WITH)) {
			if (data.endsWith(targetText)) {
				criteria_met = true;
			}
		}
		else if (criteria.equals(RuleCriteria.CONTAINS)) {
			if (data.indexOf(targetText) >= 0) {
				criteria_met = true;
			}
		}
		else if (criteria.equals(RuleCriteria.EQUALS)) {
			if (data.equals(targetText)) {
				criteria_met = true;
			}
		}
		else if (criteria.equals(RuleCriteria.GREATER_THAN)) {
			if (data.compareTo(targetText)>0) {
				criteria_met = true;
			}
		}
		else if (criteria.equals(RuleCriteria.LESS_THAN)) {
			if (data.compareTo(targetText)<0) {
				criteria_met = true;
			}
		}
		else if (criteria.equals(RuleCriteria.IS_NOT_BLANK)) {
			if (StringUtils.isNotBlank(data)) {
				criteria_met = true;
			}
		}
		else if (criteria.equals(RuleCriteria.IS_BLANK)) {
			if (StringUtils.isBlank(data)) {
				criteria_met = true;
			}
		}
		else if (criteria.equals(RuleCriteria.REG_EX)) {
			Matcher matcher = pattern.matcher(data);
			if (matcher.find()) {
				criteria_met = true;
			}
		}

		if (criteria_met) {
			// is the data listed on exclusion list?
			if (exclusionSet != null && exclusionSet.contains(data)) {
				return null; // the data is on exclusion list
			}
			else {
				return ruleName;
			}
		}
		else {
			return null;
		}
	}

	public String match(String mail_type, Object mail_obj) {
		// dummy implementation satisfying the super class
		return null;
	}
}