package jpa.test.spring;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

import jpa.spring.util.SpringJmsUtil;

public class SpringJmsUtilTest {
	protected final static Logger logger = LogManager.getLogger(SpringJmsUtilTest.class);
	
	@Test
	public void testSpringJmsUtil() {
		AbstractApplicationContext ctx =SpringJmsUtil.getApplicationContext();
		assertNotNull(ctx);
		
		AbstractMessageListenerContainer listener = SpringJmsUtil.getJmsListenerContainer();
		assertNotNull(listener);
	}

}
