package com.es.tomee.spring;

import javax.servlet.ServletContextEvent;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.ContextCleanupListener;
import org.springframework.web.context.WebApplicationContext;

public class SpringWebAppCleanupListener extends ContextCleanupListener {
	static final Logger logger = Logger.getLogger(SpringWebAppCleanupListener.class);

	@Override
    public void contextDestroyed(ServletContextEvent event) {
		logger.warn("Entring Spring contextDestroyed() method...");
        // put your shutdown code in here
		WebApplicationContext context = SpringUtil.getWebAppContext(event.getServletContext());
		
		((ConfigurableApplicationContext) context).stop();
		((ConfigurableApplicationContext) context).close();
		
		// Shutdown Spring Application contexts in MessageCore
		jpa.spring.util.SpringUtil.shutDownConfigContexts();
    }
}
