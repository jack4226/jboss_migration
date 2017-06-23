package jpa.test.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {
	static final Logger logger = Logger.getLogger(TestRunner.class);

	static final String TestPackageName = "jpa.test.common";
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(getAllJpaTestClasses(TestPackageName));
		if (!result.getFailures().isEmpty()) {
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.getDescription());
				logger.error("############### Failure: " + failure.getDescription());
				if (failure.getMessage()!=null) {
					logger.error("Failure Message: " + failure.getMessage());
				}
				logger.error(failure.getTrace());
			}
			System.err.println("!!!!! JPA test stopped with error, number of errors (" + result.getFailures().size()  + ") !!!!!");
		}
		else {
			logger.info("########## JPA test completed ##########");
		}
	}

	final static String PS = File.separator;
	static Class<?>[] getAllJpaTestClasses(String pkgName) {
		// looking for class name ending with "Test", for example SubscriberTest.class
		List<Class<?>> clsList = new ArrayList<Class<?>>();
		String homeDir = System.getProperty("user.dir") + PS + "bin" + PS;
		logger.info("Working directory: " + homeDir);
		List<File> files =  null;
		try {
			files = getClassesFromDirTree(new File(homeDir), "Test.class");
		}
		catch (Exception e) {
			logger.warn("Failed to load classes from (" + homeDir + ")!");
			homeDir = System.getProperty("user.dir") + PS + "target" + PS + "test-classes" + PS;
			logger.warn("Trying loading from (" + homeDir + ")...");
			files = getClassesFromDirTree(new File(homeDir), "Test.class");
		}
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		for (File file : files) {
			String path = file.getPath();
			String pkgPath = StringUtils.removeStart(path, homeDir);
			String clsName = pkgPath.replace("\\", ".");
			try {
				clsName = StringUtils.removeEnd(clsName,".class");
				Class<?> testCls = loader.loadClass(clsName);
				if (clsName.startsWith(pkgName) && testCls.getDeclaredAnnotations().length>=4) {
					if (clsName.contains("bo.test")) {
						continue;
					}
					clsList.add(testCls);
					logger.info("Test Class: " + testCls.getName());
				}
			}
			catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException caught: Class not found: " + clsName);
			}
		}
		return (Class<?>[])clsList.toArray(new Class<?>[clsList.size()]);
	}

	/**
	 * Recursively walk a directory tree and return a List of all Files found;
	 * 
	 * @param dir
	 *            - a valid directory
	 */
	static List<File> getClassesFromDirTree(File dir, String endsWith) {
		List<File> result = new ArrayList<File>();
		File[] files = dir.listFiles();
		for (int i=0; i<files.length; i++) {
			File file = files[i];
			if (file.isFile()) {
				if (file.getName().endsWith(endsWith)) {
					result.add(file);
					//logger.info(file.getPath());
				}
			}
			else if (file.isDirectory()) {
				// recursive call!
				List<File> deeperList =  getClassesFromDirTree(file, endsWith);
				result.addAll(deeperList);
			}
		}
		logger.info("Number of test files loaded: (" + result.size() + ").");
		return result;
	}

}
