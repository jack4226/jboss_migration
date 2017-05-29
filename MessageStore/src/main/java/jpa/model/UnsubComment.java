package jpa.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name="unsub_comment")
@XmlRootElement(name="unsubComment")
public class UnsubComment extends BaseModel implements java.io.Serializable {
	private static final long serialVersionUID = 6944693180570837420L;

	@ManyToOne(targetEntity=EmailAddress.class, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="EmailAddrRowId", insertable=true, referencedColumnName="row_id", nullable=false)
	@XmlTransient
	private EmailAddress emailAddress;

	@ManyToOne(targetEntity=MailingList.class, fetch=FetchType.EAGER, optional=false)
	@JoinColumn(name="MailingListRowId", insertable=true, referencedColumnName="row_id", nullable=true)
	@XmlTransient
	private MailingList mailingList;

	@Column(nullable=false, length=2046)
	private String comments = "";

	public UnsubComment() {
		// must have a no-argument constructor
	}

	public EmailAddress getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(EmailAddress emailAddr) {
		this.emailAddress = emailAddr;
	}

	public MailingList getMailingList() {
		return mailingList;
	}

	public void setMailingList(MailingList mailingList) {
		this.mailingList = mailingList;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
