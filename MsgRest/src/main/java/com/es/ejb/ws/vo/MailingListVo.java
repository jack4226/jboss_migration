package com.es.ejb.ws.vo;

import java.sql.Timestamp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import jpa.msgui.vo.TimestampAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "MailingListVo")
public class MailingListVo extends BaseWsVo {
	private static final long serialVersionUID = -7560592198029055531L;

	@XmlElement(required=true)
	private String listId;
	private String listEmailAddr; // =  acctUserName + @ + SenderData.domainName
	private String displayName = null;
	private String description = null;
	private boolean isBuiltin = false;
	private boolean isSendText = false;
	@XmlJavaTypeAdapter(TimestampAdapter.class)
	private Timestamp createTime;
	@XmlElement(required=true)
	private String listMasterEmailAddr;
	
	public String getListId() {
		return listId;
	}
	public void setListId(String listId) {
		this.listId = listId;
	}
	public String getListEmailAddr() {
		return listEmailAddr;
	}
	public void setListEmailAddr(String listEmailAddr) {
		this.listEmailAddr = listEmailAddr;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isBuiltin() {
		return isBuiltin;
	}
	public void setBuiltin(boolean isBuiltin) {
		this.isBuiltin = isBuiltin;
	}
	public boolean isSendText() {
		return isSendText;
	}
	public void setSendText(boolean isSendText) {
		this.isSendText = isSendText;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public String getListMasterEmailAddr() {
		return listMasterEmailAddr;
	}
	public void setListMasterEmailAddr(String listMasterEmailAddr) {
		this.listMasterEmailAddr = listMasterEmailAddr;
	}

}
