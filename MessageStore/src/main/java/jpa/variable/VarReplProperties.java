package jpa.variable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.log4j.Logger;

public class VarReplProperties extends Properties {
	static Logger logger = Logger.getLogger(VarReplProperties.class);
	private static final long serialVersionUID = 4115280968301218916L;

	public VarReplProperties() {
		super();
	}
	
	@Override
	public String getProperty(String key) {
		String template = super.getProperty(key);
		if (template == null) return null;
		PropertyRenderer renderer = PropertyRenderer.getInstance();
		try {
			String renderedText = renderer.render(template, this);
			return renderedText;
		}
		catch (Exception e) {
			logger.warn("Exception caught: " + e.getMessage());
			return template;
		}
	}

	@Override
	public String getProperty(String key, String defaultValue) {
		String template = this.getProperty(key);
		if (template == null) return defaultValue;
		return template;
	}

	private static String fileName = "META-INF/msgstore.derby.properties";
	public static void main(String[] args) {
		VarReplProperties props = loadMyProperties(fileName);
		props.list(System.out);
		logger.info("=================================");
		logger.info(props.getProperty("dataSource.url"));
		logger.info(props.getProperty("jndi.url"));
		logger.info(props.getProperty("jdbc.host"));
		logger.info(props.getProperty("not.found", "property not found"));
	}
	
	public static VarReplProperties loadMyProperties(String fileName) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		URL url = loader.getResource(fileName);
		if (url == null) {
			throw new RuntimeException("Could not find " + fileName + " file.");
		}
		VarReplProperties props = new VarReplProperties();
		try {
			InputStream is = url.openStream();
			props.load(is);
		}
		catch (IOException e) {
			throw new RuntimeException("IOException caught", e);
		}
		
		return props;
	}

}
