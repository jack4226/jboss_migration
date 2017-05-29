package jpa.model.msg;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="messageid_duplicate")
public class MessageIdDuplicate implements Serializable
{
	private static final long serialVersionUID = -1917699145122455676L;

	@Id
	@Column(name="MessageId", length=255, nullable=false)
	private String messageId;
	@Column(name="AddTime", length=3, nullable=false)
	protected Timestamp addTime = null;

	public MessageIdDuplicate() {}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public Timestamp getAddTime() {
		return addTime;
	}

	public void setAddTime(Timestamp addTime) {
		this.addTime = addTime;
	}
}
