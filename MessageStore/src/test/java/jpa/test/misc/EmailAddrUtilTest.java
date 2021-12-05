package jpa.test.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import jpa.util.EmailAddrUtil;

public class EmailAddrUtilTest {
	static final Logger logger = LogManager.getLogger(EmailAddrUtilTest.class);
	
	@Test
	public void testEmailAddrUtil() {
		String addr1 = "\"ORCPT jwang@nc.rr.com\" <jwang@nc.rr.com>";
		String addr1After = EmailAddrUtil.removeDisplayName(addr1);
		logger.info(addr1+" --> "+addr1After);
		assertEquals("jwang@nc.rr.com", addr1After);
		String addr2 = "DirectStarTV <fqusoogd.undlwfeteot@chaffingphotosensitive.com>";
		String addr2After = EmailAddrUtil.removeDisplayName(addr2);
		logger.info(addr2+" --> "+addr2After);
		assertEquals("fqusoogd.undlwfeteot@chaffingphotosensitive.com", addr2After);
		
		boolean isRemote1 = EmailAddrUtil.isRemoteEmailAddress("A!#$%&'*+/=?.^_`{|}~-BC@localhost.us");
		assertTrue(isRemote1);
		
		boolean isRemoteOrLocal =  EmailAddrUtil.isRemoteOrLocalEmailAddress("A!#$%&'*+/=?.^_`{|}~-BC");
		assertTrue(isRemoteOrLocal);
		
		String verpaddr1 = "bounce-10.07410251.0-jsmith=test.com@localhost";
		assertTrue(EmailAddrUtil.isVERPAddress(verpaddr1));
		String verporig1 = EmailAddrUtil.getOrigAddrFromVERP(verpaddr1);
		assertEquals("jsmith@test.com", verporig1);
		String verpdest1 = EmailAddrUtil.getDestAddrFromVERP(verpaddr1);
		assertEquals("bounce@localhost", verpdest1);
		
		String verpaddr2 = "remove-testlist-jsmith=test.com@localhost";
		String verporig2 = EmailAddrUtil.getOrigAddrFromVERP(verpaddr2);
		assertEquals("jsmith@test.com", verporig2);
		String verpdest2 = EmailAddrUtil.getDestAddrFromVERP(verpaddr2);
		assertEquals("remove@localhost", verpdest2);
		
		int compare1 = EmailAddrUtil.compareEmailAddrs("test@test.com", "test1<test1@test.com>");
		assertEquals(1, compare1);
		
		int compare2 = EmailAddrUtil.compareEmailAddrs("test@test.com", "test <test@test.com>");
		assertEquals(0, compare2);
		
		int compare3 = EmailAddrUtil.compareEmailAddrs("test@test.com;test3@test.com", "test1@test.com,test2@test.com");
		assertEquals(2, compare3);

		int compare4 = EmailAddrUtil.compareEmailAddrs("test1@test.com;test3@test.com", "test2@test.com,test3@test.com");
		assertEquals(0, compare4);
	}
	
	@Test
	public void testRemoveMethods() {
		String msg = "\r\n\t\tDear Smith:\r\n Thank you for contacting us.\t We have received your application.\t\t\r\n";
		
		String msgAfter = EmailAddrUtil.removeCRLFTabs(msg);
		logger.info("Remove CRLFTab: " + msgAfter);
		assertEquals("Dear Smith: Thank you for contacting us. We have received your application.", msgAfter);
		
		String addr1 = "ABC Corp <ABC.rep1@abc.corp.com>";
		assertTrue(EmailAddrUtil.hasDisplayName(addr1));
		assertFalse(EmailAddrUtil.hasDisplayName(EmailAddrUtil.removeDisplayName(addr1)));
		assertEquals("ABC Corp", EmailAddrUtil.getDisplayName(addr1));
		assertEquals("abc.rep1@abc.corp.com", EmailAddrUtil.removeDisplayName(addr1));
		assertEquals("ABC.rep1@abc.corp.com", EmailAddrUtil.removeDisplayName(addr1, false));
		
		String addr2 = "ABC.corp.rep2@abc.com <ABC.rep2@ABC.corp.com>";
		assertEquals("ABC.corp.com", EmailAddrUtil.getEmailDomainName(addr2));
		assertEquals("ABC.rep2", EmailAddrUtil.getEmailUserName(addr2));
		assertTrue(EmailAddrUtil.isInternetEmailAddress(addr2));
		assertTrue(EmailAddrUtil.isRemoteEmailAddress(addr2));
		assertTrue(EmailAddrUtil.hasValidEmailLocalPart(addr2));
		
		try {
			InternetAddress[] addrs = InternetAddress.parse(addr1+","+addr2);
			String addrsAfter1 = EmailAddrUtil.addressToString(addrs);
			logger.info("Convert to String: " + addrsAfter1);
			assertEquals("ABC.rep1@abc.corp.com,ABC.rep2@ABC.corp.com", addrsAfter1);
			String addrsAfter2 = EmailAddrUtil.addressToString(addrs, false);
			logger.info("Convert to String: " + addrsAfter2);
			assertEquals("ABC Corp <ABC.rep1@abc.corp.com>,\"ABC.corp.rep2@abc.com\" <ABC.rep2@ABC.corp.com>", addrsAfter2);
		} 
		catch (AddressException e) {
			fail();
		}
	}
}
