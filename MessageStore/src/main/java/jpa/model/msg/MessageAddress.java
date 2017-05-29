package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

import jpa.constant.EmailAddrType;
import jpa.model.BaseModel;

@Entity
@Table(name="message_address", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "addressType", "emailAddrRowId"}))
public class MessageAddress extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 4120242394404262528L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private MessageInbox messageInbox;

	@Column(length=12, nullable=false)
	private String addressType = EmailAddrType.FROM_ADDR.getValue();

	@Column(name="EmailAddrRowId", nullable=false)
	private int emailAddrRowId;

	public MessageAddress() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public String getAddressType() {
		return addressType;
	}

	public void setAddressType(String addressType) {
		this.addressType = addressType;
	}

	public int getEmailAddrRowId() {
		return emailAddrRowId;
	}

	public void setEmailAddrRowId(int emailAddrRowId) {
		this.emailAddrRowId = emailAddrRowId;
	}
}
