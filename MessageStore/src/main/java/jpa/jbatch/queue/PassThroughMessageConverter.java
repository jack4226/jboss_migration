package jpa.jbatch.queue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;

public class PassThroughMessageConverter implements MessageConverter {

	public Object fromMessage(Message msg) throws JMSException, MessageConversionException {
		return msg;
	}

	public Message toMessage(Object msg, Session session) throws JMSException, MessageConversionException {
		return (Message) msg;
	}
}
