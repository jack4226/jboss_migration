package com.es.ejb.test;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;

import org.apache.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;

import com.es.ejb.mailreader.MailReader;

import junit.framework.TestCase;

public class MailReaderTest extends TestCase {
	static final Logger logger = Logger.getLogger(MailReaderTest.class);

	 public void testMailReader() throws Exception {
		 
		 Properties properties = new Properties();
	     properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
	     EJBContainer ejbContainer = EJBContainer.createEJBContainer(properties);
	     assertNotNull(ejbContainer);
		 final Context context = ejbContainer.getContext();
		 
		 try {
			 Object obj = context.lookup("java:global/MsgRest/MailReader");
			 
			 assert(obj instanceof MailReader);
			 
			 MailReader reader = (MailReader) obj;
			 logger.info("MailReader polling interval: " + reader.getInterval());
			 // TODO start MailReader
			 
			 Thread.sleep(TimeUnit.SECONDS.toMillis(10));
		 }
		 finally {
			 if (ejbContainer != null) {
				 ejbContainer.close();
			 }
		 }
	 }
}
