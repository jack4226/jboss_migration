package com.es.ejb.ws.vo;

import java.sql.Timestamp;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.msgui.vo.TimestampAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "SubscriberDataVo")
public class SubscriberDataVo extends BaseWsVo {
	private static final long serialVersionUID = -2809569500356794118L;

	@XmlElement(required=true)
	private String senderId;
	@XmlElement(required=true)
	private String emailAddress;
	@XmlElement(required=true)
	private String subscriberId = "";
	private String ssnNumber = null;
	private String taxId = null;
	private String profession = null;
	private String firstName = null;
	private String middleName = null;
	@XmlElement(required=true)
	private String lastName = "";
	private String alias = null;
	private String streetAddress = null;
	private String streetAddress2 = null;
	private String cityName = null;
	private String stateCode = null;
	private String zipCode5 = null;
	private String zipCode4 = null;
	private String provinceName = null;
	private String postalCode = null;
	private String country = null;
	private String dayPhone = null;
	private String eveningPhone = null;
	private String mobilePhone = null;
	private Date birthDate = null;
	private Date startDate;
	private Date endDate = null;
	private String mobileCarrier = null;
	private String msgHeader = null;
	private String msgDetail = null;
	private String msgOptional = null;
	private String msgFooter = null;
	private String timeZone = null;
	private String memoText = null;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp passwordChangeTime = null;
	private String userPassword = null;
	private String securityQuestion = null;
	private String securityAnswer = null;
	
	public String getSenderId() {
		return senderId;
	}
	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getSsnNumber() {
		return ssnNumber;
	}
	public void setSsnNumber(String ssnNumber) {
		this.ssnNumber = ssnNumber;
	}
	public String getTaxId() {
		return taxId;
	}
	public void setTaxId(String taxId) {
		this.taxId = taxId;
	}
	public String getProfession() {
		return profession;
	}
	public void setProfession(String profession) {
		this.profession = profession;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getMiddleName() {
		return middleName;
	}
	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getStreetAddress() {
		return streetAddress;
	}
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
	}
	public String getStreetAddress2() {
		return streetAddress2;
	}
	public void setStreetAddress2(String streetAddress2) {
		this.streetAddress2 = streetAddress2;
	}
	public String getCityName() {
		return cityName;
	}
	public void setCityName(String cityName) {
		this.cityName = cityName;
	}
	public String getStateCode() {
		return stateCode;
	}
	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}
	public String getZipCode5() {
		return zipCode5;
	}
	public void setZipCode5(String zipCode5) {
		this.zipCode5 = zipCode5;
	}
	public String getZipCode4() {
		return zipCode4;
	}
	public void setZipCode4(String zipCode4) {
		this.zipCode4 = zipCode4;
	}
	public String getProvinceName() {
		return provinceName;
	}
	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getDayPhone() {
		return dayPhone;
	}
	public void setDayPhone(String dayPhone) {
		this.dayPhone = dayPhone;
	}
	public String getEveningPhone() {
		return eveningPhone;
	}
	public void setEveningPhone(String eveningPhone) {
		this.eveningPhone = eveningPhone;
	}
	public String getMobilePhone() {
		return mobilePhone;
	}
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getMobileCarrier() {
		return mobileCarrier;
	}
	public void setMobileCarrier(String mobileCarrier) {
		this.mobileCarrier = mobileCarrier;
	}
	public String getMsgHeader() {
		return msgHeader;
	}
	public void setMsgHeader(String msgHeader) {
		this.msgHeader = msgHeader;
	}
	public String getMsgDetail() {
		return msgDetail;
	}
	public void setMsgDetail(String msgDetail) {
		this.msgDetail = msgDetail;
	}
	public String getMsgOptional() {
		return msgOptional;
	}
	public void setMsgOptional(String msgOptional) {
		this.msgOptional = msgOptional;
	}
	public String getMsgFooter() {
		return msgFooter;
	}
	public void setMsgFooter(String msgFooter) {
		this.msgFooter = msgFooter;
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public String getMemoText() {
		return memoText;
	}
	public void setMemoText(String memoText) {
		this.memoText = memoText;
	}
	public Timestamp getPasswordChangeTime() {
		return passwordChangeTime;
	}
	public void setPasswordChangeTime(Timestamp passwordChangeTime) {
		this.passwordChangeTime = passwordChangeTime;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	public String getSecurityQuestion() {
		return securityQuestion;
	}
	public void setSecurityQuestion(String securityQuestion) {
		this.securityQuestion = securityQuestion;
	}
	public String getSecurityAnswer() {
		return securityAnswer;
	}
	public void setSecurityAnswer(String securityAnswer) {
		this.securityAnswer = securityAnswer;
	}

}
