package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlTransient;

@Embeddable
public class MessageActionLogPK implements Serializable {
	private static final long serialVersionUID = 2827238892173739680L;

	@ManyToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private MessageInbox messageInbox;

	@Column(name="LeadMessageRowId", nullable=false)
	private int leadMessageRowId = -1;

	public MessageActionLogPK() {}
	
	public MessageActionLogPK(MessageInbox messageInbox, int leadMessageId) {
		this.messageInbox = messageInbox;
		this.leadMessageRowId = leadMessageId;
	}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getLeadMessageRowId() {
		return leadMessageRowId;
	}

	public void setLeadMessageRowId(int leadMessageRowId) {
		this.leadMessageRowId = leadMessageRowId;
	}

}