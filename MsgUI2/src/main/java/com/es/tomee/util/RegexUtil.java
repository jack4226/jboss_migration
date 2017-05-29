package com.es.tomee.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class RegexUtil {
	final static Logger logger = Logger.getLogger(RegexUtil.class);
	final static String LF = System.getProperty("line.separator", "\n");
	
	public static void main(String[] args) {
		logger.info("Is redelivery property? " + isJmsDeliveryCountProperty("JMSXDeliveryCount"));
	}
	
	public static boolean isJmsDeliveryCountProperty(String prop_name) {
		Pattern p = Pattern.compile("(jms\\w{0,}delivery\\w{0,}count)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(prop_name);
		if (m.find()) {
			for (int i=0; i<=m.groupCount(); i++) {
				logger.info(i + " = " + m.group(i));
			}
			return true;
		}
		return false;
	}
}
