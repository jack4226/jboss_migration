package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

@Embeddable
public class MessageHeaderPK implements Serializable {
	private static final long serialVersionUID = -8974001276891830442L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="row_id", nullable=false,
			table="message_header", foreignKey=@ForeignKey(name="FK_message_header_MessageInboxRowId"))
	@XmlTransient
	private MessageInbox messageInbox;

	@Column(name="HeaderSequence", nullable=false)
	private int headerSequence = -1;

	public MessageHeaderPK() {}
	
	public MessageHeaderPK(MessageInbox messageInbox, int headerSequence) {
		this.messageInbox = messageInbox;
		this.headerSequence = headerSequence;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getHeaderSequence() {
		return headerSequence;
	}

	public void setHeaderSequence(int headerSequence) {
		this.headerSequence = headerSequence;
	}

}