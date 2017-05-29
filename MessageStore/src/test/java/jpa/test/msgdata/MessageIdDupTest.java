package jpa.test.msgdata;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.model.msg.MessageIdDuplicate;
import jpa.service.msgdata.MessageIdDupService;
import jpa.spring.util.BoTestBase;

public class MessageIdDupTest extends BoTestBase {

	@Autowired
	private MessageIdDupService service;
	
	static Random random = new Random();
	
	static String msgId;
	
	@Before
	public void prepare() {
		MessageIdDuplicate md = new MessageIdDuplicate();
		md.setMessageId("MsgDupTest-" + random.nextInt(10000));
		md.setAddTime(new java.sql.Timestamp(new java.util.Date().getTime()));
		service.insert(md);
		assertNotNull(md.getMessageId());
		msgId = md.getMessageId();
	}
	
	@Ignore
	public void testInsertMessagdId() {
		MessageIdDuplicate md = new MessageIdDuplicate();
		md.setMessageId("MsgDupTest-" + random.nextInt(10000));
		md.setAddTime(new java.sql.Timestamp(new java.util.Date().getTime()));
		service.insert(md);
		assertNotNull(md.getMessageId());
	}

	@Test
	public void testFindByMessageId() {
		MessageIdDuplicate md = service.getByMessageId(msgId);
		assertNotNull(md);
	}

	@Test
	public void testDelete() {
		MessageIdDuplicate md = service.getByMessageId(msgId);
		assertNotNull(md);
		service.delete(md);
		assertNull(service.getByMessageId(msgId));
	}
	
	@Test
	public void testMessageIdNotFound() {
		MessageIdDuplicate md = service.getByMessageId("Invalid-Message-Id");
		assertNull(md);
	}

	@Test
	public void testPurgeMessage() {
		// test purging duplicate messages
		assertFalse(service.isMessageIdDuplicate("jpatest-smtp-message-id"));
		assertTrue(service.isMessageIdDuplicate("jpatest-smtp-message-id"));
		assertTrue(0<=service.purgeMessageIdDuplicate(1));
	}
}
