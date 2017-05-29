package jpa.model.rule;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

@Entity
@Table(name="rule_data_value", uniqueConstraints=@UniqueConstraint(columnNames = {"RuleDataTypeRowId", "dataValue"}))
public class RuleDataValue extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -6383445491730691533L;

	@Embedded
	private RuleDataValuePK ruleDataValuePK;
	
	@Column(nullable=true, length=255)
	private String otherProps = null;

	public RuleDataValue() {
		// must have a no-argument constructor
	}
	
	public RuleDataValue(RuleDataValuePK ruleDataValuePK, String otherProps) {
		this.ruleDataValuePK = ruleDataValuePK;
		this.otherProps = otherProps;
	}

	public RuleDataValuePK getRuleDataValuePK() {
		return ruleDataValuePK;
	}

	public void setRuleDataValuePK(RuleDataValuePK ruleDataValuePK) {
		this.ruleDataValuePK = ruleDataValuePK;
	}

	public String getOtherProps() {
		return otherProps;
	}

	public void setOtherProps(String otherProps) {
		this.otherProps = otherProps;
	}
}
