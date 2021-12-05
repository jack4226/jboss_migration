package com.es.ejb.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.NamingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.es.ejb.emailaddr.EmailAddrRemote;
import com.es.ejb.mailinglist.MailingListLocal;

import jpa.constant.StatusId;
import jpa.exception.TemplateNotFoundException;
import jpa.util.ExceptionUtil;

public class MailingListTest {
	protected final static Logger logger = LogManager.getLogger(MailingListTest.class);
	
	private static EJBContainer ejbContainer;
	
	private static MailingListLocal mlist;
	private static EmailAddrRemote addrRmt;
	 
	@BeforeClass
	public static void startTheContainer() throws NamingException {
		Properties properties = new Properties();
        properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
		ejbContainer = EJBContainer.createEJBContainer(properties);
		
		lookupABean();
	}

	private static void lookupABean() throws NamingException {
		Object object = ejbContainer.getContext().lookup("java:global/MsgRest/MailingList");
		assertNotNull(object);
		assert(object instanceof MailingListLocal);
		mlist = (MailingListLocal) object;
		
		addrRmt = (EmailAddrRemote) ejbContainer.getContext().lookup(
				"java:global/MsgRest/EmailAddr!com.es.ejb.emailaddr.EmailAddrRemote");		
		assertNotNull(addrRmt);
	}

	@AfterClass
	public static void stopTheContainer() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}
	
	@Test
	public void testBroadcast() {
		try {
			int msgsSent = mlist.broadcast("SampleNewsletter2");
			assert(msgsSent > 0);
		}
//		catch (EJBException e) {
//			Exception exp = ExceptionUtil.findException(e, TemplateNotFoundException.class);
//			if (exp != null) {
//				logger.warn("Could not find TemplateId: " + exp.getMessage());
//			}
//			else {
//				throw e;
//			}
//		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail("Failed to broadcast");
		}
	}

	@Test
	public void testSendmail() {
		try {
			String toAddr = "testto@localhost";
			// Activate the email address in case it's suspended
			addrRmt.updateStatus(toAddr, StatusId.ACTIVE);
			Map<String, String> vars = new HashMap<String, String>();
			vars.put("CustomerName", "List Subscriber");
			String templateId = "SampleNewsletter3";
			int mailsSent = mlist.sendMail(toAddr, vars, templateId);
			logger.info("Number of emails sent: " + mailsSent);
			assert(mailsSent > 0);
		}
		catch (EJBException e) {
			Exception exp = ExceptionUtil.findException(e, TemplateNotFoundException.class);
			if (exp != null) {
				logger.warn("Could not find TemplateId: " + exp.getMessage());
			}
			else {
				logger.error("EJBException caught: " + e.getMessage());
				fail();
			}
		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail("Failed to sendmail");
		}
	}

}
