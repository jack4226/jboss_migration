package jpa.service.msgdata;

import java.sql.Timestamp;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import jpa.model.msg.MessageIdDuplicate;
import jpa.repository.msg.MessageIdDupRepository;

@Component("messageIdDupService")
@Transactional(propagation=Propagation.REQUIRED)
public class MessageIdDupService implements java.io.Serializable {
	private static final long serialVersionUID = -1672902981120943016L;
	static final Logger logger = Logger.getLogger(MessageIdDupService.class);

	@Autowired
	private MessageIdDupRepository repository;
	
	public MessageIdDuplicate getByMessageId(String messageId) {
		return repository.findOneByMessageId(messageId);
	}
	
	public void insert(MessageIdDuplicate msgIdDup) {
		repository.saveAndFlush(msgIdDup);
	}
	
	public void delete(MessageIdDuplicate msgIdDup) {
		repository.delete(msgIdDup);
	}
	
	/**
	 * check if the message received is a duplicate.
	 * @param smtpMessageId to check.
	 * @return true if the smtpMessageId exists.
	 */
	public synchronized boolean isMessageIdDuplicate(String messageId) {
		if (repository.findOneByMessageId(messageId) != null) {
			return true;
		}
		else {
			MessageIdDuplicate md = new MessageIdDuplicate();
			md.setMessageId(messageId);
			md.setAddTime(new java.sql.Timestamp(System.currentTimeMillis()));
			repository.saveAndFlush(md);
			return false;
		}
	}
	
	public synchronized int purgeMessageIdDuplicate(int hours) {
		logger.info("purge records older than " + hours + " hours...");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -hours);
		Timestamp go_back=new Timestamp(calendar.getTimeInMillis());

		return repository.deleteByAddTimeBefore(go_back);
	}

}
