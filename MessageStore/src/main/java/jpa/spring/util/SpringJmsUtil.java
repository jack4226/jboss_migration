package jpa.spring.util;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class SpringJmsUtil {
	protected final static Logger logger = Logger.getLogger(SpringJmsUtil.class);
	
	private static AnnotationConfigApplicationContext ctx = null;
	
	public static AbstractApplicationContext getApplicationContext() {
		if (ctx == null) {
			ctx = new AnnotationConfigApplicationContext();
			ctx.register(SpringAppConfig.class, SpringJmsConfig.class);
			ctx.refresh();
		}
		return ctx;
	}
	
	public static AbstractMessageListenerContainer getJmsListenerContainer() {
		DefaultMessageListenerContainer container = (DefaultMessageListenerContainer) getApplicationContext().getBean("jmsListenerContainer");
		return container;
	}
}