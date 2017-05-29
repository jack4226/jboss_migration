package jpa.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SessionUploadPK implements Serializable {
	private static final long serialVersionUID = 6797910190578681220L;

	@Column(nullable=false, length=50)
	private String sessionId = "";
	@Column(nullable=false)
	private int sessionSequence = -1;

	public SessionUploadPK() {}
	
	public SessionUploadPK(String sessionId, int sessionSequence) {
		this.sessionId = sessionId;
		this.sessionSequence = sessionSequence;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getSessionSequence() {
		return sessionSequence;
	}

	public void setSessionSequence(int sessionSequence) {
		this.sessionSequence = sessionSequence;
	}

}