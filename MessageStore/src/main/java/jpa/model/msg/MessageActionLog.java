package jpa.model.msg;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import jpa.model.BaseModel;

@Entity
@Table(name="message_action_log", uniqueConstraints={@UniqueConstraint(columnNames = {"MessageInboxRowId", "LeadMessageRowId"})})
public class MessageActionLog extends BaseModel implements Serializable
{
	private static final long serialVersionUID = 60873582256305774L;

	@Embedded
	private MessageActionLogPK messageActionLogPK;

	@Column(length=50, nullable=false)
	private String actionService = "";
	@Column(length=255, nullable=true)
	private String parameters = null;

	public MessageActionLog() {}

	public MessageActionLogPK getMessageActionLogPK() {
		return messageActionLogPK;
	}

	public void setMessageActionLogPK(MessageActionLogPK messageActionLogPK) {
		this.messageActionLogPK = messageActionLogPK;
	}

	public String getActionService() {
		return actionService;
	}

	public void setActionService(String actionService) {
		this.actionService = actionService;
	}

	public String getParameters() {
		return parameters;
	}

	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
}
