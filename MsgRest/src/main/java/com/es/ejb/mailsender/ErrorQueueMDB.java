package com.es.ejb.mailsender;

import java.io.ByteArrayOutputStream;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.BytesMessage;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.log4j.Logger;

import jpa.util.PrintUtil;

/**
 * Message-Driven Bean implementation class for: MailSenderMDB
 *
 */
@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") 
		,@ActivationConfigProperty(propertyName = "destination", propertyValue = "mailErrorQueue")
		,@ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "1")
		},
	mappedName = "mailErrorQueue")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public class ErrorQueueMDB implements MessageListener {
	static final Logger logger = Logger.getLogger(ErrorQueueMDB.class);
	final String LF = System.getProperty("line.separator", "\n");
	
    @Resource
    private MessageDrivenContext messageContext;

	@Resource
    private ConnectionFactory connectionFactory;
	
    /**
     * Default constructor. 
     */
    public ErrorQueueMDB() {
    	logger.info("In ErrorQueueMDB.constructor().");
    }

    /**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
		logger.debug("Message Driven Bean got a message: " + LF + message);
		
		try {
			if (message instanceof ObjectMessage) {
				Object msgObj = ((ObjectMessage) message).getObject();
				if (msgObj == null) {
					throw new MessageFormatException("Object Message is Null.");
				}
				logger.info("Object Message: " + PrintUtil.prettyPrint(msgObj));
			}
			else if (message instanceof BytesMessage) {
				// SMTP raw stream
				BytesMessage msg = (BytesMessage) message;
				logger.info("A BytesMessage received.");
				byte[] buffer = new byte[1024];
				int len = 0;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				while ( (len = msg.readBytes(buffer)) > 0) {
					baos.write(buffer, 0, len);
				}
				byte[] mailStream = baos.toByteArray();
				
				logger.info("Bytes Message: " + new String(mailStream));
			}
			else if (message instanceof TextMessage) {
				logger.info("A TextMessage received: " + ((TextMessage)message).getText());
			}
			else {
				logger.warn("Unknown Message Type: " + PrintUtil.prettyPrint(message));
			}
		}
		catch (JMSException je) {
			logger.error("JMSException caught", je);
			Exception e = je.getLinkedException();
			if (e != null) {
				logger.error("linked error", e);
			}
			messageContext.setRollbackOnly();
			throw new EJBException(je.getMessage(), je);
		}
    }
}
