package jpa.jbatch.queue;

import java.util.Enumeration;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import jpa.spring.util.SpringUtil;

/**
 * Implements spring MessageListener
 */
public class JmsListener implements MessageListener {
	static final Logger logger = Logger.getLogger(JmsListener.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Autowired
	private JmsProcessor jmsProcessor;
	
	@Override
	public void onMessage(Message message) {
		logger.info("JMS Message Received: "+message);
		try {
			if (message.getJMSRedelivered()) {
				Enumeration<?> enu = message.getPropertyNames();
				while (enu.hasMoreElements()) {
					String name = (String) enu.nextElement();
					logger.info("Propertiy Name: " + name);
					logger.info("String Property: " + message.getStringProperty(name));
					logger.info("Int Property: " + message.getIntProperty(name));
				}
				if (message.getIntProperty("JMSXDeliveryCount") > 10) {
					jmsProcessor.writeMsg(message, message.getJMSMessageID(), true);
					return;
				}
			}
			if (message instanceof TextMessage) {
				String msgText = ((TextMessage) message).getText();
				logger.info(msgText);
				try {
					String msgId = jmsProcessor.writeMsg(msgText);
					logger.info("JMSMessageId: " + msgId);
				}
				catch (MessageFormatException mfe) {
					logger.error("MessageFormatException caught, write to error queue...");
					jmsProcessor.writeMsg(msgText,message.getJMSMessageID(), true);
				}
				//throw new JMSException("Test Exception");
			}
			else if (message instanceof BytesMessage) {
				BytesMessage byteMsg = (BytesMessage) message;
				int msgLength = 0;
				int BUF_SIZE = 50000;
				byte[] data = new byte[BUF_SIZE];
				while (true) {
					int len = byteMsg.readBytes(data);
					if (len > 0) {
						msgLength += len;
					}
					else {
						break;
					}
				}
				// now we know the message length
				// so reset and read in one go.
				byte[] msg = new byte[msgLength];
				// if msgLength <= BUF_SIZE, then we already
				// have the contents
				if (msgLength <= BUF_SIZE) {
					System.arraycopy(data, 0, msg, 0, msgLength);
				}
				else {
					byteMsg.reset(); // reset cursor to beginning
					byteMsg.readBytes(msg);
				}
				jmsProcessor.writeMsg(byteMsg, null, false);
			}
			else {
				throw new IllegalArgumentException(
						"Message must be of type TextMessage, invalid message type received: "
								+ message.getClass().getName());
			}
		}
		catch (JmsException | JMSException ex) {
			throw new RuntimeException(ex);
		}
		finally {
			//
		}
	}
	
	public static void main(String[] args) {
		DefaultMessageListenerContainer listener = (DefaultMessageListenerContainer) SpringUtil.getAppContext()
				.getBean("jmsListenerContainer");
		listener.start();
		try {
			Thread.sleep(10 * 1000);
		} catch (InterruptedException e) {}
		listener.stop();
		
		System.exit(0);
	}

}