package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class MailInboxPK implements Serializable {
	private static final long serialVersionUID = -7248137139815272498L;

	@Column(nullable=false, length=30)
	private String userId = ""; 
	@Column(nullable=false, length=100)
	private String hostName = "";

	public MailInboxPK() {}
	
	public MailInboxPK(String userId, String hostName) {
		this.userId = userId;
		this.hostName = hostName;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	@Override
	public String toString() {
		return (userId+ "@" +hostName);
	}
}