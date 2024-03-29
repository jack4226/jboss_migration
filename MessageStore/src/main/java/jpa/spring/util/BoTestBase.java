package jpa.spring.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={jpa.spring.util.SpringAppConfig.class})
//@ContextConfiguration(locations={"classpath:/spring-jpa-config.xml"})
@Transactional(transactionManager="msgTransactionManager", propagation=Propagation.REQUIRED, isolation=Isolation.READ_COMMITTED)
@org.springframework.test.annotation.Rollback
public class BoTestBase {
	protected static final Logger logger = LogManager.getLogger(BoTestBase.class);
	protected final static boolean isDebugEnabled = logger.isDebugEnabled();
	protected final static String LF = System.getProperty("line.separator","\n");

	protected static long WaitTimeInMillis = 2 * 1000L;
	
	protected static boolean enableJunitRunClasses = false;
	
	static {
		System.setProperty("hibernate.generate_statistics", "true");
		//Log4jConfigUtil.modifyLogLevel(Level.ERROR, Level.INFO, true);
	}
	
	@Before
	public void prepare() {
	}

	@Test
	public void boBaseDummyTest() {
	}
}
