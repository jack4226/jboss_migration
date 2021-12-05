package jpa.msgui.spring;

import javax.servlet.ServletContextEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.JmsException;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.web.context.ContextCleanupListener;
import org.springframework.web.context.WebApplicationContext;

import jpa.msgui.util.SpringUtil;

public class SpringWebAppCleanupListener extends ContextCleanupListener {
	static final Logger logger = LogManager.getLogger(SpringWebAppCleanupListener.class);

	@Override
    public void contextDestroyed(ServletContextEvent event) {
		logger.warn("Entring Spring contextDestroyed() method...");
        // put your shutdown code in here
		WebApplicationContext context = SpringUtil.getWebAppContext(event.getServletContext());
		
		DefaultMessageListenerContainer ruleEngineListener = (DefaultMessageListenerContainer) context.getBean("ruleEngineJmsListener");
		DefaultMessageListenerContainer mailSenderListener = (DefaultMessageListenerContainer) context.getBean("mailSenderJmsListener");
		
		try {
			if (ruleEngineListener.isRunning()) {
				logger.warn("Stop and Shutdown ruleEngineJmsListener...");
				ruleEngineListener.stop();
				ruleEngineListener.shutdown();
				ruleEngineListener.destroy();
			}
			
			if (mailSenderListener.isRunning()) {
				logger.warn("Stop and Shutdown mailSenderJmsListener...");
				mailSenderListener.stop();
				mailSenderListener.shutdown();
				mailSenderListener.destroy();
			}
		}
		catch (JmsException e) {
			logger.error("JmsException caught", e);
		}
		
		BeanDefinitionRegistry factory = (BeanDefinitionRegistry) context.getAutowireCapableBeanFactory();
		try { // did not work
			if (factory.containsBeanDefinition("mailReaderTaskExr")) {
				logger.warn("Removing mailReaderTaskExr from Spring context...");
				factory.removeBeanDefinition("mailReaderTaskExr");
			}
		}
		catch (NoSuchBeanDefinitionException e) {}
		
		((ConfigurableApplicationContext) context).stop();
		((ConfigurableApplicationContext) context).close();
		
		// Shutdown Spring Application contexts in MessageCore
		jpa.spring.util.SpringUtil.shutDownConfigContexts();
    }
}
