package jpa.model;

import java.lang.reflect.Method;
import java.sql.Timestamp;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.constant.Constants;
import jpa.constant.StatusId;
import jpa.msgui.vo.TimestampAdapter;
import jpa.util.BeanCopyUtil;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

@MappedSuperclass
@Inheritance(strategy=InheritanceType.TABLE_PER_CLASS)
@Access(AccessType.FIELD)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -3737571995910644181L;
	protected static Logger logger = Logger.getLogger(BaseModel.class);
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="row_id", updatable=false)
	protected Integer rowId;

	@Column(name="status_id", length=1, nullable=false, columnDefinition="char not null default 'A'")
	private String statusId = StatusId.ACTIVE.getValue();
	@Column(name="updt_time", length=3, nullable=false)
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	protected Timestamp updtTime = new Timestamp(System.currentTimeMillis());
	@Column(name="updt_user_id", length=10, nullable=false)
	protected String updtUserId = Constants.DEFAULT_USER_ID;
	
	/* 
	 * Define transient fields and methods for UI application 
	 */
	@Transient
	@XmlTransient
	protected boolean editable = true;
	@Transient
	@XmlTransient
	protected boolean markedForDeletion = false;
	@Transient
	@XmlTransient
	protected boolean markedForEdition = false;

	public boolean isEditable() {
		return editable;
	}
	public void setEditable(boolean editable) {
		this.editable = editable;
	}
	public boolean isMarkedForDeletion() {
		return markedForDeletion;
	}
	public void setMarkedForDeletion(boolean markedForDeletion) {
		this.markedForDeletion = markedForDeletion;
	}
	public boolean isMarkedForEdition() {
		return markedForEdition;
	}
	public void setMarkedForEdition(boolean markedForEdition) {
		this.markedForEdition = markedForEdition;
	}
	
	public String getStatusIdDesc() { // Generic status description.
		try {
			Method method = this.getClass().getMethod("getStatusId", (Class[])null);
			String desc = (String) method.invoke(this, (Object[])null);
			if (StatusId.ACTIVE.getValue().equals(desc)) {
				desc = "Active";
			}
			else if (StatusId.INACTIVE.getValue().equals(desc)) {
				desc = "Inactive";
			}
			else if (StatusId.SUSPENDED.getValue().equals(desc)) {
				desc = "Suspended";
			}
			return desc;
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
			// ignored
			return null;
		}
	}

	public void copyPropertiesTo(BaseModel dest) {
		BeanCopyUtil.registerBeanUtilsConverters();
		try {
			BeanUtils.copyProperties(dest, this);
		}
		catch (Exception e) {
			logger.warn("Exception caught from BeanUtils.copyProperties() - " + e.getMessage());
			BeanCopyUtil.copyProperties(dest, this);
		}
	}
	/* end of UI */

	public String getStatusId() {
		return statusId;
	}
	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}
	public Timestamp getUpdtTime() {
		return updtTime;
	}
	public void setUpdtTime(Timestamp updtTime) {
		this.updtTime = updtTime;
	}
	public String getUpdtUserId() {
		return updtUserId;
	}
	public void setUpdtUserId(String updtUserId) {
		this.updtUserId = updtUserId;
	}
	public Integer getRowId() {
		return rowId;
	}
}
