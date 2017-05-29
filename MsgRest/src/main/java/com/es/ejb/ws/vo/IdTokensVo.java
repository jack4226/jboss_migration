package com.es.ejb.ws.vo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "IdTokensVo")
public class IdTokensVo extends BaseWsVo {
	private static final long serialVersionUID = 3125512427351036103L;
	
	@XmlElement(required=true)
	private String senderId;
	private String description = null;
	@XmlElement(required=true)
	private String bodyBeginToken;
	@XmlElement(required=true)
	private String bodyEndToken;
	private String xheaderName;
	private String xhdrBeginToken;
	private String xhdrEndToken;
	@XmlElement(required=true)
	private int maxLength = -1;

	public IdTokensVo() {
		// must have a no-argument constructor
	}

	public String getSenderId() {
		return senderId;
	}

	public void setSenderId(String senderId) {
		this.senderId = senderId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getBodyBeginToken() {
		return bodyBeginToken;
	}

	public void setBodyBeginToken(String bodyBeginToken) {
		this.bodyBeginToken = bodyBeginToken;
	}

	public String getBodyEndToken() {
		return bodyEndToken;
	}

	public void setBodyEndToken(String bodyEndToken) {
		this.bodyEndToken = bodyEndToken;
	}

	public String getXheaderName() {
		return xheaderName;
	}

	public void setXheaderName(String xheaderName) {
		this.xheaderName = xheaderName;
	}

	public String getXhdrBeginToken() {
		return xhdrBeginToken;
	}

	public void setXhdrBeginToken(String xhdrBeginToken) {
		this.xhdrBeginToken = xhdrBeginToken;
	}

	public String getXhdrEndToken() {
		return xhdrEndToken;
	}

	public void setXhdrEndToken(String xhdrEndToken) {
		this.xhdrEndToken = xhdrEndToken;
	}

	public int getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
}
