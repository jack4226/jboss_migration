package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

@Embeddable
public class SenderVariablePK implements Serializable {
	private static final long serialVersionUID = 1422523897905980641L;

	@ManyToOne(fetch=FetchType.EAGER, optional=false, targetEntity=SenderData.class)
	@JoinColumn(name="SenderDataRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	/*
	 * Add annotation to exclude the parent element to prevent a cycle.
	 * 
	 * SAXException: A cycle is detected in the object graph...
	 */
	@XmlTransient
	private SenderData senderData;
	
	@Column(name="VariableName", nullable=false, length=26)
	protected String variableName = "";
	@Column(name="StartTime", length=3, nullable=false)
	//@Temporal(TemporalType.TIMESTAMP)
	protected Timestamp startTime = new Timestamp(System.currentTimeMillis());

	public SenderVariablePK() {}
	
	public SenderVariablePK(SenderData senderData, String variableName, Timestamp startTime) {
		this.senderData = senderData;
		this.variableName = variableName;
		this.startTime = startTime;
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
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