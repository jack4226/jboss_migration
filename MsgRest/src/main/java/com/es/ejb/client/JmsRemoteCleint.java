package com.es.ejb.client;

import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jpa.tomee.util.TomeeCtxUtil;

public class JmsRemoteCleint {
	static Logger logger = LogManager.getLogger(JmsRemoteCleint.class);
	
	public static void main(String[] args) {
		try {
			JmsRemoteCleint client = new JmsRemoteCleint();
			client.testProducer();
			client.testComsumer();
		}
		catch (Exception e) {
			logger.error("Exception", e);
		}
	}

	void testProducer() throws NamingException, JMSException {
		String queueName = "mailOutboxQueue";
		Context ctx = TomeeCtxUtil.getActiveMQContext(new String[] {queueName});
		//TomeeCtxUtil.listContext(ctx, "");
		ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		logger.info("ConnectionFactory instance: " + cf);
		
		Connection connection = null;
		Session session = null;
		MessageProducer producer = null;
		try {
			connection = cf.createConnection();
			
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Destination queue = (Destination)ctx.lookup(queueName);
			logger.info("Queue instance: " + queue);
			
			producer = session.createProducer(queue);
			
			producer.send(session.createTextMessage("Hello from remote client!"));
			logger.info("Text message is sent to " + queue);
		}
		catch (JMSException e) {
			throw e;
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
	
	void testComsumer() throws NamingException, JMSException {
		String queueName = "mailErrorQueue";
		Context ctx = TomeeCtxUtil.getActiveMQContext(queueName);
		//TomeeCtxUtil.listContext(ctx, "");
		ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		logger.info("ConnectionFactory instance: " + cf);
		
		Connection connection = null;
		Session session = null;
		MessageConsumer consumer = null;
		try {
			connection = cf.createConnection();
			
			connection.start();
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			Destination queue = (Destination)ctx.lookup(queueName);
			logger.info("Queue instance: " + queue);
			
			consumer = session.createConsumer(queue);
			
			Message msg = consumer.receive(TimeUnit.SECONDS.toMillis(2));
			if (msg!=null && msg.propertyExists("UnhandledError")) {
				logger.info("UnhandledError property found:\n" + msg.getStringProperty("UnhandledError"));
			}
			if (msg instanceof TextMessage) {
				logger.info("Text message received: " + ((TextMessage)msg).getText());
			}
			else {
				logger.info("JMS Message received: " + msg);
			}
		}
		catch (JMSException e) {
			throw e;
		}
		finally {
			if (consumer!=null) {
				try {
					consumer.close();
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
