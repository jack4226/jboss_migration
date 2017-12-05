package com.es.mdb.test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.embeddable.EJBContainer;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;

public class ErrorQueueMDBTest extends TestCase {
	protected final static Logger logger = Logger.getLogger(ErrorQueueMDBTest.class);
	
	@Resource //(name = "connectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(name = "mailErrorQueue")
    private Queue mailErrorQueue;

	public void testMDB() {
		logger.info("In ErrorQueueMDBTest.testMDB()...");
		Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
        EJBContainer container = null;
        Context context = null;
		try {
			container = EJBContainer.createEJBContainer(properties);
			context = container.getContext();
			context.bind("inject", this);
			
			final Connection connection = connectionFactory.createConnection();
			
			connection.start();
			logger.info("In ErrorQueueMDBTest.testMDB() - JMS Connection started");
			
	        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	
	        final MessageProducer producer = session.createProducer(mailErrorQueue);
	        
	        sendText(ErrorQueueMDBTest.class.getSimpleName() + " - Test message.", producer, session);
	        logger.info("In ErrorQueueMDBTest.testMDB() - JMS Message Sent!");
	        
	        producer.close();
			session.close();
			connection.close();
			
			logger.info("########## Waiting for 10 seconds for the messages to be consumed... %%%%%%%%%%");
	        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
		}
		finally {
			if (context != null) {
				try {
					context.close();
				} catch (Exception e) {
					logger.error("Exception caught: " + e.getMessage());
				}
			}
			if (container != null) {
				container.close();
			}
		}
	}
	
	private void sendText(String text, MessageProducer producer, Session session) throws JMSException {
		TextMessage msg = session.createTextMessage(text);
		msg.setBooleanProperty(ErrorQueueMDBTest.class.getSimpleName(), Boolean.TRUE);
		producer.send(msg);
	}

}
