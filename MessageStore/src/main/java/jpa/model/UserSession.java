package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name="user_session")
@XmlRootElement(name="userSession")
public class UserSession extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 7367026694851772455L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="UserDataRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private UserData userData;
	
	@Column(nullable=false, length=50)
	private String sessionId = "";
	@Column(nullable=true, length=50)
	private String sessionName = "";
	@Column(length=8190)
	private String sessionValue = null;

	public UserSession() {
		// must have a no-argument constructor
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getSessionValue() {
		return sessionValue;
	}

	public void setSessionValue(String sessionValue) {
		this.sessionValue = sessionValue;
	}
}
