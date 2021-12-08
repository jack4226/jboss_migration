package com.es.tomee.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import jpa.model.EmailAddress;
import jpa.util.PrintUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.es.ejb.ws.vo.MailingListVo;

public class BeanReflUtil {
	protected final static Logger logger = LogManager.getLogger(BeanReflUtil.class);

	/*
	 * copy properties from form parameters by invoking its setters using reflection.
	 */
	public static void copyProperties(Object dest, MultivaluedMap<String, String> formParams) {
		Map<String, String> methodMap = new LinkedHashMap<String, String>();
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			List<String> values = formParams.get(key);
			methodMap.put(key, StringUtils.join(values.toArray()));
		}
		copyProperties(dest, methodMap);
	}
	
	public static void copyProperties(Object dest, Map<String, String> formParams) {
		Map<String, Method> settersMap = new LinkedHashMap<String, Method>();
		Method methods[] = dest.getClass().getMethods();
		
		// retrieve method setters
		for (Method method : methods) {
			String methodName = method.getName();
			if (Modifier.isPublic(method.getModifiers())
					&& !Modifier.isStatic(method.getModifiers())
					&& (methodName.length() > 3 && methodName.startsWith("set"))) {
				if (method.getParameterTypes().length == 1) {
					settersMap.put(methodName, method);
					//logger.info("Method setter added: " + methodName);
				}
			}
		}
		
		for (Iterator<String> it = formParams.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			String value = formParams.get(key);
			//logger.info("Form Key: " + key + ", Values: " + formParams.get(key));
			String setterName = "set" + WordUtils.capitalize(key);
			//logger.info("Method name constructed: " + methodName);
			if (settersMap.containsKey(setterName)) {
				Method setter = settersMap.get(setterName);
				Class<?> paramType = setter.getParameterTypes()[0];
				if (paramType.isAssignableFrom(String.class)) {
					String setDisp =  dest.getClass().getSimpleName() + "." + setterName + "(\"" + value + "\")";
					try {
						logger.info("Executing: " + setDisp);
						setter.invoke(dest, value);
					} catch (Exception e) {
						logger.error("Failed to execute: " + setDisp, e);
					}
				}
				else if ("int".equals(paramType.getName()) || paramType.isAssignableFrom(Integer.class)) {
					String setDisp =  dest.getClass().getSimpleName() + "." + setterName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setDisp);
							setter.invoke(dest, Integer.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setDisp, e);
						}
					}
				}
				else if ("boolean".equals(paramType.getName()) || paramType.isAssignableFrom(Boolean.class)) {
					String setDisp =  dest.getClass().getSimpleName() + "." + setterName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setDisp);
							setter.invoke(dest, Boolean.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setDisp, e);
						}
					}
				}
				else if (paramType.isAssignableFrom(java.sql.Timestamp.class)) {
					String setDisp =  dest.getClass().getSimpleName() + "." + setterName + "(" + value + ")";
					if (StringUtils.isNotBlank(value)) {
						try {
							logger.info("Executing: " + setDisp);
							setter.invoke(dest, java.sql.Timestamp.valueOf(value));
						} catch (Exception e) {
							logger.error("Failed to execute:" + setDisp, e);
						}
					}
				}
			}
		}
	}

	public static void copyProperties(Object dest, Object orig) {
		Map<String, Method> gettersDestMap = new LinkedHashMap<String, Method>();
		Map<String, Method> settersDestMap = new LinkedHashMap<String, Method>();
		Map<String, Method> gettersOrigMap = new LinkedHashMap<String, Method>();
		Method destMethods[] = dest.getClass().getDeclaredMethods();
		
		// retrieve method getters and setters
		for (Method destMethod : destMethods) {
			String methodName = destMethod.getName();
			if (Modifier.isPublic(destMethod.getModifiers())
					&& !Modifier.isStatic(destMethod.getModifiers())) {
				if (methodName.length() > 3 && methodName.startsWith("get")
						|| methodName.length() > 2 && methodName.startsWith("is")) {
					try {
						Method origMethod = orig.getClass().getMethod(methodName, destMethod.getParameterTypes());
						if (!destMethod.getReturnType().equals(origMethod.getReturnType())
								|| !Modifier.isPublic(origMethod.getModifiers())) {
							continue;
						}
						if (destMethod.getParameterTypes().length == 0) {
							gettersDestMap.put(methodName, destMethod);
							gettersOrigMap.put(methodName, origMethod);
							//logger.info("Method getter added: " + methodName);
						}
					}
					catch (Exception e) {
						logger.warn("Method name obj1." + methodName + " not found in obj2, ignored.");
						continue;
					}
				}
				else if (methodName.length() > 3 && methodName.startsWith("set")) {
					try {
						Method origMethod = orig.getClass().getMethod(methodName, destMethod.getParameterTypes());
						if (!destMethod.getReturnType().equals(origMethod.getReturnType())) {
							continue;
						}
						if (destMethod.getParameterTypes().length == 1) {
							settersDestMap.put(methodName, destMethod);
							//logger.info("Method setter added: " + methodName);
						}
					}
					catch (Exception e) {
						logger.warn("Method name obj1." + methodName + " not found in obj2, ignored.");
						continue;
					}
				}
			}
		}
		
		Object [] params = {};
		// loop through getters and copy data from orig to dest if their values are different
		for (Iterator<String> it=gettersDestMap.keySet().iterator(); it.hasNext();) {
			String getterName = it.next();
			String setterName = null;
			if (getterName.startsWith("get")) {
				setterName = getterName.replaceFirst("get", "set");
			}
			else {
				setterName = getterName.replaceFirst("is", "set");
			}
			if (!settersDestMap.containsKey(setterName)) {
				continue;
			}
			Method destMethod = gettersDestMap.get(getterName);
			Method origMethod = gettersOrigMap.get(getterName);
			Method destSetter = settersDestMap.get(setterName);
			try {
				Object destRst = destMethod.invoke(dest, params);
				Object origRst = origMethod.invoke(orig, params);
				String setDisp = destSetter.getClass().getSimpleName() + "." + setterName + "(" + origRst + ")";
				if (destRst == null) {
					if (origRst != null) {
						logger.info("Invoking.1 " + setDisp);
						destSetter.invoke(dest, origRst);
					}
				}
				else if (!destRst.equals(origRst)) {
					logger.info("Invoking.2 " + setDisp);
					destSetter.invoke(dest, origRst);
				}
			}
			catch (Exception e) {
				logger.error("Exception caught", e);
			}
		}
	}
	
	public static void main(String[] args) {
		Map<String, String> formMap = new LinkedHashMap<String, String>();
		formMap.put("address", "mynewaddress@test.com");
		formMap.put("updtUserId", "new user");
		formMap.put("bounceCount", "123");
		formMap.put("acceptHtml", "false");
		formMap.put("lastSentTime", "2015-04-30 13:12:34.123456");
		formMap.put("updtTime", "2015-04-30 13:12:34.123456789");
		EmailAddress obj = new EmailAddress();
		copyProperties(obj, formMap);
		logger.info(PrintUtil.prettyPrint(obj));
		
		// test fieldsDiff
		jpa.model.MailingList ml = new jpa.model.MailingList();
		MailingListVo vo = new MailingListVo();
		ml.setDisplayName("Display name 1");
		vo.setDisplayName("Display Name 2");
		vo.setListId("SMPLLST1");
		vo.setBuiltin(true);
		vo.setCreateTime(new java.sql.Timestamp(System.currentTimeMillis()));
		copyProperties(ml, vo);
		logger.info(PrintUtil.prettyPrint(ml));
	}
}
