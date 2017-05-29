package jpa.jbatch.queue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.core.MessageCreator;

/**
 * sub-classed from MessageCreator to provide access to JmsMessageId
 */
public abstract class SendMessageCreator implements MessageCreator {
	
	private Message message; // XXX make sure to set it in createMessage
	
	public abstract Message createMessage(Session session) throws JMSException;
	
	public String getJMSMessageId() throws JMSException {
		if (message != null) {
			return message.getJMSMessageID();
		}
		else {
			return null;
		}
	}
	
	void setMessage(Message msg) {
		this.message = msg;
	}
}
