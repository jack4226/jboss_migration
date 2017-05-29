package jpa.model.rule;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.model.SenderData;
import jpa.msgui.vo.TimestampAdapter;

@Embeddable
public class RuleActionPK implements Serializable {
	private static final long serialVersionUID = 5992598892836372267L;

	@ManyToOne(targetEntity=RuleLogic.class, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="RuleLogicRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private RuleLogic ruleLogic;
	
	@Column(nullable=false)
	private int actionSequence = 0;
	@Column(length=3, nullable=false)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp startTime;

	@ManyToOne(targetEntity=SenderData.class, fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="SenderDataRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=true)
	@XmlTransient
	private SenderData senderData;

	public RuleActionPK() {}
	
	public RuleActionPK(RuleLogic ruleLogic, int actionSequence, Timestamp startTime, SenderData senderData) {
		this.ruleLogic = ruleLogic;
		this.actionSequence = actionSequence;
		this.startTime = startTime;
		this.senderData = senderData;
	}

	public RuleLogic getRuleLogic() {
		return ruleLogic;
	}

	public void setRuleLogic(RuleLogic ruleLogic) {
		this.ruleLogic = ruleLogic;
	}

	public int getActionSequence() {
		return actionSequence;
	}

	public void setActionSequence(int actionSequence) {
		this.actionSequence = actionSequence;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}
}