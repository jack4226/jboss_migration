package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="sender_variable", uniqueConstraints=@UniqueConstraint(columnNames = {"senderDataRowId", "variableName", "startTime"}))
@XmlRootElement(name="senderVariable")
public class SenderVariable extends BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = -5873779791693771806L;

	@Embedded
	private SenderVariablePK senderVariablePK;

	@Column(name="VariableValue", length=2046, nullable=true)
	private String variableValue = null;

	public SenderVariable() {}
	
	public SenderVariablePK getSenderVariablePK() {
		return senderVariablePK;
	}
	public void setSenderVariablePK(SenderVariablePK senderVariablePK) {
		this.senderVariablePK = senderVariablePK;
	}
	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}
