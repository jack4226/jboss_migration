package jpa.model.msg;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="msg_unread_count")
public class MsgUnreadCount implements java.io.Serializable {
	private static final long serialVersionUID = -1386088526586388727L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="row_id", updatable=false)
	protected Integer rowId;
	
	@Column(nullable=false, columnDefinition="int")
	private int inboxUnreadCount = 0;
	
	@Column(nullable=false, columnDefinition="int")
	private int sentUnreadCount = 0;

	
	public Integer getRowId() {
		return rowId;
	}

	public int getInboxUnreadCount() {
		return inboxUnreadCount;
	}

	public void setInboxUnreadCount(int inboxUnreadCount) {
		this.inboxUnreadCount = inboxUnreadCount;
	}

	public int getSentUnreadCount() {
		return sentUnreadCount;
	}

	public void setSentUnreadCount(int sentUnreadCount) {
		this.sentUnreadCount = sentUnreadCount;
	}
}
