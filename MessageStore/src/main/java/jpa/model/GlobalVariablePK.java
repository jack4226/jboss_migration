package jpa.model;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class GlobalVariablePK implements Serializable {
	private static final long serialVersionUID = -4075342089197999829L;

	@Column(name="VariableName", nullable=false, length=26)
	protected String variableName = "";
	@Column(name="StartTime", length=3, nullable=false)
	//@Temporal(TemporalType.TIMESTAMP) // only required for java.util.Date
	protected Timestamp startTime = new Timestamp(System.currentTimeMillis());

	public GlobalVariablePK() {}
	
	public GlobalVariablePK(String variableName, java.sql.Timestamp startTime) {
		this.variableName = variableName;
		this.startTime = startTime;
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