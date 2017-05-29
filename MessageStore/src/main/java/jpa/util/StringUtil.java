package jpa.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;


public final class StringUtil {
	static final Logger logger = Logger.getLogger(StringUtil.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	static final String LF = System.getProperty("line.separator", "\n");

    private StringUtil() {
        // static only
    }

	/**
	 * trim the input string from the right to the provided length.
	 * 
	 * @param str -
	 *            original string
	 * @param len -
	 *            string size
	 * @return string with maximum size of "len" plus three dots.
	 */
	public static String cutWithDots(String str, int len) {
		if (str == null || str.length() <= len || len < 0) {
			return str;
		}
		else if (str.length() > len) {
			return str.substring(0, len) + "...";
		}
		else {
			return str;
		}
	}

	/**
	 * remove double and single quotes from input string
	 * 
	 * @param data -
	 *            input string
	 * @return string with quotes removed, or null if input is null
	 */
	public static String removeQuotes(String data) {
		return StringUtils.removeAll(data, "[\'\"]+");
	}

	/**
	 * Strip off leading and trailing spaces for all String objects in the list
	 * 
	 * @param list -
	 *            a list objects
	 */
	public static void stripAll(List<String> list) {
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			String obj = list.get(i);
			if (obj != null) {
				list.set(i, obj.trim());
			}
		}
	}

	/**
	 * Strip off leading and trailing spaces for all String objects in an array
	 * 
	 * @param list -
	 *            a list of objects
	 */
	public static void stripAll(String[] list) {
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.length; i++) {
			String obj = list[i];
			if (obj != null) {
				list[i] = obj.trim();
			}
		}
	}

	/**
	 * For String fields defined in the bean class with a getter and a setter,
	 * this method will strip off those fields' leading and trailing spaces.
	 * 
	 * @param obj -
	 *            a bean object
	 */
	public static void stripAll(Object obj) {
		if (obj == null) {
			return;
		}
		Method methods[] = obj.getClass().getDeclaredMethods();
		try {
			Class<?> setParms[] = { Class.forName("java.lang.String") };
			for (Method method : methods) {
				int mod = method.getModifiers();
				if (!Modifier.isPublic(mod) || Modifier.isAbstract(mod) || Modifier.isStatic(mod)) {
					continue;
				}
				Class<?> parmTypes[] = method.getParameterTypes();
				if (method.getName().startsWith("get") && parmTypes.length == 0
						&& method.getReturnType().isAssignableFrom(String.class)) {
					// invoke the get method
					String str = (String) method.invoke(obj, (Object[])parmTypes);
					if (str != null) { // trim the string
						String setMethodName = method.getName().replaceFirst("get", "set");
						try {
							Method setMethod = obj.getClass().getMethod(setMethodName, setParms);
							String strParms[] = { str.trim() };
							setMethod.invoke(obj, (Object[])strParms);
						}
						catch (Exception e) {
							logger.error("Exception caught: " + e.getMessage());
							// no corresponding set method, ignore.
						}
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			System.err.println("ERROR: Exception caught during reflection - " + e);
		}
	}

	static Method findHighestMethod(Class<?> cls, Method method) {
		if (cls.getSuperclass() != null) {
			Method parentMethod = findHighestMethod(cls.getSuperclass(), method);
			if (parentMethod != null) {
				return parentMethod;
			}
		}
		Method[] methods = cls.getMethods();
		for (int i = 0; i < methods.length; i++) {
			// we ignore parameter types for now - you need to add this
			if (methods[i].getName().equals(method.getName())) {
				return methods[i];
			}
		}
		// did not find in super class, return self
		return method;
	}

	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	final static int MAX_LEVELS = 12;

	/**
	 * remove the first occurrence of the given string from body text.
	 * 
	 * @param body -
	 *            original body
	 * @param removeStr -
	 *            string to be removed
	 * @return new body
	 */
	public static String removeStringFirst(String body, String removeStr) {
		return StringUtils.removeFirst(body, removeStr); //removeString(body, removeStr, false);
	}

	/**
	 * remove the last occurrence of the given string from body text.
	 * 
	 * @param body -
	 *            original body
	 * @param removeStr -
	 *            string to be removed
	 * @return new body
	 */
	public static String removeStringLast(String body, String removeStr) {
		return removeString(body, removeStr, true);
	}

	private static String removeString(String body, String removeStr, boolean removeLast) {
		if (StringUtils.isEmpty(body) || StringUtils.isEmpty(removeStr)) {
			return body;
		}
		int pos = -1;
		if (removeLast) {
			pos = body.lastIndexOf(removeStr);
		}
		else { // remove first
			pos = body.indexOf(removeStr);
		}
		if (pos >= 0) {
			body = body.substring(0, pos) + body.substring(pos + removeStr.length());
		}
		return body;
	}

	/**
	 * returns a string of dots with given number of dots.
	 * 
	 * @param level -
	 *            specify number dots to be returned
	 * @return string of dots
	 */
	public static String getDots(int level) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < level; i++) {
			sb.append(".");
		}
		return sb.toString();
	}

	/**
	 * trim white spaces from the RIGHT side of a string.
	 * 
	 * @param text
	 *            to be trimmed
	 * @return trimmed string
	 */
	public static String trimRight(String text) {
		/*
		 * We could also do this: ("A" + text).trim().substring(1)
		 * but the performance is poor.
		 */
		if (StringUtils.isEmpty(text)) {
			return text; 
		}
		int idx = text.length() - 1;
		while (idx >= 0 && Character.isWhitespace(text.charAt(idx))) {
			idx--;
		}
		if (idx < 0) {
			return "";
		}
		else {
			return text.substring(0, idx + 1);
		}
	}

	/**
	 * Trim the given string with the given trim value from both sites or either side.
	 * For example: trim("trimABCtrim", "trim") = "ABC"
	 * 				trim("trimABCtrimNot", "trim") = "ABCtrimNot"
	 * 				trim("NottrimABCtrim", "trim") = "NottrimABC"
	 * 
	 * @param string
	 *            The string to be trimmed.
	 * @param trim
	 *            The value to trim the given string off.
	 * @return The trimmed string.
	 */
    public static String trim(String string, String trim) {
        if (StringUtils.isBlank(string)) {
        	return string;
        }
        int start = 0;
        int end = string.length();
        int length = trim.length();
        while ((start + length) <= end && string.substring(start, start + length).equals(trim)) {
			start += length;
		}
		while ((start + length) <= end && string.substring(end - length, end).equals(trim)) {
			end -= length;
		}
        return string.substring(start, end);
    }

    /**
	 * Add PRE tags for plain text message so the spaces and line breaks are
	 * preserved in web browser.
	 * 
	 * @param msgBody -
	 *            message text
	 * @return new message text
	 */
	public static String getHtmlDisplayText(String text) {
		if (text == null) {
			return null;
		}
		if (text.startsWith("<pre>") && text.endsWith("</pre>")) {
			return text;
		}
		String str = StringUtils.replace(text, "<", "&lt;");
		return "<pre>" + StringUtils.replace(str, ">", "&gt;") + "</pre>";
	}

	public static void main(String[] args) {
		logger.info(removeStringFirst("<pre>12345abcdefklqhdkh</pre>", "<pre>"));
		String str1 = "methodtest Stringutil.trim()-methodmethod";
		logger.info(trim(str1,"method"));
	}
}
