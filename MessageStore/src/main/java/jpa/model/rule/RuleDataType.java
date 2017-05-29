package jpa.model.rule;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import jpa.model.BaseModel;

@Entity
@Table(name="rule_data_type")
public class RuleDataType extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -8077139332207748775L;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="ruleDataValuePK.ruleDataType", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<RuleDataValue> ruleDataValues;

	@Column(nullable=false, length=26, unique=true)
	private String dataType = "";
	@Column(nullable=true, length=100)
	private String description = null;

	public RuleDataType() {
		// must have a no-argument constructor
	}
	
	public RuleDataType(String dataType, String description) {
		this.dataType = dataType;
		this.description = description;
	}

	public RuleDataType(String dataType) {
		this.dataType = dataType;
	}

	public List<RuleDataValue> getRuleDataValues() {
		return ruleDataValues;
	}

	public void setRuleDataValues(List<RuleDataValue> ruleDataValues) {
		this.ruleDataValues = ruleDataValues;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
