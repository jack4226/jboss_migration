package jpa.test.msgdata;

import static org.junit.Assert.*;

import java.util.List;

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
		List<MsgUnreadCount> list1 = service.getAll();
		if (list1.isEmpty()) {
			MsgUnreadCount muc1 = new MsgUnreadCount();
			service.insert(muc1);
		}
	}
	
	@Test
	@Rollback(value=false)
	public void testMsgUnreadCount() {
		List<MsgUnreadCount> list1 = service.getAll();
		assertFalse(list1.isEmpty());
		
		MsgUnreadCount muc1 = list1.get(list1.size() - 1);
		MsgUnreadCount muc2 = service.getByRowId(muc1.getRowId());
		
		assertEquals(muc1, muc2);
		
		assertEquals(1, service.deleteByRowId(muc1.getRowId()));
		
		assertNull(service.getByRowId(muc1.getRowId()));
	}
}
