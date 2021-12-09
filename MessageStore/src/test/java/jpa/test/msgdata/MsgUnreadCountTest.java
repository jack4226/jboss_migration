package jpa.test.msgdata;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import jpa.model.msg.MsgUnreadCount;
import jpa.service.msgdata.MsgUnreadCountService;
import jpa.spring.util.BoTestBase;

public class MsgUnreadCountTest extends BoTestBase {

	@Autowired
	MsgUnreadCountService service;
	
	@Before
	@Rollback(value=false)
	public void prepare() {
		MsgUnreadCount muc1 = new MsgUnreadCount();
		service.upsert(muc1);
	}
	
	@Test
	@Rollback(value=false)
	public void testMsgUnreadCount1() {
		List<MsgUnreadCount> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		MsgUnreadCount muc1 = list1.get(list1.size() - 1);
		Optional<MsgUnreadCount> muc2 = service.getByRowId(muc1.getRowId());
		assertTrue(muc2.isPresent());
		
		assertEquals(muc1, muc2.get());
		
		assertEquals(1, service.deleteByRowId(muc1.getRowId()));
		
		assertFalse(service.getByRowId(muc1.getRowId()).isPresent());
	}
	
	@Test
	@Rollback(value=false)
	public void testMsgUnreadCount2() {
		MsgUnreadCount count1 = service.getCount();
		int inboxBefore = count1.getInboxUnreadCount();
		int sentBefore = count1.getSentUnreadCount();
		service.increaseInboxCount();
		MsgUnreadCount count2 = service.getCount();
		assertEquals(inboxBefore + 1, count2.getInboxUnreadCount());
		service.increaseSentCount(3);
		MsgUnreadCount count3 = service.getCount();
		assertEquals(sentBefore + 3, count3.getSentUnreadCount());
	}

}
