package com.es.tomee.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.johnzon.mapper.Mapper;
import org.apache.johnzon.mapper.MapperBuilder;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import jpa.util.PrintUtil;

public class JasonParser<T> {
	static final Logger logger = Logger.getLogger(JasonParser.class);

	public static <T> String objectToJson(@NotNull T jsonObj, @NotNull Class<T> clazz) {
		if (ClassUtils.isPrimitiveOrWrapper(clazz) || jsonObj instanceof String) {
			String jsonStr = (jsonObj == null ? "" : jsonObj.toString());
			return jsonStr;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final Mapper mapper = new MapperBuilder().build();
		mapper.writeObject(jsonObj, baos);
        return new String(baos.toByteArray());
	}

	public static <T> String listToJson(@NotNull List<T> list) {
		StringBuilder sb = new StringBuilder();
		if (!list.isEmpty()) {
			T obj0 = list.get(0);
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>) obj0.getClass();
			sb.append("[");
			sb.append(objectToJson(list.get(0), clazz));
			for (int i = 1; i < list.size(); i++) {
				sb.append(", " + objectToJson(list.get(i), clazz));
			}
			sb.append("]");
		}
		return sb.toString();
	}
	
	public static <T> T jsonToObject(@NotNull String jsonString, @NotNull Class<T> clazz) {
		final Mapper mapper = new MapperBuilder().build();
		T obj = mapper.readObject(jsonString, clazz);
		return obj;
	}

	public static <T> List<T> jsonToList(@NotNull String jsonArray, @NotNull Class<T> clazz) {
		StringReader reader = new StringReader(jsonArray);
		final Mapper mapper = new MapperBuilder().build();
		T[] objs = mapper.readArray(reader, clazz);
		return Arrays.asList(objs);
	}

	@SuppressWarnings("unchecked")
	public static <T> String mapToJson(@NotNull Map<String, Object> jsonMap) {
		StringBuilder sb = new StringBuilder();
		int idx = 0;
		for (Iterator<String> it = jsonMap.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Object obj = jsonMap.get(key);
			if (obj == null) {
				continue;
			}
			Class<?> clazz = obj.getClass();
			String jsonStr = objectToJson((T) obj, (Class<T>) clazz);
			logger.info("Key=" + key + ", Object=" + jsonStr + ", Class name: " + clazz.getName());
			if (obj instanceof String || obj instanceof java.util.Date) {
				jsonStr = "\"" + jsonStr + "\"";
			}
			if (idx++ > 0) {
				sb.append(", ");
			}
			sb.append("\"" + key + "\":" + jsonStr + "");
		}
		String rtnStr = "{" + sb.toString() + "}";
		return rtnStr;
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

}
