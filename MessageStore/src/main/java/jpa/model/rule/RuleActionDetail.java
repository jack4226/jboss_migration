package jpa.model.rule;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import jpa.model.BaseModel;

@Entity
@Table(name="rule_action_detail")
public class RuleActionDetail extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -7004743275045358426L;

	@ManyToOne(targetEntity=RuleDataType.class, fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="RuleDataTypeRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=true)
	@XmlTransient
	private RuleDataType ruleDataType;

	@Column(nullable=false, length=26, unique=true)
	private String actionId = "";
	@Column(nullable=true, length=100)
	private String description = null;
	@Column(nullable=false, length=50)
	private String serviceName = "";
	@Column(nullable=true, length=255)
	private String className = null;

	public RuleActionDetail() {
		// must have a no-argument constructor
	}

	public RuleActionDetail(RuleDataType ruleDataType, String actionId,
			String descriprion, String serviceName, String classNama) {
		this.ruleDataType = ruleDataType;
		this.actionId = actionId;
		this.description = descriprion;
		this.serviceName = serviceName;
		this.className = classNama;
	}
 
	public RuleDataType getRuleDataType() {
		return ruleDataType;
	}

	public void setRuleDataType(RuleDataType ruleDataType) {
		this.ruleDataType = ruleDataType;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}
}
