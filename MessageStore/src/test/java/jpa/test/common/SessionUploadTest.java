package jpa.test.common;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.model.SessionUpload;
import jpa.model.SessionUploadPK;
import jpa.model.UserData;
import jpa.service.common.SessionUploadService;
import jpa.service.common.UserDataService;
import jpa.spring.util.BoTestBase;
import jpa.util.PrintUtil;

public class SessionUploadTest extends BoTestBase {

	@BeforeClass
	public static void SessionUploadPrepare() {
	}

	@Autowired
	SessionUploadService service;
	@Autowired
	UserDataService userService;

	private String testSessionId = "jpa test session id";
	
	@Test
	public void userDataService() {
		List<UserData> lst0 = userService.getAll();
		assertFalse(lst0.isEmpty());
		UserData usr1 = lst0.get(0);
		
		List<SessionUpload> lst1 = service.getByUserId(usr1.getUserId());
		assertNotNull(lst1);
		
		// test insert
		SessionUploadPK pk1 = new SessionUploadPK(testSessionId,0);
		SessionUpload tkn1 = new SessionUpload();
		tkn1.setSessionUploadPK(pk1);
		tkn1.setFileName("jpatest1.txt");
		tkn1.setUserData(usr1);
		tkn1.setSessionValue("jpa test 1 content".getBytes());
		service.insert(tkn1);
		
		SessionUpload tkn2 = service.getByPrimaryKey(pk1);
		assertNotNull(tkn2);
		logger.info(PrintUtil.prettyPrint(tkn2,2));
		
		assertFalse(service.getBySessionId(pk1.getSessionId()).isEmpty());
		assertFalse(service.getByUserId(usr1.getUserId()).isEmpty());
		
		// test insert 2
		SessionUploadPK pk2 = new SessionUploadPK(testSessionId,0);
		SessionUpload tkn3 = new SessionUpload();
		tkn3.setSessionUploadPK(pk2);
		tkn3.setFileName("jpatest2.txt");
		tkn3.setUserData(usr1);
		tkn3.setSessionValue("jpa test 2 content".getBytes());
		assertTrue(1==service.getBySessionId(testSessionId).size());
		service.insertLast(tkn3);
		assertNotNull(service.getByPrimaryKey(new SessionUploadPK(testSessionId,1)));
		assertTrue(2==service.getBySessionId(testSessionId).size());
		
		SessionUpload tkn4 = service.getByPrimaryKey(pk2);
		assertNotNull(tkn4);
		assertFalse(tkn2.getFileName().equals(tkn4.getFileName()));

		assertTrue(0<=service.deleteExpired(1));

		// test update
		tkn2.setUpdtUserId("JpaTest");
		service.update(tkn2);
		
		Optional<SessionUpload> tkn5 = service.getByRowId(tkn2.getRowId());
		assertTrue(tkn5.isPresent());
		assertTrue("JpaTest".equals(tkn5.get().getUpdtUserId()));
		// end of test update
		
		// test select with No Result
		service.delete(tkn5.get());
		assertTrue(service.getByRowId(tkn5.get().getRowId()).isEmpty());
		
		assertTrue(0==service.deleteByPrimaryKey(tkn5.get().getSessionUploadPK()));
		assertTrue(0==service.deleteByRowId(tkn5.get().getRowId()));
		
		assertTrue(0<service.deleteAll());

	}
}
