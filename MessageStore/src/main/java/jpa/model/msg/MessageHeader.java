package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

@Entity
@Table(name="message_header", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "headerSequence"}))
public class MessageHeader extends BaseModel implements Serializable
{
	private static final long serialVersionUID = -6910801978360656523L;

	@Embedded
	private MessageHeaderPK messageHeaderPK;

	@Column(length=100, nullable=true)
	private String headerName = null;
	@Lob
	@Column(length=32700, nullable=true)
	private String headerValue = null;

	public MessageHeader() {}

	public MessageHeaderPK getMessageHeaderPK() {
		return messageHeaderPK;
	}

	public void setMessageHeaderPK(MessageHeaderPK messageHeaderPK) {
		this.messageHeaderPK = messageHeaderPK;
	}

	public String getHeaderName() {
		return headerName;
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public String getHeaderValue() {
		return headerValue;
	}

	public void setHeaderValue(String headerValue) {
		this.headerValue = headerValue;
	}
}
