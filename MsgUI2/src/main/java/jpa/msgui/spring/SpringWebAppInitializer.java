package jpa.msgui.spring;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import jpa.spring.util.SpringAppConfig;

public class SpringWebAppInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext container) throws ServletException {
		// Create the 'root' Spring application context
		AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
	    rootContext.register(SpringAppConfig.class);
	    rootContext.register(jpa.spring.util.SpringTaskConfig.class);
	    //rootContext.register(jpa.spring.util.SpringJmsConfig.class);
	    rootContext.refresh();
	    rootContext.registerShutdownHook();
		
	    // Manage the life cycle of the root application context
	    container.addListener(new ContextLoaderListener(rootContext));
	    
	    // line adding an implementation of ContextCleanupListener
        container.addListener(new SpringWebAppCleanupListener());
	}

}
