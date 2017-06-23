package jpa.util;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Category;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

public class Log4jConfigUtil {

	public static void main(String[] args) {
		modifyLogLevel(Level.ERROR, Level.INFO);
		
		try {
			addFileAppender("./logs/fileAppender.log", false, Level.DEBUG);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * modify the log level for console appender and file appender.
	 * @param console - console logging level
	 * @param file - file logging level
	 */
	public static void modifyLogLevel(Level console, Level file) {
		modifyLogLevel(console, file, false);
	}

	public static void modifyLogLevel(Level console, Level file, boolean setHibernateLoggingLevelToInfo) {
		Logger root = Logger.getRootLogger();
		@SuppressWarnings("unchecked")
		Enumeration<Appender> appenders = root.getAllAppenders();
		while(appenders.hasMoreElements()) {
			Appender appender = appenders.nextElement();
			if ("CONSOLE".equalsIgnoreCase(appender.getName()) && console != null) {
				System.out.println("Appender: " + appender.getName() + ", level: " + console.toString());
				if (appender instanceof ConsoleAppender)
					((ConsoleAppender)appender).setThreshold(console);
			}
			else if ("FILE".equalsIgnoreCase(appender.getName()) && file != null) {
				System.out.println("Appender: " + appender.getName() + ", level: " + file.toString());
				if (appender instanceof FileAppender) {
					((FileAppender)appender).setThreshold(file);
				}
				if (appender instanceof RollingFileAppender) {
					((RollingFileAppender)appender).rollOver();
				}
			}
		}
		
		if (setHibernateLoggingLevelToInfo) {
			@SuppressWarnings({ "unchecked" })
			Enumeration<Category> categories = LogManager.getCurrentLoggers();
			while (categories.hasMoreElements()) {
				Category category = categories.nextElement();
				if (category.getName().startsWith("org.hibernate")) {
					if (Level.TRACE.equals(category.getLevel()) || Level.DEBUG.equals(category.getLevel())) {
						System.out.println(category.getName() + ", Level: " + category.getLevel() + " --> " + Level.INFO);
						category.setLevel(Level.INFO);
					}
				}
			}
		}
	}
	
	public static void addFileAppender(String fileName, boolean append, Level level) throws IOException {
		addFileAppender(null, fileName, append, level);
	}
	
	public static Appender addFileAppender(Layout layout, String fileName, boolean append, Level level) throws IOException {
		if (layout == null) {
			layout = new PatternLayout("[%d] [%p,%c:%L] %m%n");
		}
		Logger root = Logger.getRootLogger();
		FileAppender appender = new FileAppender(layout, fileName, append);
		if (level == null) {
			level = Level.INFO;
		}
		appender.setThreshold(level);
		System.out.println("File appender for \"" + fileName + "\" created.");
		//appender.setName("");
		root.addAppender(appender);
		return appender;
	}
}
