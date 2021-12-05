package jpa.jbatch.queue;

import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;

import jpa.exception.DataValidationException;
import jpa.message.MessageBean;
import jpa.message.MessageContext;
import jpa.service.msgin.MessageParserBo;
import jpa.service.task.TaskSchedulerBo;

public class RuleEngineListener implements MessageListener {
	static final Logger logger = LogManager.getLogger(RuleEngineListener.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();

	@Autowired
	private JmsProcessor jmsProcessor;
	@Autowired
	private TaskSchedulerBo dispatcher;
	@Autowired
	private MessageParserBo parser;
	
	private @Value("${ruleEngineError.Queue}") String ruleEngineErrorQueue;
	private @Value("${errorOutput.Queue}") String errorQueueName;
	
	public RuleEngineListener() {
		logger.info("Entering construct...");
	}
	
	@Override
	public void onMessage(Message message) {
		logger.info("JMS Message Received: " + message);
		jmsProcessor.setQueueName(ruleEngineErrorQueue);
		long start = System.currentTimeMillis();
		try {
			String JmsMessageId = message.getJMSMessageID();
			if (message instanceof ObjectMessage) {
				Object obj = ((ObjectMessage) message).getObject();
				if (obj instanceof MessageBean) {
					MessageBean messageBean = (MessageBean) obj;
					parser.parse(messageBean); // for RuleName
					try {
						dispatcher.scheduleTasks(new MessageContext(messageBean));
					} catch (MessagingException e) {
						logger.error("onMessage() - MessageFormatException caught", e);
						jmsProcessor.writeMsg(messageBean, JmsMessageId, true);
					} catch (DataValidationException e) {
						logger.error("onMessage() - DataValidationException caught", e);
						jmsProcessor.writeMsg(messageBean, JmsMessageId, true);
					} catch (DuplicateKeyException e) {
						logger.error("onMessage() - DuplicateKeyException caught, message ignored.", e);
					}
				}
				else {
					// Not a MessageBean instance
					logger.warn("Message object is not a MessageBean, cless name: " + obj.getClass().getName());
					jmsProcessor.writeMsg(message, JmsMessageId, true);
				}
			}
			else {
				// Not an Object Message
				logger.warn("Message received is not an ObjectMessage: " + message.getClass().getName());
			}
		}
		catch (JMSException je) {
			logger.error("onMessage() - JMSException caught", je);
			throw new RuntimeException(je);
		}
		catch (Throwable e) {
			logger.error("onMessage() - Throwable caught", e);
			throw new RuntimeException(e);
		}
		finally {
			/* Message processed, update processing time */
			long proc_time = System.currentTimeMillis() - start;
			logger.info("onMessage() ended. Time spent in milliseconds: " + proc_time);
		}
	}

	@PreDestroy
	public void destroy() {
		logger.warn("Entering @PreDestroy destroy() method...");
	}
}
