package jpa.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

import jpa.constant.Constants;
import jpa.model.rule.RuleAction;

@Entity
@Table(name="sender_data")
@XmlRootElement(name="senderData")
public class SenderData extends BaseModel implements Serializable {
	private static final long serialVersionUID = 8789436921442107499L;

	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="senderVariablePK.senderData", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<SenderVariable> senderVariables;

	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="senderData", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	private IdTokens idTokens;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="senderData")
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<SubscriberData> subscribers;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="senderData")
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<UserData> userDatas;
	
	@OneToMany(cascade=CascadeType.ALL,fetch=FetchType.LAZY,mappedBy="ruleActionPK.senderData", orphanRemoval=true)
	@org.eclipse.persistence.annotations.CascadeOnDelete
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	private List<RuleAction> ruleActions;
	
	//@Index
	@Column(name="SenderId", unique=true, nullable=false, length=16)
	private String senderId = "";
	
	@Column(length=40, nullable=false)
	private String senderName = "";
	@Column(length=1, columnDefinition="char")
	private String senderType = null;
	@Column(length=100, nullable=false)
	private String domainName = "";
	@Column(length=10)
	private String irsTaxId = null;
	@Column(length=100)
	private String webSiteUrl = null;
	@Column(length=1, nullable=false, columnDefinition="boolean not null")
	private boolean isSaveRawMsg = true;
	@Column(length=60)
	private String contactName = null;
	@Column(length=18)
	private String contactPhone = null;
	@Column(length=255, nullable=false)
	private String securityEmail = "";
	@Column(length=255, nullable=false)
	private String subrCareEmail = "";
	@Column(length=255, nullable=false)
	private String rmaDeptEmail = "";
	@Column(length=255, nullable=false)
	private String spamCntrlEmail = "";
	@Column(length=255, nullable=false)
	private String virusCntrlEmail = "";
	@Column(length=255, nullable=false)
	private String chaRspHndlrEmail = "";
	@Column(length=3, nullable=false, columnDefinition="boolean not null")
	private boolean isEmbedEmailId = true;
	@Column(length=50, nullable=false)
	private String returnPathLeft = "";
	@Column(length=3, nullable=false, columnDefinition="boolean not null")
	private boolean isUseTestAddr = false;
	@Column(length=255)
	private String testFromAddr = null; 
	@Column(length=255)
	private String testToAddr = null;
	@Column(length=255)
	private String testReplytoAddr = null;
	@Column(length=3, nullable=false, columnDefinition="boolean not null")
	private boolean isVerpEnabled = false;
	@Column(length=50)
	private String verpSubDomain = null;
	@Column(length=50)
	private String verpInboxName = null;
	@Column(length=50)
	private String verpRemoveInbox = null;
	@Column(length=40, nullable=false)
	private String systemId = "";
	@Column(length=30)
	private String systemKey = null;
	@Column(length=1, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isDikm = null;
	@Column(length=1, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isDomainKey = null;
	@Column(length=200)
	private String keyFilePath = null;
	@Column(length=1, columnDefinition="Boolean")
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isSpf = null;

	@Transient
	private String origSenderId = null;
	
	public SenderData() {
		// must have a no-argument constructor
	}

	public List<SenderVariable> getSenderVariables() {
		return senderVariables;
	}

	public void setSenderVariables(List<SenderVariable> senderVariables) {
		this.senderVariables = senderVariables;
	}

	public IdTokens getIdTokens() {
		return idTokens;
	}

	public void setIdTokens(IdTokens idTokens) {
		this.idTokens = idTokens;
	}

	/** define components for UI */
	public boolean isSystemSender() {
		return Constants.DEFAULT_SENDER_ID.equalsIgnoreCase(senderId);
	}
	/** end of UI components */
	
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getSenderName() {
		return senderName;
	}
	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}
	public String getSenderType() {
		return senderType;
	}
	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}
	public String getDomainName() {
		return domainName;
	}
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	public String getIrsTaxId() {
		return irsTaxId;
	}
	public void setIrsTaxId(String irsTaxId) {
		this.irsTaxId = irsTaxId;
	}
	public String getWebSiteUrl() {
		return webSiteUrl;
	}
	public void setWebSiteUrl(String webSiteUrl) {
		this.webSiteUrl = webSiteUrl;
	}
	public boolean isSaveRawMsg() {
		return isSaveRawMsg;
	}

	public void setSaveRawMsg(boolean isSaveRawMsg) {
		this.isSaveRawMsg = isSaveRawMsg;
	}

	public String getContactName() {
		return contactName;
	}
	public void setContactName(String contactName) {
		this.contactName = contactName;
	}
	public String getContactPhone() {
		return contactPhone;
	}
	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}
	public String getSecurityEmail() {
		return securityEmail;
	}
	public void setSecurityEmail(String securityEmail) {
		this.securityEmail = securityEmail;
	}
	public String getSubrCareEmail() {
		return subrCareEmail;
	}
	public void setSubrCareEmail(String subrCareEmail) {
		this.subrCareEmail = subrCareEmail;
	}
	public String getRmaDeptEmail() {
		return rmaDeptEmail;
	}
	public void setRmaDeptEmail(String rmaDeptEmail) {
		this.rmaDeptEmail = rmaDeptEmail;
	}
	public String getSpamCntrlEmail() {
		return spamCntrlEmail;
	}
	public void setSpamCntrlEmail(String spamCntrlEmail) {
		this.spamCntrlEmail = spamCntrlEmail;
	}
	public String getVirusCntrlEmail() {
		return virusCntrlEmail;
	}
	public void setVirusCntrlEmail(String virusCntrlEmail) {
		this.virusCntrlEmail = virusCntrlEmail;
	}
	public String getChaRspHndlrEmail() {
		return chaRspHndlrEmail;
	}
	public void setChaRspHndlrEmail(String chaRspHndlrEmail) {
		this.chaRspHndlrEmail = chaRspHndlrEmail;
	}
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	public boolean isEmbedEmailId() {
		return isEmbedEmailId;
	}
	public void setEmbedEmailId(boolean isEmbedEmailId) {
		this.isEmbedEmailId = isEmbedEmailId;
	}
	public String getTestFromAddr() {
		return testFromAddr;
	}
	public void setTestFromAddr(String testFromAddr) {
		this.testFromAddr = testFromAddr;
	}
	public String getTestToAddr() {
		return testToAddr;
	}
	public void setTestToAddr(String testToAddr) {
		this.testToAddr = testToAddr;
	}
	public String getTestReplytoAddr() {
		return testReplytoAddr;
	}
	public void setTestReplytoAddr(String testReplytoAddr) {
		this.testReplytoAddr = testReplytoAddr;
	}
	public boolean isUseTestAddr() {
		return isUseTestAddr;
	}
	public void setUseTestAddr(boolean isUseTestAddr) {
		this.isUseTestAddr = isUseTestAddr;
	}
	public boolean isVerpEnabled() {
		return isVerpEnabled;
	}
	public void setVerpEnabled(boolean isVerpEnabled) {
		this.isVerpEnabled = isVerpEnabled;
	}
	public String getVerpSubDomain() {
		return verpSubDomain;
	}
	public void setVerpSubDomain(String verpSubDomain) {
		this.verpSubDomain = verpSubDomain;
	}
	public String getVerpInboxName() {
		return verpInboxName;
	}
	public void setVerpInboxName(String verpInboxName) {
		this.verpInboxName = verpInboxName;
	}
	public String getVerpRemoveInbox() {
		return verpRemoveInbox;
	}
	public void setVerpRemoveInbox(String verpRemoveInbox) {
		this.verpRemoveInbox = verpRemoveInbox;
	}
	public String getOrigSenderId() {
		return origSenderId;
	}
	public void setOrigSenderId(String origSenderId) {
		this.origSenderId = origSenderId;
	}
	public String getReturnPathLeft() {
		return returnPathLeft;
	}
	public void setReturnPathLeft(String returnPathLeft) {
		this.returnPathLeft = returnPathLeft;
	}

	public String getSystemKey() {
		return systemKey;
	}

	public void setSystemKey(String systemKey) {
		this.systemKey = systemKey;
	}

	public String getKeyFilePath() {
		return keyFilePath;
	}

	public void setKeyFilePath(String keyFilePath) {
		this.keyFilePath = keyFilePath;
	}

	public Boolean getIsDikm() {
		return isDikm;
	}

	public void setIsDikm(Boolean isDikm) {
		this.isDikm = isDikm;
	}

	public Boolean getIsDomainKey() {
		return isDomainKey;
	}

	public void setIsDomainKey(Boolean isDomainKey) {
		this.isDomainKey = isDomainKey;
	}

	public Boolean getIsSpf() {
		return isSpf;
	}

	public void setIsSpf(Boolean isSpf) {
		this.isSpf = isSpf;
	}

	public List<SubscriberData> getSubscribers() {
		return subscribers;
	}

	public void setSubscribers(List<SubscriberData> subscribers) {
		this.subscribers = subscribers;
	}

	public List<UserData> getUserDatas() {
		return userDatas;
	}

	public void setUserDatas(List<UserData> userDatas) {
		this.userDatas = userDatas;
	}

	public List<RuleAction> getRuleActions() {
		return ruleActions;
	}

	public void setRuleActions(List<RuleAction> ruleActions) {
		this.ruleActions = ruleActions;
	}
}