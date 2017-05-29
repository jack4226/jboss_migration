package jpa.model;

import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import jpa.constant.CodeType;
import jpa.constant.StatusId;

@MappedSuperclass
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Access(AccessType.FIELD)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseVariableModel implements Serializable {
	private static final long serialVersionUID = 3239024926806006588L;

	@Id
	/*
	 * XXX GenerationType.IDENTITY caused following error when deployed to JBoss 5.1: 
	 * 	org.hibernate.MappingException: Cannot use identity column key generation with <union-subclass> mapping for: ...
	 * 
	 * Cause (Hybernate specific):
	 * There is a problem with mixing Table per Class inheritance and GenerationType.IDENTITY. 
	 * Consider an identity column in MySql. It is Column-Based. In a Table-Per-Class Strategy
	 * you use one table per class and each one has an id.
	 * 
	 * Fix:
	 * Use GenerationType.TABLE instead.
	 */
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/*
	 * XXX !!!!!!!!!! Very important !!!!!!!!!!
	 * "name" attribute in Column annotation is isRequired for native query
	 * to map query results to the JPA model class.
	 */
	@Column(name="row_id", updatable=false) 
	protected Integer rowId;
	@Column(name="variable_format", length=50, nullable=true)
	private String variableFormat= null;
	/*
	 * XXX received following error when deployed to JBoss 5.1:
	 * org.hibernate.HibernateException: Wrong column type Found: char, expected: varchar(1)
	 * 
	 * Cause (Hybernate specific):
	 * Hibernate has found "char" as the dataType of the column in the DATABASE and in 
	 * Hybernate mapping the default data type is String.
	 * 
	 * Fix:
	 * Add columnDefinition="char" to @Column annotation.
	 */
	@Column(name="variable_type", nullable=false, length=1, columnDefinition="char not null default 'T'")
	private String variableType = "";
	// T - text, N - numeric, D - DateField/time,
	// A - address, X - Xheader, L - LOB(Attachment)
	//private String statusId = Constants.ACTIVE;
	// A - Active, I - Inactive
	@Column(name="status_id", length=1, nullable=false, columnDefinition="char not null default 'A'")
	private String statusId = StatusId.ACTIVE.getValue();
	@Column(name="allow_override", length=1, nullable=false, columnDefinition="char not null default 'Y'")
	private String allowOverride = CodeType.YES_CODE.getValue();
	// allow override value to be supplied at runtime, Y/N/M, M=Mandatory
	@Column(name="is_required", length=1, nullable=false, columnDefinition="boolean not null default false")
	private boolean isRequired = false;
	
	public Integer getRowId() {
		return rowId;
	}
	public String getAllowOverride() {
		return allowOverride;
	}
	public void setAllowOverride(String allowOverride) {
		this.allowOverride = allowOverride;
	}
	public boolean isRequired() {
		return isRequired;
	}
	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
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
	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
}