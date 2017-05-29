package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

import jpa.model.BaseModel;

@Entity
@Table(name="message_stream"
	, indexes = {
		@Index(columnList = "FromAddrRowId", name = "MsgStream_FromAddr_Idx")
	}
	, uniqueConstraints=@UniqueConstraint(columnNames = {"MessageInboxRowId"}))
@SqlResultSetMappings({ // used by native queries
	  @SqlResultSetMapping(name="MessageStreamNative",
		entities={
		 @EntityResult(entityClass=MessageStream.class),
	  	}
	  	),
	})
public class MessageStream extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 6941824800672413874L;
	@Transient
	public static final String MAPPING_MESSAGE_STREAM = "MessageStreamNative";

	@OneToOne(fetch=FetchType.LAZY, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="row_id", nullable=false,
			table="message_stream", foreignKey=@ForeignKey(name="FK_message_stream_MessageInboxRowId"))
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	@XmlTransient
	private MessageInbox messageInbox;

//	@SuppressWarnings("deprecation")
//	@org.eclipse.persistence.annotations.Index
//	@org.hibernate.annotations.Index(name="MsgStream_FromAddr_Idx")
	@Column(name="FromAddrRowId", nullable=true)
	private int fromAddrRowId;

	@Column(name="ToAddrRowId", nullable=true)
	private int toAddrRowId;

	@Column(length=255, nullable=true)
	private String msgSubject = null;
	
	@Lob
	@Column(length=262136, nullable=true) // <= 262144(256k) - 8
	private byte[] msgStream = null;
	
	public MessageStream() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public int getFromAddrRowId() {
		return fromAddrRowId;
	}

	public void setFromAddrRowId(int fromAddrRowId) {
		this.fromAddrRowId = fromAddrRowId;
	}

	public int getToAddrRowId() {
		return toAddrRowId;
	}

	public void setToAddrRowId(int toAddrRowId) {
		this.toAddrRowId = toAddrRowId;
	}

	public String getMsgSubject() {
		return msgSubject;
	}

	public void setMsgSubject(String msgSubject) {
		this.msgSubject = msgSubject;
	}

	public byte[] getMsgStream() {
		return msgStream;
	}

	public void setMsgStream(byte[] msgStream) {
		this.msgStream = msgStream;
	}

}
