package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

@Entity
@Table(name="render_variable", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageRenderedRowId", "variableName"}))
public class RenderVariable extends BaseModel implements Serializable {
	private static final long serialVersionUID = -2242608473824854034L;

	@Embedded
	private RenderVariablePK renderVariablePK;
	
	@Column(nullable=true,length=50)
	private String variableFormat = null;
	@Column(nullable=true,length=1, columnDefinition="char(1)")
	private String variableType = null;
	@Lob
	@Column(nullable=true,length=32000)
	private String variableValue = null;

	public RenderVariable() {
		// must have a no-argument constructor
	}

	public RenderVariablePK getRenderVariablePK() {
		return renderVariablePK;
	}

	public void setRenderVariablePK(RenderVariablePK renderVariablePK) {
		this.renderVariablePK = renderVariablePK;
	}

	public String getVariableFormat() {
		return variableFormat;
	}

	public void setVariableFormat(String variableFormat) {
		this.variableFormat = variableFormat;
	}

	public String getVariableType() {
		return variableType;
	}

	public void setVariableType(String variableType) {
		this.variableType = variableType;
	}

	public String getVariableValue() {
		return variableValue;
	}

	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
}