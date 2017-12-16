package com.es.jaxrs.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.openejb.OpenEjbContainer;
import org.apache.openejb.config.EjbModule;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.SingletonBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.junit.ApplicationComposer;
import org.apache.openejb.loader.IO;
import org.apache.openejb.testing.Configuration;
import org.apache.openejb.testing.EnableServices;
import org.apache.openejb.testing.Module;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.es.ejb.mailinglist.MailingListRS;
import com.es.jaxrs.common.IllegalArgumentExceptionMapper;

@EnableServices(value = "jaxrs")
@RunWith(ApplicationComposer.class)
public class CustomProviderTest {
	static final Logger logger = Logger.getLogger(CustomProviderTest.class);
	
	@Configuration
    public Properties configuration() {
        return new Properties() {
			private static final long serialVersionUID = -5008776205102566768L;
		{
            setProperty(OpenEjbContainer.OPENEJB_EMBEDDED_REMOTABLE, Boolean.TRUE.toString());
        }};
    }
	
	@Module
	public EjbModule app() {
		final SingletonBean bean = (SingletonBean) new SingletonBean(MailingListRS.class).localBean();
		bean.setRestService(true);
		
		// now create an ejbjar and an openejb-jar to hold the provider config
		 
		final EjbJar ejbJar = new EjbJar();
        ejbJar.addEnterpriseBean(bean);

        final OpenejbJar openejbJar = new OpenejbJar();
        openejbJar.addEjbDeployment(new EjbDeployment(ejbJar.getEnterpriseBeans()[0]));

        final Properties properties = openejbJar.getEjbDeployment().iterator().next().getProperties();
        properties.setProperty("cxf.jaxrs.providers", IllegalArgumentExceptionMapper.class.getName());

        // link all and return this module

        final EjbModule module = new EjbModule(ejbJar);
        module.setOpenejbJar(openejbJar);

        return module;
	}

	@Test
	//@org.junit.Ignore
    public void checkProviderIsUsed() throws IOException {
        final String message = IO.slurp(new java.net.URL("http://localhost:4204/CustomProviderTest/msgapi/mailinglist/greeting"));
        assertEquals("this exception is handled by an exception mapper", message);
    }

}
