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
@Table(name="template_data", uniqueConstraints=@UniqueConstraint(columnNames = {"SenderDataRowId", "templateId", "startTime"}))
public class TemplateData extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 577665280960727849L;

	@Embedded
	private TemplateDataPK templateDataPK;
	
	@Column(nullable=true, length=100)
	private String description = null;
	@Lob
	@Column(nullable=true, length=65530)
	private String bodyTemplate = null;
	@Column(nullable=false, length=100)
	private String contentType = null;
	@Column(nullable=true, length=255)
	private String subjectTemplate = null;

	public TemplateData() {}

	public TemplateDataPK getTemplateDataPK() {
		return templateDataPK;
	}

	public void setTemplateDataPK(TemplateDataPK templateDataPK) {
		this.templateDataPK = templateDataPK;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBodyTemplate() {
		return bodyTemplate;
	}

	public void setBodyTemplate(String bodyTemplate) {
		this.bodyTemplate = bodyTemplate;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getSubjectTemplate() {
		return subjectTemplate;
	}

	public void setSubjectTemplate(String subjectTemplate) {
		this.subjectTemplate = subjectTemplate;
	}
}
