package jpa.model.rule;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

@Embeddable
public class RuleDataValuePK implements Serializable {
	private static final long serialVersionUID = -355018168711050034L;

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="RuleDataTypeRowId",insertable=true,referencedColumnName="row_id",nullable=false)
	@XmlTransient
	private RuleDataType ruleDataType;
	
	@Column(nullable=false, length=100)
	private String dataValue = "";

	public RuleDataValuePK() {}
	
	public RuleDataValuePK(RuleDataType ruleDataType, String dataValue) {
		this.ruleDataType = ruleDataType;
		this.dataValue = dataValue;
	}

	public RuleDataType getRuleDataType() {
		return ruleDataType;
	}

	public void setRuleDataType(RuleDataType ruleDataType) {
		this.ruleDataType = ruleDataType;
	}

	public String getDataValue() {
		return dataValue;
	}

	public void setDataValue(String dataValue) {
		this.dataValue = dataValue;
	}

}