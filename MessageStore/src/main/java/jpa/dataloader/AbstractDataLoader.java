package jpa.dataloader;

import jpa.spring.util.SpringUtil;
import jpa.util.EnvUtil;
import jpa.util.FileUtil;
import jpa.variable.VarReplProperties;

public abstract class AbstractDataLoader {
	public static final String LF = System.getProperty("line.separator", "\n");

	protected VarReplProperties props;
	
	public abstract void loadData();

	protected void startTransaction() {
		SpringUtil.beginTransaction();
	}
	
	protected void commitTransaction() {
		SpringUtil.commitTransaction();
	}
	
	protected String getProperty(String name) {
		if (props == null) {
			String propsFile = "META-INF/dataloader." + EnvUtil.getEnv() + ".properties";
			props = VarReplProperties.loadMyProperties(propsFile);
		}
		return props.getProperty(name);
	}


	protected byte[] loadFromSamples(String fileName) {
		return FileUtil.loadFromFile("samples/", fileName);
	}
}
