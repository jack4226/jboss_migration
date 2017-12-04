package com.es.tomee.util;

import java.io.ByteArrayOutputStream;
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

public class JasonParser<T> {
	static final Logger logger = Logger.getLogger(JasonParser.class);

	@SuppressWarnings("unchecked")
	public static <T> String ObjectToJson(@NotNull T jsonObj, @NotNull Class<T> clazz) throws JAXBException {
		if (ClassUtils.isPrimitiveOrWrapper(clazz) || jsonObj instanceof String) {
			String jsonStr = (jsonObj == null ? "" : jsonObj.toString());
			return jsonStr;
		}
		if (!isXmlAnnotated(clazz)) {
			if (jsonObj instanceof List) {
				StringBuilder sb = new StringBuilder();
				int idx = 0;
				for (T obj : (List<T>) jsonObj) {
					if (idx++ > 0) {
						sb.append(", ");
					}
					String tmpstr = ObjectToJson(obj, clazz);
					//logger.info("List Item: " + tmpstr);
					if (tmpstr.endsWith("}}") && tmpstr.indexOf(":{") > 0) {
						tmpstr = tmpstr.substring(tmpstr.indexOf(":{") + 1, tmpstr.length() - 1);
					}
					sb.append(tmpstr);
				}
				return sb.toString();
			}
			else if (jsonObj instanceof Map) {
				String jsonStr = MapToJson(((Map<String, Object>) jsonObj));
				return jsonStr;
			}
			else {
				String jsonStr = jsonObj == null ? "" : jsonObj.toString();
				return jsonStr;
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
			logger.error("JSONException caught", e);
			throw e;
		}
	}

	public static <T> T JsonToObject(@NotNull String jsonString, @NotNull Class<T> clazz) {
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
		} catch (JSONException e) {
			logger.error("JSONException caught", e);
		} catch (XMLStreamException e) {
			logger.error("XMLStreamException caught", e);
		} catch (JAXBException e) {
			logger.error("JAXBException caught", e);
		}
		return null;
	}

	public static <T> List<T> JsonArrayToList(@NotNull String jsonString, @NotNull Class<T> clazz) {
		List<T> list = new ArrayList<>();
		if (!isJsonArray(jsonString)) {
			throw new IllegalArgumentException("The input string is not a json array!");
		}
		T cls = findJsonArrayClass(jsonString);
		assert(cls != null);
		@SuppressWarnings({"unchecked" })
		String clsName = ((Class<T>) cls).getSimpleName();
		jsonString = formatJsonArray(jsonString);
		try {
			JSONArray jsonArray = new JSONArray(jsonString);
			for (int i = 0; i < jsonArray.length(); i++) {
				String jsonItem = jsonArray.getString(i);
				jsonItem = "{\"" + clsName + "\":" + jsonItem + "}";
				logger.info("json array line: " + jsonItem);
				T obj = JsonToObject(jsonItem, clazz);
				list.add(obj);
			}
		} catch (JSONException e) {
			logger.error("JSONException caught", e);
		}
		return list;
	}
	
	public static <T> T findJsonArrayClass(String jsonString) {
		if (isJsonArray(jsonString) && !StringUtils.startsWith(jsonString, "[")) {
			String beginning = StringUtils.substring(jsonString, 0, jsonString.indexOf("["));
			String classStr = StringUtils.substring(beginning, beginning.indexOf("\"") + 1, beginning.lastIndexOf("\""));
			try {
				@SuppressWarnings("unchecked")
				T clazz = (T) Class.forName("com.es.ejb.ws.vo." + classStr);
				logger.info("Class name found in json array: " + clazz.toString());
				return clazz;
			} catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException: " + e.getMessage());
			}
		}
		return null;
	}
	
	static <T> String formatJsonArray(String jsonString) {
		if (isJsonArray(jsonString) && !StringUtils.startsWith(jsonString, "[")) {
			String formatted = StringUtils.substring(jsonString, jsonString.indexOf("["), jsonString.lastIndexOf("]") + 1);
			return formatted;
		}
		return jsonString;
	}
	
	static boolean isJsonArray(String jsonString) {
		Stack<String> stack = new Stack<>();
		char[] chars = jsonString.toCharArray();
		int opening = 10;
		int idx = 0;
		while (idx < chars.length) {
			if (chars[idx] == '{' || chars[idx] == '[') {
				if (chars[idx] == '[') {
					opening = Math.min(opening, stack.size());
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
								throw new RuntimeException("Could not find the opening {");
							}
						}
						else if (chars[idx] == ']') {
							if (stack.peek().equals(String.valueOf('['))) {
								stack.pop();
							}
							else {
								throw new RuntimeException("Could not find the opening [");
							}
						}

					}
				}
			}
			idx++;
		}
		if (opening <= 1) {
			return true;
		}
		return false;
	}
 
	@SuppressWarnings("unchecked")
	public static <T> String MapToJson(@NotNull Map<String, Object> jsonMap) throws JAXBException {
		StringBuilder sb = new StringBuilder();

		int idx = 0;
		for (Iterator<String> it = jsonMap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Object obj = jsonMap.get(key);
			String jsonStr = null;

			try {
				Class<?> clazz = Class.forName(obj.getClass().getName());
				logger.info("Class name: " + clazz.getName());
				jsonStr = ObjectToJson((T) obj, (Class<T>)clazz);
				logger.info("Key=" + key + ", Object=" + jsonStr);
				if (idx++ > 0) {
					sb.append(", ");
				}
				if (obj instanceof List) {
					sb.append("\"" + key + "\":[" + jsonStr + "]");
				}
				else {
					sb.append("\"" + key + "\":\"" + jsonStr + "\"");
				}
			} catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException: " + e.getMessage());
			}
		}

		String rtnStr = "{" + sb.toString() + "}";
		return rtnStr;
	}

	static <T> boolean isXmlAnnotated(@NotNull Class<T> clazz) {
		for (Annotation ann : clazz.getAnnotations()) {
			logger.info("Annotation: " + ann.toString());
			if (ann instanceof XmlAccessorType || ann instanceof XmlRootElement) {
				return true;
			}
		}
		return false;
	}


}
