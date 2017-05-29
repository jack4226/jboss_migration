package jpa.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class ExceptionUtil {
	
	public static Exception findException(Exception ex, Class<?> clazz) {
		Exception exception = null;
		if (clazz == null || ex == null) {
			throw new IllegalArgumentException("Input parameters must not be null");
		}
		if (clazz.isInstance(ex)) {
			exception = ex;
		}
		while (ex.getCause()!=null) {
			if (clazz.isInstance(ex.getCause())) {
				exception = (Exception)ex.getCause();
			}
			ex = (Exception)ex.getCause();
		}
		return exception;
	}
	
	public static Exception findRootCause(Exception ex) {
		if (ex == null) {
			throw new IllegalArgumentException("Input parameter must not be null");
		}
		Exception exception = ex;
		while (ex.getCause()!=null) {
			exception = (Exception)ex.getCause();
			ex = exception;
		}
		return exception;
	}
	
	public static String findNestedStackTrace(Exception ex, String className) {
		Exception exception = ex;
		if (ex == null || StringUtils.isBlank(className)) {
			throw new IllegalArgumentException("Input parameters must not be null");
		}
		while (ex.getCause()!=null) {
			exception = (Exception)ex.getCause();
			ex = (Exception)ex.getCause();
		}
		return findNestedStackTrace(ExceptionUtils.getStackTrace(exception), className);
	}
	
	static String findNestedStackTrace(String ex, String className) {
		if (StringUtils.isBlank(ex) || StringUtils.isBlank(className)) {
			throw new IllegalArgumentException("Input parameters must not be null");
		}
		String _name = StringUtils.replace(className, ".", "\\.");
		Pattern p = Pattern.compile("nested exception is:((.{1,100}" + _name + ")(.*))", 
				Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		
		Matcher m = p.matcher(ex);
		if (m.find() && m.groupCount()>=1) {
			for (int i=0; i<=m.groupCount(); i++) {
				//System.out.println("[" + i + "]: " + m.group(i));
			}
			return m.group(1);
		}
		return null;
	}

	static Pattern classNameRegex = Pattern.compile("jpa\\.(jbatch|service|msgui)\\..{1,100}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	
	public static String findCallingClass(Exception e) {
		for (StackTraceElement elm : e.getStackTrace()) {
			String className = elm.getClassName();
			Matcher m = classNameRegex.matcher(className);
			if (m.find()) {
				for (int i = 0; i <= m.groupCount(); i++) {
					//logger.info("group[" + i + "]: " + m.group(i));
				}
				String stackLine = elm.getClassName() + ":" + elm.getMethodName() + "(" + elm.getLineNumber() + ")";
				return stackLine;
			}
		}
		return "Root class: unknown";
	}

}
