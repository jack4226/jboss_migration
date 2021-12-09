package jpa.test.msgdata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Random;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.data.preload.FolderEnum;
import jpa.model.msg.MessageFolder;
import jpa.service.msgdata.MessageFolderService;
import jpa.service.msgdata.MessageInboxService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class MessageFolderTest extends BoTestBase {

	@BeforeClass
	public static void MessageFolderPrepare() {
	}

	@Autowired
	MessageFolderService service;
	
	@Autowired
	MessageInboxService inboxService;

	@Before
	public void prepare() {
	}
	
	private MessageFolder adr1;
	private MessageFolder adr2;

	@Test
	public void messageFolderService() {
		Random r = new Random();
		String name1 = "Test-1-" + r.nextInt(10000);
		String name2 = "Test-2-" + r.nextInt(10000);
		// test insert
		adr1 = new MessageFolder();
		adr1.setFolderName(name1);;
		adr1.setDescription("Test folder 1");
		service.insert(adr1);
		
		adr2 = new MessageFolder();
		adr2.setFolderName(name2);;
		adr2.setDescription("Test folder 2");
		service.insert(adr2);
		
		Optional<MessageFolder> adr11 = service.getByRowId(adr1.getRowId());
		assertTrue(adr11.isPresent());
		
		logger.info(PrintUtil.prettyPrint(adr11.get(),2));
		
		MessageFolder adr12 = service.getOneByFolderName(name1);
		assertNotNull(adr12);

		assertEquals(adr11.get().getFolderName(), adr12.getFolderName());
		assertEquals(adr11.get().getDescription(), adr12.getDescription());
		
		// test update
		adr2.setUpdtUserId("jpa test");
		service.update(adr2);
		Optional<MessageFolder> adr22 = service.getByRowId(adr2.getRowId());
		assertTrue(adr22.isPresent());
		assertTrue("jpa test".equals(adr22.get().getUpdtUserId()));
		
		logger.info(PrintUtil.prettyPrint(adr22,2));
		
		// test delete
		service.delete(adr11.get());
		assertFalse(service.getByRowId(adr11.get().getRowId()).isPresent());
		
		assertTrue(1==service.deleteByRowId(adr2.getRowId()));
		
	}
	
	@Test
	public void testCountByFolderName() {
		int msgCount = inboxService.getMessageCountByFolderName("bad folder name");
		assertEquals(0, msgCount);
		msgCount = inboxService.getMessageCountByFolderName(FolderEnum.Inbox.name());
		assertTrue(msgCount > 0);
		
		int unreadCount = inboxService.getUnreadCountByFolderName("bad folder name");
		assertEquals(0, unreadCount);
		unreadCount = inboxService.getUnreadCountByFolderName(FolderEnum.Inbox.name());
		assertTrue(unreadCount >= 0);
		
		int readCount  = inboxService.getReadCountByFolderName("bad folder name");
		assertEquals(0, readCount);
		readCount = inboxService.getReadCountByFolderName(FolderEnum.Inbox.name());
		assertTrue(readCount >= 0);

		assertEquals(readCount + unreadCount, msgCount);
		
		msgCount = inboxService.getMessageCountByFolderName(FolderEnum.Sent.name());
		assertTrue(msgCount >= 0);
		
		unreadCount = inboxService.getUnreadCountByFolderName(FolderEnum.Sent.name());
		assertTrue(unreadCount >= 0);
		
		readCount = inboxService.getReadCountByFolderName(FolderEnum.Sent.name());
		assertTrue(readCount >= 0);
		
		assertEquals(readCount + unreadCount, msgCount);
	}
}
