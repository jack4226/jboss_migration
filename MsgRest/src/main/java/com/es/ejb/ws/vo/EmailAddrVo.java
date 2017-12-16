package com.es.ejb.ws.vo;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.johnzon.mapper.JohnzonConverter;

import jpa.msgui.vo.TimestampAdapter;
import jpa.msgui.vo.TimestampConverter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "EmailAddrVo")
public class EmailAddrVo extends BaseWsVo {
	private static final long serialVersionUID = -918554579365101630L;

	@XmlElement(required=true)
	private String address;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	@JohnzonConverter(TimestampConverter.class)
	private Timestamp statusChangeTime;
 	private String statusChangeUserId;
	@XmlElement(required=true)
	private boolean isAcceptHtml;

	public EmailAddrVo() {
		// must have a no-argument constructor
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Timestamp getStatusChangeTime() {
		return statusChangeTime;
	}

	public void setStatusChangeTime(Timestamp statusChangeTime) {
		this.statusChangeTime = statusChangeTime;
	}

	public String getStatusChangeUserId() {
		return statusChangeUserId;
	}

	public void setStatusChangeUserId(String statusChangeUserId) {
		this.statusChangeUserId = statusChangeUserId;
	}

	public boolean isAcceptHtml() {
		return isAcceptHtml;
	}

	public void setAcceptHtml(boolean isAcceptHtml) {
		this.isAcceptHtml = isAcceptHtml;
	}

}
