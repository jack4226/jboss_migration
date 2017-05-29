package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

@Entity
@Table(name="message_attachment", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "attachmentDepth", "attachmentSequence"}))
public class MessageAttachment extends BaseModel implements Serializable
{
	private static final long serialVersionUID = -2228140043531630257L;

	@Embedded
	private MessageAttachmentPK messageAttachmentPK;

	@Column(length=100, nullable=true)
	private String attachmentName = null;
	@Column(length=100, nullable=true)
	private String attachmentType = null;
	@Column(length=100, nullable=true)
	private String attachmentDisp = null;
	@Lob
	@Column(length=32700, nullable=true)
	private byte[] attachmentValue = null;

	public MessageAttachment() {}

	/** Define UI Components */
	@Transient
	private int attachmentSize = 0;
	
	public int getAttachmentSize() {
		if (attachmentValue == null) {
			return attachmentSize;
		}
		else {
			return attachmentValue.length;
		}
	}
	
	public String getSizeAsString() {
		int len = getAttachmentSize();
		if (len < 1024) {
			return 1024 + "";
		}
		else {
			return (int) Math.ceil((double)len / 1024.0) + "K";
		}
	}

	public void setAttachmentSize(int size) {
		attachmentSize = size;
	}
	/** End of UI Components */

	public MessageAttachmentPK getMessageAttachmentPK() {
		return messageAttachmentPK;
	}

	public void setMessageAttachmentPK(MessageAttachmentPK messageAttachmentPK) {
		this.messageAttachmentPK = messageAttachmentPK;
	}

	public String getAttachmentName() {
		return attachmentName;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public String getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(String attachmentType) {
		this.attachmentType = attachmentType;
	}

	public String getAttachmentDisp() {
		return attachmentDisp;
	}

	public void setAttachmentDisp(String attachmentDisp) {
		this.attachmentDisp = attachmentDisp;
	}

	public byte[] getAttachmentValue() {
		return attachmentValue;
	}

	public void setAttachmentValue(byte[] attachmentValue) {
		this.attachmentValue = attachmentValue;
	}
}
