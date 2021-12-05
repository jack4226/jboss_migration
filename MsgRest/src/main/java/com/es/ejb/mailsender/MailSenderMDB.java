package com.es.ejb.mailsender;

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.annotation.Resource.AuthenticationType;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import jpa.message.MessageBean;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Message-Driven Bean implementation class for: MailSenderMDB
 *
 */
@MessageDriven(
	activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") 
		,@ActivationConfigProperty(propertyName = "destination", propertyValue = "mailOutboxQueue")
		,@ActivationConfigProperty(propertyName = "maxSessions", propertyValue = "4")
		},
	mappedName = "mailOutboxQueue")
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Resource(name = "msgdb_pool",mappedName = "jdbc/MessageDS",
	type = javax.sql.DataSource.class,
	authenticationType = AuthenticationType.CONTAINER)
@EJB(name="MailSender",beanInterface=MailSenderLocal.class)
public class MailSenderMDB implements MessageListener {
	static final Logger logger = LogManager.getLogger(MailSenderMDB.class);
	final String LF = System.getProperty("line.separator", "\n");
	
    @Resource
    private MessageDrivenContext messageContext;

	@Resource
    private ConnectionFactory connectionFactory;

    @Resource(name = "mailErrorQueue")
    private Queue errorQueue;
    
    @javax.ejb.EJB
    private MailSenderLocal mailSender;

    private static final int MAX_DELIVERY_COUNT = 4;

    /**
     * Default constructor. 
     */
    public MailSenderMDB() {
    	logger.info("In MailSenderMDB.constructor().");
    }

    /**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
		logger.debug("Message Driven Bean got a message: " + LF + message);

		try {
			int deliveryCount = 0;
			Enumeration<?> properties_enu;
			if (message instanceof ActiveMQMessage) {
				ActiveMQMessage msg = (ActiveMQMessage) message;
				deliveryCount = msg.getRedeliveryCounter() + 1;
				logger.info("ActiveMQ RedeliveryCounter: " + msg.getRedeliveryCounter());
				properties_enu = msg.getAllPropertyNames();
			}
			else {
				properties_enu = message.getPropertyNames();
			}
			//if (message.getJMSRedelivered()) {
				// find vendor specific JMS delivery count property
				while (properties_enu.hasMoreElements()) {
					String propName = (String) properties_enu.nextElement();
					if (StringUtils.isBlank(propName)) {
						continue;
					}
					logger.info("JMS Property name: " + propName + " = " + message.getObjectProperty(propName));
					Pattern p = Pattern.compile("jms\\w{0,}delivery\\w{0,}count", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
					Matcher m = p.matcher(propName);
					if (m.find()) {
						deliveryCount = message.getIntProperty(propName);
						logger.info(propName + ": " + deliveryCount);
					}
				}
			//}
			if (deliveryCount > 1) {
				if (MAX_DELIVERY_COUNT > 0 && deliveryCount > MAX_DELIVERY_COUNT) {
					sendToErrorQueue(message, new JMSException(
							"DeliveryCount exceeded Listener's maximum: "
									+ MAX_DELIVERY_COUNT));
					return;
				}
			}
		}
		catch (JMSException e) {
			logger.error("JMSException caught", e);
		}
		
		MessageBean msgBean = null;
		try {
			//String jndiName = "MailSenderLocal"; // use injection instead
			//MailSenderLocal mailSender = (MailSenderLocal) TomeeCtxUtil.getLocalContext().lookup(jndiName);
			logger.info("MailSender instance: " + mailSender);
			
			if (message instanceof ObjectMessage) {
				Object msgObj = ((ObjectMessage) message).getObject();
				if (msgObj == null) {
					throw new MessageFormatException("Object Message is Null.");
				}
				if (msgObj instanceof MessageBean) {
					msgBean = (MessageBean) msgObj;
					logger.info("A MessageBean object received.");
					
					mailSender.send(msgBean);
				}
				else {
					logger.error("message was not a MessageBean as expected" + LF + message);
				}
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
				
				mailSender.send(mailStream);
			}
			else if (message instanceof TextMessage) {
				logger.warn("A TextMessage received: " + ((TextMessage)message).getText());
				sendToErrorQueue(message, new Exception("Not expected message type: TestMessage"));
			}
		}
//		catch (NamingException ne) {
//			logger.error("NamingException caught", ne);
//			messageContext.setRollbackOnly();
//			throw new EJBException("Failed to lookup jndi: " + jndiName, ne);
//		}
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
    
	private void sendToErrorQueue(javax.jms.Message message, Exception exception) {
		logger.info("Entering sendToErrorQueue()..., " + exception);
		
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		try {
			message.setJMSCorrelationID(message.getJMSMessageID());

			message.clearProperties();
			message.setStringProperty("UnhandledError", ExceptionUtils.getFullStackTrace(exception));
			
			connection = connectionFactory.createConnection();
			
			connection.start();
			logger.info("In MailSenderMDB.sendToErrorQueue() - JMS Connection started");
			
	        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

	        producer = session.createProducer(errorQueue);
	        producer.setDeliveryMode(DeliveryMode.PERSISTENT);
	        
	        producer.send(message);

			logger.info("Message written to Error Queue: " + producer.getDestination());
		} 
		catch (JMSException e) {
			logger.error("Failled to send message to error queue - ", e);
		}
		finally {
			if (producer!=null) {
				try {
					producer.close();
				}
				catch (JMSException e) {
					logger.error("JMSException caught: " + e.getMessage());
				}
			}
			if (session!=null) {
				try {
					session.close();
				}
				catch (JMSException e) {
					logger.error("JMSException caught: " + e.getMessage());
				}
			}
			if (connection!=null) {
				try {
					connection.close();
				}
				catch (JMSException e) {
					logger.error("JMSException caught: " + e.getMessage());
				}
			}
		}
	}

}
