package jpa.test.msgin;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jpa.data.preload.MailInboxEnum;
import jpa.model.MailInbox;
import jpa.model.MailInboxPK;
import jpa.service.msgin.MailInboxService;
import jpa.spring.util.BoTestBase;

public class MailInboxTest extends BoTestBase {

	@BeforeClass
	public static void MailInboxPrepare() {
	}

	@Autowired
	MailInboxService service;

	@Test
	public void mailInboxService() {
		MailInbox mc1 = null;
		// test insert
		MailInboxEnum mc = MailInboxEnum.jwang;
		MailInboxPK pk1 = new MailInboxPK(mc.getUserId(),mc.getHostName());
		mc1 = service.getByPrimaryKey(pk1);
		if (mc1 == null) {
			mc1 = new MailInbox();
			mc1.setMailInboxPK(pk1);
			mc1.setUserPswd(mc.getUserPswd());
			mc1.setPortNumber(mc.getPort());
			mc1.setProtocol(mc.getProtocol().value());
			mc1.setDescription(mc.getDescription());
			mc1.setStatusId(mc.getStatus().getValue());
			mc1.setIsInternalOnly(mc.getIsInternalOnly());
			mc1.setReadPerPass(mc.getReadPerPass());
			mc1.setUseSsl(mc.isUseSsl());
			mc1.setNumberOfThreads(mc.getNumberOfThreads());
			mc1.setMaximumRetries(mc.getMaximumRetries());
			mc1.setMinimumWait(mc.getMinimumWait());
			mc1.setMessageCount(mc.getMessageCount());
			mc1.setIsToPlainText(mc.getIsToPlainText());
			if (StringUtils.isBlank(mc1.getToAddressDomain())) {
				mc1.setToAddressDomain(mc1.getMailInboxPK().getHostName());
			}
			mc1.setToAddressDomain(mc.getToAddressDomain());
			mc1.setIsCheckDuplicate(mc.getIsCheckDuplicate());
			mc1.setIsAlertDuplicate(mc.getIsAlertDuplicate());
			mc1.setIsLogDuplicate(mc.getIsLogDuplicate());
			mc1.setPurgeDupsAfter(mc.getPurgeDupsAfter());
			service.insert(mc1);
		}
		
		List<MailInbox> list = service.getAll(true);
		assertFalse(list.isEmpty());
		
		MailInbox tkn0 = service.getByPrimaryKey(list.get(0).getMailInboxPK());
		assertNotNull(tkn0);
		
		tkn0 = service.getByRowId(list.get(0).getRowId());
		assertNotNull(tkn0);
		
		// test update
		tkn0.setUpdtUserId("JpaTest");
		service.update(tkn0);
		
		MailInbox tkn1 = service.getByRowId(tkn0.getRowId());
		assertTrue("JpaTest".equals(tkn1.getUpdtUserId()));
		// end of test update
		
		// test insert
		MailInbox tkn2 = new MailInbox();
		try {
			BeanUtils.copyProperties(tkn2, tkn1);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		MailInboxPK pk2 = new MailInboxPK(tkn2.getMailInboxPK().getUserId()+"_v2",tkn2.getMailInboxPK().getHostName());
		tkn2.setMailInboxPK(pk2);
		service.insert(tkn2);
		
		MailInbox tkn3 = service.getByPrimaryKey(tkn2.getMailInboxPK());
		assertTrue(tkn3.getRowId()!=tkn1.getRowId());
		// end of test insert
		
		// test select with No Result
		service.delete(tkn3);
		assertNull(service.getByPrimaryKey(tkn2.getMailInboxPK()));

		
		assertTrue(1==service.deleteByPrimaryKey(tkn1.getMailInboxPK()));
		assertTrue(0==service.deleteByRowId(tkn3.getRowId()));
	}
}
