package jpa.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name="session_upload", uniqueConstraints=@UniqueConstraint(columnNames = {"sessionId", "sessionSequence"}))
@XmlRootElement(name="sessionUpload")
public class SessionUpload extends BaseModel implements java.io.Serializable {
private static final long serialVersionUID = 2468665095583100932L;

	@ManyToOne(fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="UserDataRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private UserData userData;
	
	@Embedded
	private SessionUploadPK sessionUploadPK;

	@Column(nullable=false, length=100)
	private String fileName = "";
	@Column(nullable=true, length=100)
	private String contentType = null;
	@Lob
	private byte[] sessionValue = null;

	@Transient
	private long fileSize = 0;

	public SessionUpload() {
		// must have a no-argument constructor
	}

	public SessionUploadPK getSessionUploadPK() {
		return sessionUploadPK;
	}

	public void setSessionUploadPK(SessionUploadPK sessionUploadPK) {
		this.sessionUploadPK = sessionUploadPK;
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getSessionValue() {
		return sessionValue;
	}

	public void setSessionValue(byte[] sessionValue) {
		this.sessionValue = sessionValue;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
}
