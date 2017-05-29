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
@Table(name="message_delivery_status", uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId", "FinalRcptAddrRowId"}))
public class MessageDeliveryStatus extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 3655196488523414559L;

	@Embedded
	private MessageDeliveryStatusPK messageDeliveryStatusPK;

	@Column(length=255, nullable=true)
	private String finalRecipientAddress = null;
	@Column(nullable=true)
	private Integer originalRcptAddrRowId = null;
	@Column(length=255, nullable=true)
	private String smtpMessageId = null;
	@Column(nullable=false)
	private int receivedCount = 0;
	@Column(length=50, nullable=true)
	private String dsnStatus = null;
	@Column(length=255, nullable=true)
	private String dsnReason = null;
	@Lob
	@Column(length=32700, nullable=true)
	private String dsnText = null;
	@Lob
	@Column(length=32700, nullable=true)
	private String deliveryStatus = null;

	public MessageDeliveryStatus() {}

	public MessageDeliveryStatusPK getMessageDeliveryStatusPK() {
		return messageDeliveryStatusPK;
	}

	public void setMessageDeliveryStatusPK(
			MessageDeliveryStatusPK messageDeliveryStatusPK) {
		this.messageDeliveryStatusPK = messageDeliveryStatusPK;
	}

	public String getFinalRecipientAddress() {
		return finalRecipientAddress;
	}

	public void setFinalRecipientAddress(String finalRecipientAddress) {
		this.finalRecipientAddress = finalRecipientAddress;
	}

	public Integer getOriginalRcptAddrRowId() {
		return originalRcptAddrRowId;
	}

	public void setOriginalRcptAddrRowId(Integer originalRcptAddrRowId) {
		this.originalRcptAddrRowId = originalRcptAddrRowId;
	}

	public String getSmtpMessageId() {
		return smtpMessageId;
	}

	public void setSmtpMessageId(String smtpMessageId) {
		this.smtpMessageId = smtpMessageId;
	}

	public int getReceivedCount() {
		return receivedCount;
	}

	public void setReceivedCount(int receivedCount) {
		this.receivedCount = receivedCount;
	}

	public String getDsnStatus() {
		return dsnStatus;
	}

	public void setDsnStatus(String dsnStatus) {
		this.dsnStatus = dsnStatus;
	}

	public String getDsnReason() {
		return dsnReason;
	}

	public void setDsnReason(String dsnReason) {
		this.dsnReason = dsnReason;
	}

	public String getDsnText() {
		return dsnText;
	}

	public void setDsnText(String dsnText) {
		this.dsnText = dsnText;
	}

	public String getDeliveryStatus() {
		return deliveryStatus;
	}

	public void setDeliveryStatus(String deliveryStatus) {
		this.deliveryStatus = deliveryStatus;
	}
}
