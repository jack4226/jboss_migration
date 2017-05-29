package jpa.model.msg;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseVariableModel;

@Entity
@Table(name="template_variable", uniqueConstraints=@UniqueConstraint(columnNames = {"SenderDataRowId", "variableId", "variableName", "startTime"}))
public class TemplateVariable extends BaseVariableModel implements Serializable
{
	private static final long serialVersionUID = -5646384767553614998L;

	@Embedded
	private TemplateVariablePK templateVariablePK;
	
	@ManyToMany(fetch=FetchType.LAZY,cascade=CascadeType.ALL) // TemplateVariable <-n:n-> MessageSource
	@JoinTable(name="source_variable",
			joinColumns = {@JoinColumn(name = "TemplateVariableRowId", referencedColumnName = "row_id", columnDefinition="int")},
			inverseJoinColumns = {@JoinColumn(name="MessageSoureRowId",referencedColumnName="row_id", columnDefinition="int")}
			)
	private List<MessageSource> messageSourceList;

	@Lob
	@Column(name="VariableValue", length=65530, nullable=true)
	private String variableValue = null;

	public TemplateVariable() {}
	
	public TemplateVariablePK getTemplateVariablePK() {
		return templateVariablePK;
	}

	public void setTemplateVariablePK(TemplateVariablePK templateVariablePK) {
		this.templateVariablePK = templateVariablePK;
	}

	public String getVariableValue() {
		return variableValue;
	}
	public void setVariableValue(String variableValue) {
		this.variableValue = variableValue;
	}
	public List<MessageSource> getMessageSourceList() {
		return messageSourceList;
	}
	public void setMessageSourceList(List<MessageSource> messageSourceList) {
		this.messageSourceList = messageSourceList;
	}
}
