package jpa.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import jpa.constant.CarrierCode;
import jpa.constant.MailServerType;

@Entity
@Table(name="mail_inbox", uniqueConstraints=@UniqueConstraint(columnNames = {"userId", "hostName"}))
@XmlRootElement(name="mailInbox")
public class MailInbox extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -2636716589190853166L;

	@Embedded
	private MailInboxPK mailInboxPK;

	@Column(nullable=false, length=32)
	private String userPswd = "";
	@Column(nullable=false)
	private int portNumber = -1;
	@Column(nullable=false, length=4)
	private String protocol = "";
	@Column(nullable=true, length=10)
	private String serverType = MailServerType.SMTP.value();
	@Column(nullable=true, length=30)
	private String folderName = "Inbox";
	@Column(nullable=true, length=50)
	private String description = null;
	@Column(nullable=false, length=1, columnDefinition="char")
	private String carrierCode = CarrierCode.SMTPMAIL.getValue();
	@Column(nullable=true, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isInternalOnly = null;
	@Column(nullable=false, columnDefinition="int")
	private int readPerPass = -1;
	@Column(nullable=false, columnDefinition="boolean")
	private boolean isUseSsl = false;
	@Column(nullable=true, columnDefinition="Integer")
	private int numberOfThreads;
	@Column(nullable=true, columnDefinition="Integer")
	private Integer maximumRetries = null;
	@Column(nullable=true, columnDefinition="Integer")
	private Integer minimumWait = null;
	@Column(nullable=true, columnDefinition="Integer")
	private int messageCount;
	@Column(nullable=true, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isToPlainText = null;
	@Column(nullable=true, length=500)
	private String toAddressDomain = null;
	@Column(nullable=true, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isCheckDuplicate = null;
	@Column(nullable=true, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isAlertDuplicate = null;
	@Column(nullable=true, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isLogDuplicate = null;
	@Column(nullable=true, columnDefinition="Integer")
	private Integer purgeDupsAfter = null;

	@Transient
	@XmlTransient
	// used to tell if it's from batch or EJB timer
	private boolean isFromTimer = false; // default to batch

	public MailInbox() {
		// must have a no-argument constructor
	}

	public MailInboxPK getMailInboxPK() {
		return mailInboxPK;
	}

	public void setMailInboxPK(MailInboxPK mailInboxPK) {
		this.mailInboxPK = mailInboxPK;
	}

	public String getUserPswd() {
		return userPswd;
	}

	public void setUserPswd(String userPswd) {
		this.userPswd = userPswd;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCarrierCode() {
		return carrierCode;
	}

	public void setCarrierCode(String carrierCode) {
		this.carrierCode = carrierCode;
	}

	public Boolean getIsInternalOnly() {
		return isInternalOnly;
	}

	public void setIsInternalOnly(Boolean isInternalOnly) {
		this.isInternalOnly = isInternalOnly;
	}

	public int getReadPerPass() {
		return readPerPass;
	}

	public void setReadPerPass(int readPerPass) {
		this.readPerPass = readPerPass;
	}

	public boolean isUseSsl() {
		return isUseSsl;
	}

	public void setUseSsl(boolean isUseSsl) {
		this.isUseSsl = isUseSsl;
	}

	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public Integer getMaximumRetries() {
		return maximumRetries;
	}

	public void setMaximumRetries(Integer maximumRetries) {
		this.maximumRetries = maximumRetries;
	}

	public Integer getMinimumWait() {
		return minimumWait;
	}

	public void setMinimumWait(Integer minimumWait) {
		this.minimumWait = minimumWait;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

	public Boolean getIsToPlainText() {
		return isToPlainText;
	}

	public void setIsToPlainText(Boolean isToPlainText) {
		this.isToPlainText = isToPlainText;
	}

	public String getToAddressDomain() {
		return toAddressDomain;
	}

	public void setToAddressDomain(String toAddressDomain) {
		this.toAddressDomain = toAddressDomain;
	}

	public Boolean getIsCheckDuplicate() {
		return isCheckDuplicate;
	}

	public void setIsCheckDuplicate(Boolean isCheckDuplicate) {
		this.isCheckDuplicate = isCheckDuplicate;
	}

	public Boolean getIsAlertDuplicate() {
		return isAlertDuplicate;
	}

	public void setIsAlertDuplicate(Boolean isAlertDuplicate) {
		this.isAlertDuplicate = isAlertDuplicate;
	}

	public Boolean getIsLogDuplicate() {
		return isLogDuplicate;
	}

	public void setIsLogDuplicate(Boolean isLogDuplicate) {
		this.isLogDuplicate = isLogDuplicate;
	}

	public Integer getPurgeDupsAfter() {
		return purgeDupsAfter;
	}

	public void setPurgeDupsAfter(Integer purgeDupsAfter) {
		this.purgeDupsAfter = purgeDupsAfter;
	}

	public boolean isFromTimer() {
		return isFromTimer;
	}

	public void setFromTimer(boolean isFromTimer) {
		this.isFromTimer = isFromTimer;
	}

}
