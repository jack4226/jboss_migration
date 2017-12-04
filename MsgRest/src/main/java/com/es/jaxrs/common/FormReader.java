package com.es.jaxrs.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

@Consumes("application/x-www-form-urlencoded")
@Provider
public class FormReader implements MessageBodyReader<Map<String, String>> {

	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, String> readFrom(Class<Map<String, String>> arg0,
			Type arg1, Annotation[] arg2, MediaType arg3,
			MultivaluedMap<String, String> arg4, InputStream arg5)
			throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
