package jpa.model.rule;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name="rule_element", uniqueConstraints=@UniqueConstraint(columnNames = {"RuleLogicRowId", "elementSequence"}))
public class RuleElement extends BaseModel implements Serializable {
	private static final long serialVersionUID = -4142842697269887792L;

	@Embedded
	private RuleElementPK ruleElementPK;

	@Column(length=26, nullable=false)
	private String dataName = "";
	@Column(length=50, nullable=true)
	private String headerName = null;
	@Column(length=16, nullable=false)
	private String criteria = "";
	@Column(length=1, nullable=false, columnDefinition="boolean not null default false")
	private boolean isCaseSensitive = false;
	@Column(length=2000, nullable=true)
	private String targetText = null;
	@Column(length=100, nullable=true)
	private String targetProcName = null;
	@Column(length=8100, nullable=true)
	private String exclusions = null;
	@Column(length=100, nullable=true)
	private String exclListProcName = null;
	@Column(length=5, nullable=true, columnDefinition="char(5)")
	private String delimiter = null;

	@Transient
	private String exclusionsAll;
	@Transient
	private String targetTextAll;
	
	public RuleElement() {
		// must have a no-argument constructor
	}

	public RuleElementPK getRuleElementPK() {
		return ruleElementPK;
	}

	public void setRuleElementPK(RuleElementPK ruleElementPK) {
		this.ruleElementPK = ruleElementPK;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getCriteria() {
		return criteria;
	}

	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}

	public boolean isCaseSensitive() {
		return isCaseSensitive;
	}

	public void setCaseSensitive(boolean isCaseSensitive) {
		this.isCaseSensitive = isCaseSensitive;
	}

	public String getTargetText() {
		return targetText;
	}

	public void setTargetText(String targetText) {
		this.targetText = targetText;
	}

	public String getTargetProcName() {
		return targetProcName;
	}

	public void setTargetProcName(String targetProcName) {
		this.targetProcName = targetProcName;
	}

	public String getExclusions() {
		return exclusions;
	}

	public void setExclusions(String exclusions) {
		this.exclusions = exclusions;
	}

	public String getExclListProcName() {
		return exclListProcName;
	}

	public void setExclListProcName(String exclListProcName) {
		this.exclListProcName = exclListProcName;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	public String getExclusionsAll() {
		if (StringUtils.isBlank(exclusionsAll)) {
			return exclusions;
		}
		return exclusionsAll;
	}

	public void setExclusionsAll(String exclusionsAll) {
		this.exclusionsAll = exclusionsAll;
	}

	public String getTargetTextAll() {
		if (StringUtils.isBlank(targetTextAll)) {
			return targetText;
		}
		return targetTextAll;
	}

	public void setTargetTextAll(String targetTextAll) {
		this.targetTextAll = targetTextAll;
	}

}