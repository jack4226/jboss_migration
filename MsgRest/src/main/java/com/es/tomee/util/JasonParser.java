package com.es.tomee.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.validation.constraints.NotNull;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import jpa.util.PrintUtil;

public class JasonParser<T> {
	static final Logger logger = Logger.getLogger(JasonParser.class);

	@SuppressWarnings("unchecked")
	public static <T> String objectToJson(@NotNull T jsonObj, @NotNull Class<T> clazz) throws JAXBException {
		if (ClassUtils.isPrimitiveOrWrapper(clazz) || jsonObj instanceof String) {
			String jsonStr = (jsonObj == null ? "" : jsonObj.toString());
			return jsonStr;
		}
		if (!isXmlAnnotated(clazz)) {
			if (jsonObj instanceof List) {
				return listToJson((List<T>) jsonObj);
			}
			else if (jsonObj instanceof Map) {
				return mapToJson(((Map<String, Object>) jsonObj));
			}
			else {
				return (jsonObj == null ? "" : jsonObj.toString());
			}
		}
		try {
			JAXBContext jc = JAXBContext.newInstance(clazz);
			
			Configuration config = new Configuration();
	        MappedNamespaceConvention con = new MappedNamespaceConvention(config);
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        Writer writer = new OutputStreamWriter(baos);
	        XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(con, writer);
	 
	        Marshaller marshaller = jc.createMarshaller();
	        marshaller.marshal(jsonObj, xmlStreamWriter);
	        return new String(baos.toByteArray());
		} catch (JAXBException e) {
			logger.error("JAXBException caught", e);
			throw e;
		}
	}

	public static <T> String listToJson(@NotNull List<T> list) throws JAXBException {
		StringBuilder sb = new StringBuilder();
		if (!list.isEmpty()) {
			T obj0 = list.get(0);
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) obj0.getClass();
			sb.append("{\"" + clazz.getSimpleName() + "\":[");
	
			int idx = 0;
			for (T obj : (List<T>) list) {
				if (idx++ > 0) {
					sb.append(", ");
				}
				String tmpStr = objectToJson(obj, clazz);
				//logger.info("List Item: " + tmpStr);
				if (tmpStr.endsWith("}}") && tmpStr.indexOf(":{") > 0) {
					// Strip off the class name
					tmpStr = tmpStr.substring(tmpStr.indexOf(":{") + 1, tmpStr.length() - 1);
				}
				sb.append(tmpStr);
			}
			sb.append("]}");
		}
		return sb.toString();
	}
	
	public static <T> T jsonToObject(@NotNull String jsonString, @NotNull Class<T> clazz) {
		if (isJsonArray(jsonString)) {
			String arrayName = getXmlRootName(clazz);
			String nameToReplace = parseJsonArrayName(jsonString);
			if (StringUtils.isNotBlank(nameToReplace)) {
				jsonString.replaceFirst(nameToReplace, arrayName);
			}
		}
		try {
			JSONObject obj = new JSONObject(jsonString);
			Configuration config = new Configuration();
			MappedNamespaceConvention con = new MappedNamespaceConvention(config);
		    XMLStreamReader xmlStreamReader = new MappedXMLStreamReader(obj, con);

			JAXBContext jc = JAXBContext.newInstance(clazz);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			@SuppressWarnings("unchecked")
			T vo = (T) unmarshaller.unmarshal(xmlStreamReader);
			return vo;
		} catch (JSONException | XMLStreamException |JAXBException e) {
			logger.error("Exception caught", e);
			throw new RuntimeException(e.getClass().getSimpleName() + " caught, " + e.getMessage());
		}
	}

	public static <T> List<T> jsonArrayToList(@NotNull String jsonString, @NotNull Class<T> clazz) {
		List<T> list = new ArrayList<>();
		if (!isJsonArray(jsonString)) {
			throw new IllegalArgumentException("The input string is not a json array!");
		}
		String arrayName = parseJsonArrayName(jsonString);
		jsonString = removeArrayNameIfPresent(jsonString);
		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); i++) {
				String jsonItem = jsonArray.getString(i);
				if (StringUtils.isNotBlank(arrayName)) {
					jsonItem = "{\"" + arrayName + "\":" + jsonItem + "}";
				}
				logger.info("json array line: " + jsonItem);
				T obj = jsonToObject(jsonItem, clazz);
				list.add(obj);
			}
			return list;
		} catch (JSONException e) {
			logger.error("JSONException caught", e);
			throw new RuntimeException("JSONException caught: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> String mapToJson(@NotNull Map<String, Object> jsonMap) throws JAXBException {
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		for (Iterator<String> it = jsonMap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Object obj = jsonMap.get(key);
			if (obj == null) {
				continue;
			}
			String jsonStr = null;
			Class<?> clazz = obj.getClass();
			logger.info("Class name: " + clazz.getName());
			jsonStr = objectToJson((T) obj, (Class<T>) clazz);
			logger.info("Key=" + key + ", Object=" + jsonStr);
			if (idx++ > 0) {
				sb.append(", ");
			}
			if (obj instanceof List) {
				jsonStr = removeArrayNameIfPresent(jsonStr);
				sb.append("\"" + key + "\":" + jsonStr + "");
			}
			else {
				sb.append("\"" + key + "\":\"" + jsonStr + "\"");
			}
		}
		String rtnStr = "{" + sb.toString() + "}";
		return rtnStr;
	}

	/*
	 * Returns JSON array name, for example it will return "name" from {"name":[{},{}]}
	 */
	public static String parseJsonArrayName(String jsonString) {
		if (isJsonArray(jsonString) && !StringUtils.startsWith(jsonString, "[")) {
			String beginning = StringUtils.substring(jsonString, 0, jsonString.indexOf("["));
			String arrayName = StringUtils.substring(beginning, beginning.indexOf("\"") + 1, beginning.lastIndexOf("\""));
			return arrayName;
		}
		return null;
	}

	/*
	 * Returns a Class by JSON array name, for example it will return "name" from {"name":[{},{}]}
	 */
	public static <T> Class<T> findJsonArrayClass(String jsonString) {
		String arrayName = parseJsonArrayName(jsonString);
		if (StringUtils.isNotBlank(arrayName)) {
			List<Class<T>> list = findClassesByPattern("classpath*:com/es/ejb/ws/vo/" + StringUtils.capitalize(arrayName) + ".class");
			if (!list.isEmpty()) {
				return list.get(0);
			}
		}
		return null;
	}
	
	public static <T> List<Class<T>> findClassesByPattern(String pattern) {
		// pattern example 1: classpath*:edi/*/vo/*.class
		// pattern example 2: classpath*:edi/*/vo/QueueInVo.class
		logger.info("Find classes by pattern: " + pattern);
		ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
		List<Class<T>> clsList = new ArrayList<>();
		try {
			Resource[] resources = resourceResolver.getResources(pattern);
			for (Resource res : resources) {
				logger.info("Resource: " + PrintUtil.prettyPrint(res, 3));
				String path = null;
				if (res instanceof FileSystemResource) {
					File file = res.getFile();
					path = file.getPath();
					path = StringUtils.replace(path, "\\", "/");
					logger.info("File path: " + path);
				}
				else if (res instanceof UrlResource) {
					java.net.URL url = res.getURL();
					path = url.getPath();
					logger.info("URL path: " + path);
				}
				else {
					logger.warn("Unchecked object type: " + res.getClass().getName());
					continue;
				}
				
				String classNamePath = path;
				if (path.startsWith("file:") && path.contains(".jar!/")) {
					classNamePath = path.substring(path.indexOf(".jar!/") + 6);
				}
				else if (StringUtils.contains(path, "classes/")) {
					classNamePath = path.substring(path.indexOf("classes/") + 8);
				}
				logger.info("Class name path: " + classNamePath);
				
				String className = org.springframework.util.ClassUtils.convertResourcePathToClassName(classNamePath);
				String pkgName = org.apache.commons.lang3.ClassUtils.getPackageName(className);
				logger.info("Package name: " + pkgName);
				try {
					@SuppressWarnings("unchecked")
					Class<T> clazz = (Class<T>) Class.forName(pkgName);
					clsList.add(clazz);
				} catch (ClassNotFoundException e) {
					logger.warn("Could not find the class: " + e.getMessage());
				}
			}
		} catch (IOException e) {
			logger.error("IOException caught", e);
		}
		return clsList;
	}
	
	/*
	 *  Strip off the JSON array name, for example it will return [{},{}] from {"name":[{},{}]}
	 */
	private static <T> String removeArrayNameIfPresent(String jsonString) {
		if (isJsonArray(jsonString) && !StringUtils.startsWith(jsonString, "[")) {
			String formatted = StringUtils.substring(jsonString, jsonString.indexOf("["), jsonString.lastIndexOf("]") + 1);
			return formatted;
		}
		return jsonString;
	}
	
	/*
	 * Returns true is the input looks like {"name":[{},{}]} or [{},{}]
	 */
	private static boolean isJsonArray(String jsonString) {
		Stack<String> stack = new Stack<>();
		char[] chars = jsonString.toCharArray();
		int firstOpening = 10;
		int idx = 0;
		while (idx < chars.length) {
			if (chars[idx] == '{' || chars[idx] == '[') {
				if (chars[idx] == '[') {
					firstOpening = Math.min(firstOpening, stack.size());
				}
				stack.push(String.valueOf(chars[idx]));
			}
			else {
				if (chars[idx] == '}' || chars[idx] == ']') {
					if (!stack.isEmpty()) {
						if (chars[idx] == '}') {
							if (stack.peek().equals(String.valueOf('{'))) {
								stack.pop();
							}
							else {
								throw new RuntimeException("Could not find matching opening {");
							}
						}
						else if (chars[idx] == ']') {
							if (stack.peek().equals(String.valueOf('['))) {
								stack.pop();
							}
							else {
								throw new RuntimeException("Could not find matching opening [");
							}
						}

					}
					else {
						throw new RuntimeException("Could not find matching opening { or [");
					}
				}
			}
			idx++;
		}
		if (firstOpening <= 1) {
			return true;
		}
		return false;
	}
 
	private static <T> boolean isXmlAnnotated(@NotNull Class<T> clazz) {
		for (Annotation anno : clazz.getAnnotations()) {
			logger.info("Class Annotation: " + anno.toString());
			if (anno instanceof XmlAccessorType || anno instanceof XmlRootElement) {
				return true;
			}
		}
		return false;
	}

	private static <T> String getXmlRootName(@NotNull Class<T> clazz) {
		for (Annotation anno : clazz.getAnnotations()) {
			if (anno instanceof XmlRootElement) {
				logger.info("XmlRootElement Annotation: " + anno.toString());
				String rootName = ((XmlRootElement) anno).name();
				if (StringUtils.isNotBlank(rootName)) {
					return rootName;
				}
			}
		}
		return clazz.getSimpleName();
	}

}
