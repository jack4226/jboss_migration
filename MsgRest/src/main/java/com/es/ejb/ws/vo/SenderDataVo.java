package com.es.ejb.ws.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SenderDataVo")
public class SenderDataVo extends BaseWsVo {
	private static final long serialVersionUID = 738356381063538434L;

	@XmlElement(required=true)
	private String senderId;
	@XmlElement(required=true)
	private String senderName;
	private String senderType;
	@XmlElement(required=true)
	private String domainName;
	private String irsTaxId;
	private String webSiteUrl;
	@XmlElement(required=true, defaultValue="true")
	private boolean isSaveRawMsg = true;
	private String contactName;
	private String contactPhone;
	@XmlElement(required=true)
	private String securityEmail;
	@XmlElement(required=true)
	private String subrCareEmail;
	@XmlElement(required=true)
	private String rmaDeptEmail;
	@XmlElement(required=true)
	private String spamCntrlEmail;
	@XmlElement(required=true)
	private String virusCntrlEmail;
	@XmlElement(required=true)
	private String chaRspHndlrEmail;
	@XmlElement(required=true, defaultValue="true")
	private boolean isEmbedEmailId = true;
	@XmlElement(required=true)
	private String returnPathLeft;
	@XmlElement(required=true, defaultValue="false")
	private boolean isUseTestAddr = false;
	private String testFromAddr; 
	private String testToAddr;
	private String testReplytoAddr;
	@XmlElement(required=true, type=Boolean.class, defaultValue="false")
	private boolean isVerpEnabled = false;
	private String verpSubDomain;
	private String verpInboxName;
	private String verpRemoveInbox;
	@XmlElement(required=true)
	private String systemId;
	private String systemKey;
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isDikm;
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isDomainKey;
	private String keyFilePath;
	//@XmlJavaTypeAdapter(BooleanAdapter.class)
	private Boolean isSpf;
	
	public SenderDataVo() {}
	
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
	public boolean isEmbedEmailId() {
		return isEmbedEmailId;
	}
	public void setEmbedEmailId(boolean isEmbedEmailId) {
		this.isEmbedEmailId = isEmbedEmailId;
	}
	public String getReturnPathLeft() {
		return returnPathLeft;
	}
	public void setReturnPathLeft(String returnPathLeft) {
		this.returnPathLeft = returnPathLeft;
	}
	public boolean isUseTestAddr() {
		return isUseTestAddr;
	}
	public void setUseTestAddr(boolean isUseTestAddr) {
		this.isUseTestAddr = isUseTestAddr;
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
	public String getSystemId() {
		return systemId;
	}
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}
	public String getSystemKey() {
		return systemKey;
	}
	public void setSystemKey(String systemKey) {
		this.systemKey = systemKey;
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
	public String getKeyFilePath() {
		return keyFilePath;
	}
	public void setKeyFilePath(String keyFilePath) {
		this.keyFilePath = keyFilePath;
	}
	public Boolean getIsSpf() {
		return isSpf;
	}
	public void setIsSpf(Boolean isSpf) {
		this.isSpf = isSpf;
	}

}
