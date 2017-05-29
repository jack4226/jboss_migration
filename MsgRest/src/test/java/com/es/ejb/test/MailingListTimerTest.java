package com.es.ejb.test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;

import com.es.ejb.mailinglist.MailingListTimer;

public class MailingListTimerTest extends TestCase {
	static final Logger logger = Logger.getLogger(MailingListTimerTest.class);

	 public void testMailingListTimer() throws Exception {
		 
		 Properties properties = new Properties();
	     properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
	     EJBContainer ejbContainer = EJBContainer.createEJBContainer(properties);
	     assertNotNull(ejbContainer);
		 final Context context = ejbContainer.getContext();
		 
		 try {
			 Object obj = context.lookup("java:global/MsgRest/MailingListTimer");
			 
			 assert(obj instanceof MailingListTimer);
			 
			 MailingListTimer reader = (MailingListTimer) obj;
			 logger.info("MailingListTimer current hour: " + reader.currentHour());
			 
			 reader.scheduleSingleTask("SampleNewsletter1", 2);
			 
			 Thread.sleep(TimeUnit.SECONDS.toMillis(10));
		 }
		 finally {
			 if (ejbContainer != null) {
				 ejbContainer.close();
			 }
		 }
	 }

}
