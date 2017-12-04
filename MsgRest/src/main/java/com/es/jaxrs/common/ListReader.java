package com.es.jaxrs.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.es.tomee.util.JasonParser;

@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
@Provider
public class ListReader<T> implements MessageBodyReader<List<T>> {
	static final Logger logger = Logger.getLogger(ListReader.class);

	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] anno, MediaType mediaType) {
		logger.info("in isReadable()..."); 
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> readFrom(Class<List<T>> clazz, Type type, Annotation[] anno, MediaType mediaType,
			MultivaluedMap<String, String> map, InputStream is) throws IOException {
		logger.info("in readFrom()... class name: " + clazz.getName());
		logger.info("in readFrom()... Multivalued Map: " + map); 
		if (MediaType.APPLICATION_JSON.toString().equals(mediaType.toString())) {
			String message = IOUtils.toString(is, "UTF-8");
			logger.info("in readFrom()... message: " + message);
			T cls = JasonParser.findJsonArrayClass(message);
			logger.info("Class name: " + cls.toString());
			@SuppressWarnings({ "rawtypes" })
			List list = JasonParser.JsonArrayToList(message, (Class<T>)cls);
			return list;
		}
 		
		return null;
	}

}
