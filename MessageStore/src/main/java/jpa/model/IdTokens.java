package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name="id_tokens")
@XmlRootElement(name="idTokens")
public class IdTokens extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = -632308305179136081L;

	@OneToOne(targetEntity=SenderData.class, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="SenderDataRowId", insertable=true, referencedColumnName="row_id", nullable=false, unique=true)
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	@XmlTransient
	private SenderData senderData;

	@Column(nullable=true, length=100)
	private String description = null;
	@Column(nullable=false, length=16)
	private String bodyBeginToken = "";
	@Column(nullable=false, length=4)
	private String bodyEndToken = "";
	@Column(length=20)
	private String xheaderName = null;
	@Column(length=16)
	private String xhdrBeginToken = null;
	@Column(length=4)
	private String xhdrEndToken = null;
	@Column(nullable=false)
	private int maxLength = -1;

	public IdTokens() {
		// must have a no-argument constructor
	}

	public SenderData getSenderData() {
		return senderData;
	}

	public void setSenderData(SenderData senderData) {
		this.senderData = senderData;
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
