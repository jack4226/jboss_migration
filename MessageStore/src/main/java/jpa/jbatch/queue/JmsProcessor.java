package jpa.jbatch.queue;

import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

/**
 * <pre>
 * Not implemented yet.
 * About Retry. A preliminary analysis concluded that the retry should be performed
 * on the Session level, and for JMS reads/writes only.
 * 
 * A likely implementation:
 * 1) Introduce a new instance variable: tranCount, initialized to zero
 * 2) Whenever a queue read/write is performed, tranCount++
 * 3) Whenever a commit/close is performed, reset tranCount to zero
 * 4) Whenever a JMSException is caught from read/write
 * 		AND linkedException!=null
 * 		AND is a com.ibm.mq.MQException
 *  	AND tranCount==0 (not in a middle of a transaction):
 * 	a) issue close(), ignore JMSException
 *  b) issue open() using this.msgType and this.msgSelector
 *  c) perform retry: reissue JMS read/write
 * 	d) throw Exception to the calling program if any
 * </pre>
 */

/**
 * This class provides implementation of JMS read and write operations. It uses
 * Spring frameworks's MessageCreator to construct JMS message and JmsTemplate to
 * deliver the message. It is designed to be thread safe.
 */
@Component
@Scope(value="prototype")
@Lazy(value=false)
public class JmsProcessor {
	
	static final Logger logger = Logger.getLogger(JmsProcessor.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	private String msgType = "JMS";
	private String msgSelector = null;

	/*
	 * experimental: on Read operations only - if error occurred during read and
	 * the number of errors is under ErrorMsgLimit, write the message to error
	 * queue instead of throwing JMSException - reset the ErrorMsgCount to zero
	 * if 10 consecutive good messages are received.
	 * 
	 * The purpose of this is that we'd better stopping the queue reader rather
	 * than flooding the error queue by unknown or bad messages. 
	 */
	int ErrorMsgLimit = 0, ErrorMsgCount = 0, GoodMsgCount = 0;
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	@Autowired
	private JmsTemplate jmsTemplateError;
	
	private @Value("${mailReaderOutput.Queue}") String queueName;
	private @Value("${errorOutput.Queue}") String errorQueueName;

	/**
	 * Constructor
	 */
	public JmsProcessor() {
	}

	/**
	 * write a message bean with default properties
	 * 
	 * @param msgBean -
	 *            a MessageBean object
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public String writeMsg(Object msgBean) throws JMSException {
		return writeMsg(msgBean, (String) null, false);
	}

	/**
	 * write a message bean with specified correlation id
	 * 
	 * @param msgBean -
	 *            a MessageBean object
	 * @param useThisCorrelId -
	 *            Correlation Id for the Message
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public String writeMsg(Object msgBean, String useThisCorrelId) throws JMSException {
		return writeMsg(msgBean, useThisCorrelId, false);
	}

	/**
	 * write a message bean with specified correlation id
	 * 
	 * @param msgBean -
	 *            a MessageBean object
	 * @param useThisCorrelId -
	 *            Correlation Id for the Message
	 * @param toErrorQue -
	 *            weather to write to Error Queue
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public synchronized String writeMsg(final Object msgBean, final String useThisCorrelId, final boolean toErrorQue)
			throws JMSException {
		String rtnMessageId = null;
		try {
			if (isDebugEnabled) {
				logger.debug("Creating a ObjectMessage");
			}
			SendMessageCreator msgCreator = new SendMessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					ObjectMessage outObjMsg = session.createObjectMessage();
					outObjMsg.clearBody();
					outObjMsg.clearProperties();

					if (isDebugEnabled) {
						logger.debug("Adding Object");
					}
					outObjMsg.setObject((java.io.Serializable) msgBean);
					setCorrelationId(outObjMsg, useThisCorrelId);
					setMessage(outObjMsg);
					return outObjMsg;
				}
			};

			rtnMessageId = writeToQueue(msgCreator, toErrorQue, "MessageBean");
		}
		catch (Exception e) {
			logger.error("Exception Caught", e);
			throw new JMSException("Exception caught during writeMsg() " + e.getMessage());
		}

		if (isDebugEnabled) {
			logger.debug("Message sent at " + new Date() + ", JmsMsgId: " + rtnMessageId);
		}
		return rtnMessageId;
	}

	/**
	 * write a String message with default properties
	 * 
	 * @param mStr -
	 *            a String message
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public String writeMsg(String mStr) throws JMSException {
		return writeMsg(mStr, (String) null, false);
	}

	/**
	 * write a String message with specified correlation id
	 * 
	 * @param mStr -
	 *            a String message
	 * @param useThisCorrelId -
	 *            Correlation Id for the Message
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public String writeMsg(String mStr, String useThisCorrelId) throws JMSException {
		return writeMsg(mStr, useThisCorrelId, false);
	}

	/**
	 * write a String message with specified correlation id
	 * 
	 * @param mStr -
	 *            a String message
	 * @param useThisCorrelId -
	 *            Correlation Id for the Message
	 * @param toErrorQue -
	 *            weather to write to Error Queue
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public synchronized String writeMsg(final String mStr, final String useThisCorrelId, final boolean toErrorQue)
			throws JMSException {
		String rtnMessageId = null;
		try {
			if (isDebugEnabled) {
				logger.debug("Creating a TextMessage");
			}
			SendMessageCreator msgCreator = new SendMessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					TextMessage outTextMsg = session.createTextMessage();
					outTextMsg.clearBody();
					outTextMsg.clearProperties();

					if (isDebugEnabled) {
						logger.debug("Adding Text");
					}
					outTextMsg.setText(mStr);
					setCorrelationId(outTextMsg, useThisCorrelId);
					setMessage(outTextMsg);
					return outTextMsg;
				}
			};

			rtnMessageId = writeToQueue(msgCreator, toErrorQue, "String");
		}
		catch (Exception e) {
			logger.error("Exception Caught", e);
			throw new JMSException("Exception caught during writeMsg() " + e.getMessage());
		}

		if (isDebugEnabled) {
			logger.debug("Message sent at " + new Date() + ", JmsMsgId: " + rtnMessageId);
		}
		return rtnMessageId;
	}

	/**
	 * write a JMS message to queue, using PassThroughMessageConverter class.
	 * 
	 * @param msg -
	 *            a JMS message
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public String writeMsg(javax.jms.Message msg) throws JMSException {
		return writeMsg(msg, false);
	}
	
	public synchronized String writeMsg(javax.jms.Message msg, boolean toErrorQue) throws JMSException {
		return writeMsg(msg, null, toErrorQue);
	}
	
	/**
	 * write a JMS message to queue, using PassThroughMessageConverter class.
	 * 
	 * @param msg -
	 *            a JMS message
	 * @param toErrorQue -
	 *            weather to write to Error Queue
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public synchronized String writeMsg(javax.jms.Message msg, final String useThisCorrelId, boolean toErrorQue)
			throws JMSException {
		String rtnMessageId = null;
		MessageConverter converter = getJmsTemplate().getMessageConverter();
		try {
			if (isDebugEnabled) {
				logger.debug("Creating a Message");
			}
			getJmsTemplate().setMessageConverter(new PassThroughMessageConverter());
			if (StringUtils.isBlank(msg.getJMSCorrelationID())) {
				setCorrelationId(msg, useThisCorrelId);
			}
			
			// Ask the QueueSender to send the message we have created
			if (!toErrorQue) {
				if (isDebugEnabled) {
					logger.debug("Sending the message to " + getJmsTemplate().getDefaultDestination());
				}
				getJmsTemplate().convertAndSend(getJmsTemplate().getDefaultDestination(), msg);
			}
			else {
				if (isDebugEnabled) {
					logger.debug("Sending the message to " + getJmsTemplateError().getDefaultDestination());
				}
				getJmsTemplateError().convertAndSend(getJmsTemplateError().getDefaultDestination(), msg);
			}
			
			rtnMessageId = msg.getJMSMessageID();
		}
		catch (Exception e) {
			logger.error("Exception Caught", e);
			throw new JMSException("Exception caught during writeMsg() " + e.getMessage());
		}
		finally {
			getJmsTemplate().setMessageConverter(converter);
		}

		if (isDebugEnabled) {
			logger.debug("Message sent at " + new Date() + ", JmsMsgId: " + rtnMessageId);
		}
		return rtnMessageId;
	}

	/**
	 * write a JMS message to queue, using SendMessageCreator class.
	 * 
	 * @param msg -
	 *            a JMS message
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public String writeJmsMsg(javax.jms.Message msg) throws JMSException {
		return writeJmsMsg(msg, false);
	}
	
	/**
	 * write a JMS message to queue, using SendMessageCreator class.
	 * 
	 * @param msg -
	 *            a JMS message
	 * @param toErrorQue -
	 *            weather to write to Error Queue
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public synchronized String writeJmsMsg(final javax.jms.Message msg, boolean toErrorQue) throws JMSException {
		String rtnMessageId = null;
		MessageConverter converter = getJmsTemplate().getMessageConverter();
		try {
			if (isDebugEnabled) {
				logger.debug("Creating a Message");
			}
			SendMessageCreator msgCreator = new SendMessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					setMessage(msg);
					return msg;
				}
			};
			
			rtnMessageId = writeToQueue(msgCreator, toErrorQue, "JMS");
		}
		catch (Exception e) {
			logger.error("Exception Caught", e);
			throw new JMSException("Exception caught during writeMsg() " + e.getMessage());
		}
		finally {
			getJmsTemplate().setMessageConverter(converter);
		}

		if (isDebugEnabled) {
			logger.debug("Message sent at " + new Date() + ", JmsMsgId: " + rtnMessageId);
		}
		return rtnMessageId;
	}

	/**
	 * write a bytes message with specified correlation id indicator
	 * 
	 * @param blob -
	 *            a byte array
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public String writeMsg(byte[] bytes) throws JMSException {
		return writeBytesMsg(bytes, null, false);
	}
	
	/**
	 * write a bytes message with provided correlation id
	 * 
	 * @param blob -
	 *            a byte array
	 * @param useThisCorrelId -
	 *            Correlation Id for the Message
	 * @param toErrorQue -
	 *            weather to write to Error Queue
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public synchronized String writeBytesMsg(final byte[] bytes, final String useThisCorrelId, final boolean toErrorQue)
			throws JMSException {
		/* StreamMessage was tried and JMS inserts xml tags into the message */
		String rtnMessageId = null;
		try {
			SendMessageCreator msgCreator = new SendMessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					if (isDebugEnabled) {
						logger.debug("Creating a BytesMessage");
					}
					BytesMessage outBytesMsg = session.createBytesMessage();
					outBytesMsg.clearBody();
					outBytesMsg.clearProperties();

					if (isDebugEnabled) {
						logger.debug("Adding message components");
					}
					outBytesMsg.writeBytes(bytes);
					setCorrelationId(outBytesMsg, useThisCorrelId);
					setMessage(outBytesMsg);
					return outBytesMsg;
				}
			};

			rtnMessageId = writeToQueue(msgCreator, toErrorQue, "StructNode");
		}
		catch (Exception e) {
			logger.error("Exception Caught", e);
			throw new JMSException("Exception caught during writeMsg() " + e.getMessage());
		}

		if (isDebugEnabled) {
			logger.debug("Message sent at " + new Date() + ", JmsMsgId: " + rtnMessageId);
		}
		return rtnMessageId;
	}

	/**
	 * write a Stream message with provided correlation id
	 * 
	 * @param blob -
	 *            a byte array
	 * @param useThisCorrelId -
	 *            Correlation Id for the Message
	 * @param toErrorQue -
	 *            weather to write to Error Queue
	 * @return JMS Message Id as a String
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public synchronized String writeStreamMsg(final byte[] bytes, final String useThisCorrelId,
			final boolean toErrorQue) throws JMSException {
		/* StreamMessage was tried and JMS inserts xml tags to the message */
		String rtnMessageId = null;
		try {
			SendMessageCreator msgCreator = new SendMessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					if (isDebugEnabled) {
						logger.debug("Creating a StreamMessage");
					}
					StreamMessage outStreamMsg = session.createStreamMessage();
					outStreamMsg.clearBody();
					outStreamMsg.clearProperties();

					if (isDebugEnabled) {
						logger.debug("Adding message components");
					}
					outStreamMsg.writeBytes(bytes);
					setCorrelationId(outStreamMsg, useThisCorrelId);
					setMessage(outStreamMsg);
					return outStreamMsg;
				}
			};

			rtnMessageId = writeToQueue(msgCreator, toErrorQue, "Stream");
		}
		catch (Exception e) {
			logger.error("Exception Caught", e);
			throw new JMSException("Exception caught during writeMsg() " + e.getMessage());
		}

		if (isDebugEnabled) {
			logger.debug("Message sent at " + new Date() + ", JmsMsgId: " + rtnMessageId);
		}
		return rtnMessageId;
	}

	/**
	 * return a JMS message read from default queue.
	 * 
	 * @return Message
	 * @throws JMSException
	 *             if JMS error occurred
	 */
	public synchronized Message readMsg() throws JMSException {
		Message message = getJmsTemplate().receive(getJmsTemplate().getDefaultDestination());
		return message;
	}

	private String writeToQueue(SendMessageCreator msgCreator, boolean toErrorQue, String msgType) throws JMSException {

		// Ask the QueueSender to send the message we have created
		if (!toErrorQue) {
			if (isDebugEnabled) {
				logger.debug("Sending the " + msgType + " message to " + getJmsTemplate().getDefaultDestination());
			}
			getJmsTemplate().send(getJmsTemplate().getDefaultDestination(), msgCreator);
		}
		else {
			if (isDebugEnabled) {
				logger.debug("Sending the " + msgType + " message to " + getJmsTemplateError().getDefaultDestination());
			}
			getJmsTemplateError().send(getJmsTemplateError().getDefaultDestination(), msgCreator);
		}

		String rtnMessageId = msgCreator.getJMSMessageId();
		return rtnMessageId;
	}
	
	private void setCorrelationId(Message outMsg, String useThisCorrelId) throws JMSException {
		if (StringUtils.isNotBlank(useThisCorrelId)) {
			if (useThisCorrelId.toUpperCase().startsWith("ID:")) {
				try {
					outMsg.setJMSCorrelationIDAsBytes(getCorrelationIDAsBytes(useThisCorrelId));
				}
				catch (NumberFormatException e) {
					logger.error("Failed to set CorrelationId as Bytes, set it as String ...");
					// fall back to String format
					outMsg.setJMSCorrelationID(useThisCorrelId.substring(3));
				}
			}
			else {
				outMsg.setJMSCorrelationID(useThisCorrelId);
			}
		}
	}

	private byte[] getCorrelationIDAsBytes(String msgid) {
		// sample msgid: ID:414d51205551444130312020202020203b0938cb00032013
		byte[] barray = new byte[24];
		for (int i = 0; i < (msgid.length() - 3) / 2; i++) {
			String twoHex = msgid.substring(i * 2 + 3, i * 2 + 5);
			int intValue = Integer.parseInt(twoHex, 16);
			barray[i] = (byte)intValue;
		}
		return barray;
	}

	synchronized boolean errorsUnderLimit() {
		GoodMsgCount = 0;
		// caught a bad message, reset the good message counter
		if (++ErrorMsgCount < ErrorMsgLimit) {
			return true;
		}
		else {
			return false;
		}
	}

	synchronized void receivedGoodMsg() {
		if (++GoodMsgCount % 10 == 0) {
			ErrorMsgCount = 0;
		}
		// 10 consecutive good messages has been returned, reset the error
		// message counter.
	}

	public String getMsgSelector() {
		return msgSelector;
	}

	public void setMsgSelector(String msgSelector) {
		this.msgSelector = msgSelector;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public synchronized JmsTemplate getJmsTemplate() {
		if (jmsTemplate.getDefaultDestination() == null) {
			jmsTemplate.setDefaultDestination(new ActiveMQQueue(getQueueName()));
		}
		return jmsTemplate;
	}
	
	public synchronized JmsTemplate getJmsTemplateError() {
		if (jmsTemplateError.getDefaultDestination() == null) {
			jmsTemplateError.setDefaultDestination(new ActiveMQQueue(errorQueueName));
		}
		return jmsTemplateError;
	}
	
	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

}
