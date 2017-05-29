package jpa.model.msg;

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
public class TemplateVariablePK implements Serializable {
	private static final long serialVersionUID = 7193883507541681000L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=SenderData.class)
	@JoinColumn(name="SenderDataRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private SenderData senderData;
	
	@Column(name="VariableId", nullable=false, length=26)
	private String variableId = "";
	@Column(name="VariableName", nullable=false, length=26)
	protected String variableName = "";
	@Column(name="StartTime", length=3, nullable=false)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	protected Timestamp startTime = new Timestamp(System.currentTimeMillis());

	public TemplateVariablePK() {}
	
	public TemplateVariablePK(SenderData senderData, String variableId, String variableName, Timestamp startTime) {
		this.senderData = senderData;
		this.variableId = variableId;
		this.variableName = variableName;
		this.startTime = startTime;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
	}
	public String getVariableId() {
		return variableId;
	}

	public void setVariableId(String variableId) {
		this.variableId = variableId;
	}

	public String getVariableName() {
		return variableName;
	}

	public void setVariableName(String variableName) {
		this.variableName = variableName;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}
}