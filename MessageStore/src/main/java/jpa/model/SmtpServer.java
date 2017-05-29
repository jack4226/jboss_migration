package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name="smtp_server")
@XmlRootElement(name="smtpServer")
public class SmtpServer extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -5853276462328429525L;

	@Column(nullable=true, length=50, unique=true)
	private String serverName = null;
	@Column(nullable=false, length=100)
	private String smtpHostName = "";
	@Column(nullable=false)
	private int smtpPortNumber = -1;
	@Column(nullable=true, length=50)
	private String description = null;
	@Column(nullable=false, columnDefinition="boolean")
	private boolean isUseSsl = false;
	@Column(nullable=true, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isUseAuth = null;
	@Column(nullable=false, length=30)
	private String userId = ""; 
	@Column(nullable=false, length=32)
	private String userPswd = "";
	@Column(nullable=false, columnDefinition="boolean")
	private boolean isPersistence = true;
	@Column(nullable=true, length=10)
	private String serverType = null;
	@Column(nullable=false)
	private int numberOfThreads = 4;
	@Column(nullable=false)
	private int maximumRetries = -1;
	@Column(nullable=false)
	private int retryFrequence = -1;
	@Column(nullable=true, columnDefinition="Integer")
	private Integer minimumWait = -1;
	@Column(nullable=true, columnDefinition="Integer")
	private Integer alertAfter = null;
	@Column(nullable=true, length=10)
	private String alertLevel = null;
	@Column(nullable=true, columnDefinition="Integer")
	private int messageCount;

	public SmtpServer() {
		// must have a no-argument constructor
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getSmtpHostName() {
		return smtpHostName;
	}

	public void setSmtpHostName(String smtpHostName) {
		this.smtpHostName = smtpHostName;
	}

	public int getSmtpPortNumber() {
		return smtpPortNumber;
	}

	public void setSmtpPortNumber(int smtpPortNumber) {
		this.smtpPortNumber = smtpPortNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean getIsUseSsl() {
		return isUseSsl;
	}

	public void setIsUseSsl(boolean isUseSsl) {
		this.isUseSsl = isUseSsl;
	}

	public Boolean getIsUseAuth() {
		return isUseAuth;
	}

	public void setIsUseAuth(Boolean isUseAuth) {
		this.isUseAuth = isUseAuth;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserPswd() {
		return userPswd;
	}

	public void setUserPswd(String userPswd) {
		this.userPswd = userPswd;
	}

	public boolean getIsPersistence() {
		return isPersistence;
	}

	public void setIsPersistence(boolean isPersistence) {
		this.isPersistence = isPersistence;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public int getMaximumRetries() {
		return maximumRetries;
	}

	public void setMaximumRetries(int maximumRetries) {
		this.maximumRetries = maximumRetries;
	}

	public int getRetryFrequence() {
		return retryFrequence;
	}

	public void setRetryFrequence(int retryFrequence) {
		this.retryFrequence = retryFrequence;
	}

	public Integer getMinimumWait() {
		return minimumWait;
	}

	public void setMinimumWait(Integer minimumWait) {
		this.minimumWait = minimumWait;
	}

	public Integer getAlertAfter() {
		return alertAfter;
	}

	public void setAlertAfter(Integer alertAfter) {
		this.alertAfter = alertAfter;
	}

	public String getAlertLevel() {
		return alertLevel;
	}

	public void setAlertLevel(String alertLevel) {
		this.alertLevel = alertLevel;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}
}
