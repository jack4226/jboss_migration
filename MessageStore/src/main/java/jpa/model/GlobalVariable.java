package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="global_variable", uniqueConstraints=@UniqueConstraint(columnNames = {"variableName", "startTime"}))
@XmlRootElement(name="globalVariable")
public class GlobalVariable extends BaseVariableModel implements Serializable {
	private static final long serialVersionUID = 7381275253094081485L;
	
	@Embedded
	private GlobalVariablePK globalVariablePK;
	
	@Column(name="VariableValue", length=510)
	private String variableValue = null;

	public GlobalVariablePK getGlobalVariablePK() {
		return globalVariablePK;
	}
	public void setGlobalVariablePK(GlobalVariablePK globalVariablePK) {
		this.globalVariablePK = globalVariablePK;
	}
	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}
