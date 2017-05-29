package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlTransient;

import jpa.model.BaseModel;
import jpa.model.EmailAddress;
import jpa.model.MailingList;

@Entity
@Table(name="message_unsub_comment", uniqueConstraints=@UniqueConstraint(name= "UK_MsgUnsubCmt_MsgInbox", columnNames = {"MessageInboxRowId"}))
public class MessageUnsubComment extends BaseModel implements Serializable
{
	private static final long serialVersionUID = -8816644697506979193L;

	@OneToOne(fetch=FetchType.EAGER, optional=false, targetEntity=MessageInbox.class)
	@JoinColumn(name="MessageInboxRowId", insertable=true, referencedColumnName="row_id", nullable=false,
			table="message_unsub_comment", foreignKey=@ForeignKey(name="FK_MsgUnsubCmt_MessageInboxRowId"))
	@org.hibernate.annotations.OnDelete(action=org.hibernate.annotations.OnDeleteAction.CASCADE)
	@XmlTransient
	private MessageInbox messageInbox;

	@ManyToOne(targetEntity=MailingList.class, fetch=FetchType.EAGER, optional=true)
	@JoinColumn(name="MailingListRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=true,
			table="message_unsub_comment", foreignKey=@ForeignKey(name="FK_MsgUnsubCmt_MailingLstRowId"))
	@XmlTransient
	private MailingList mailingList;

	@ManyToOne(targetEntity=EmailAddress.class, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="EmailAddrRowId", insertable=true, updatable=true, referencedColumnName="row_id", nullable=true,
			table="message_unsub_comment", foreignKey=@ForeignKey(name="FK_MsgUnsubCmt_EmailAddrRowId"))
	@XmlTransient
	private EmailAddress emailAddr;

	@Column(length=1000, nullable=false)
	private String comments = "";

	public MessageUnsubComment() {}

	public MessageInbox getMessageInbox() {
		return messageInbox;
	}

	public void setMessageInbox(MessageInbox messageInbox) {
		this.messageInbox = messageInbox;
	}

	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
	}

	public EmailAddress getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(EmailAddress emailAddr) {
		this.emailAddr = emailAddr;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}
}
