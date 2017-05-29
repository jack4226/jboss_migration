package jpa.model.rule;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

@Embeddable
public class RuleElementPK implements Serializable {
	private static final long serialVersionUID = 4082282320803459127L;

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="RuleLogicRowId",insertable=true,referencedColumnName="row_id",nullable=false,
			foreignKey=@ForeignKey(name="FK_rule_element_RuleLogicRowId"))
	@XmlTransient
	private RuleLogic ruleLogic;
	
	@Column(name="elementSequence", nullable=false)
	private int elementSequence = -1;

	public RuleElementPK() {}
	
	public RuleElementPK(RuleLogic ruleLogic, int elementSequence) {
		this.ruleLogic = ruleLogic;
		this.elementSequence = elementSequence;
	}

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public int getElementSequence() {
		return elementSequence;
	}

	public void setElementSequence(int elementSequence) {
		this.elementSequence = elementSequence;
	}
}