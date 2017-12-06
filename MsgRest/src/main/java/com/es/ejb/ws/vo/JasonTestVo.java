package com.es.ejb.ws.vo;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.msgui.vo.TimestampAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "JasonTestVo")
public class JasonTestVo extends BaseWsVo {
	private static final long serialVersionUID = -3265032698753962834L;

	@XmlElement(required=true)
	private String listId;
	private String description;

	@XmlElement(required=true)
	private boolean isSubscribed = true;

	private Boolean isOptIn = null;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp CreateTime;

	private List<EmailAddrVo> addrList;
	
	public JasonTestVo() {
		// must have a no-argument constructor
	}

	public List<EmailAddrVo> getAddrList() {
		if (addrList == null) {
			addrList = new ArrayList<>();
		}
		return addrList;
	}

	public void setAddrList(List<EmailAddrVo> addrList) {
		this.addrList = addrList;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isSubscribed() {
		return isSubscribed;
	}

	public void setSubscribed(boolean isSubscribed) {
		this.isSubscribed = isSubscribed;
	}

	public Boolean getIsOptIn() {
		return isOptIn;
	}

	public void setIsOptIn(Boolean isOptIn) {
		this.isOptIn = isOptIn;
	}

	public Timestamp getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(Timestamp createTime) {
		CreateTime = createTime;
	}

}
