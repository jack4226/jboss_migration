package com.es.ejb.test;

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;

import com.es.ejb.idtokens.IdTokensLocal;

import jpa.constant.Constants;
import jpa.util.PrintUtil;
import junit.framework.TestCase;

public class IdTokensTest extends TestCase {
	static final Logger logger = LogManager.getLogger(IdTokensTest.class);

	public void testIdTokens() {

		Properties properties = new Properties();
		properties.setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, "true");
		EJBContainer ejbContainer = EJBContainer.createEJBContainer(properties);
		assertNotNull(ejbContainer);
		final Context context = ejbContainer.getContext();

		try {
			Object obj = context.lookup("java:global/MsgRest/IdTokens!com.es.ejb.idtokens.IdTokensLocal");

			assert (obj instanceof IdTokensLocal);

			IdTokensLocal idTokens = (IdTokensLocal) obj;

			final CountDownLatch ready = new CountDownLatch(1);
			final Future<?> cd = idTokens.stayBusy(ready);

			while (cd.get() == null) {
				TimeUnit.MILLISECONDS.sleep(10);
				ready.countDown();
			}
			logger.info("Time lapsed: " + cd.get());

			try {
				assertNotNull(idTokens.findBySenderId(Constants.DEFAULT_SENDER_ID));
			} catch (Exception e) {
				logger.error("Exception caught: " + e.getMessage());
				fail("Failed to find Sender by Id: " + Constants.DEFAULT_SENDER_ID);
			}

			try {
				assertNull(idTokens.findBySenderId(""));
			} catch (Exception e) {
				logger.error("Exception caught: " + e.getMessage());
				fail();
			}

			List<jpa.model.IdTokens> list = idTokens.findAll();
			assertTrue(!list.isEmpty());
			logger.info(PrintUtil.prettyPrint(list.get(0)));

			jpa.model.IdTokens idvo = list.get(list.size() - 1);
			Timestamp ts = new Timestamp(System.currentTimeMillis());
			idvo.setUpdtTime(ts);
			String desc = idvo.getDescription();
			if (StringUtils.lastIndexOf(desc, "-") > 0) {
				desc = desc.substring(0, StringUtils.lastIndexOf(desc, "-"));
			} else {
				desc += "-V2";
			}
			idvo.setDescription(desc);
			idTokens.update(idvo);
			idvo = idTokens.findBySenderId(idvo.getSenderData().getSenderId());
			// assert(ts.equals(idvo.getUpdtTime())); // TODO not working with Hibernate under Maven
			assert (desc.equals(idvo.getDescription()));
		} catch (NamingException | InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
			fail();
		} finally {
			if (ejbContainer != null) {
				ejbContainer.close();
			}
		}
	}
}
