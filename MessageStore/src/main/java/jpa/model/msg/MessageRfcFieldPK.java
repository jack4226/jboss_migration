package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

@Embeddable
public class MessageRfcFieldPK implements Serializable {
	private static final long serialVersionUID = -9184530957952906331L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private MessageInbox messageInbox;

	@Column(length=50, nullable=false)
	private String rfcType = "";

	public MessageRfcFieldPK() {}
	
	public MessageRfcFieldPK(MessageInbox messageInbox,  String rfcType) {
		this.messageInbox = messageInbox;
		this.rfcType = rfcType;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public String getRfcType() {
		return rfcType;
	}

	public void setRfcType(String rfcType) {
		this.rfcType = rfcType;
	}

}