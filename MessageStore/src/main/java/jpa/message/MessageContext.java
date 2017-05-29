package jpa.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import jpa.model.MailInbox;

public class MessageContext implements Serializable {
	private static final long serialVersionUID = -8429972515707390401L;

	private javax.mail.Message[] messages;
	private MailInbox mailInbox;
	private MessageBean messageBean;
	private byte[] messageStream;
	private String taskArguments;
	private List<Integer> rowIds;
	
	public MessageContext() {}
	
	public MessageContext(@NotNull javax.mail.Message[] messages, @NotNull MailInbox mailInbox) {
		this.messages = messages;
		this.mailInbox = mailInbox;
	}

	public MessageContext(@NotNull MessageBean messageBean) {
		this.messageBean = messageBean;
	}

	public MessageContext(byte[] messageStream) {
		this.messageStream = messageStream;
	}

	public javax.mail.Message[] getMessages() {
		return messages;
	}

	public void setMessages(javax.mail.Message[] messages) {
		this.messages = messages;
	}

	public MailInbox getMailInbox() {
		return mailInbox;
	}

	public void setMailInbox(MailInbox mailInbox) {
		this.mailInbox = mailInbox;
	}

	public MessageBean getMessageBean() {
		return messageBean;
	}

	public void setMessageBean(MessageBean messageBean) {
		this.messageBean = messageBean;
	}

	public byte[] getMessageStream() {
		return messageStream;
	}

	public String getTaskArguments() {
		return taskArguments;
	}

	public void setMessageStream(byte[] messageStream) {
		this.messageStream = messageStream;
	}

	public void setTaskArguments(String taskArguments) {
		this.taskArguments = taskArguments;
	}

	public List<Integer> getRowIds() {
		if (rowIds == null) {
			rowIds = new ArrayList<Integer>();
		}
		return rowIds;
	}
}
