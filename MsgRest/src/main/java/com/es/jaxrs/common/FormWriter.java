package com.es.jaxrs.common;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

@Produces("text/html")
@Provider
public class FormWriter implements MessageBodyWriter<Map<String, String>> {

	ResponseBuilder rb;
	
	@Override
	public long getSize(Map<String, String> arg0, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4) {
		// deprecated by JAX-RS 2.0 and ignored by Jersey runtime
		return 0;
	}

	@Override
	public boolean isWriteable(Class<?> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3) {
		return arg0 == Map.class;
	}

	@Override
	public void writeTo(Map<String, String> arg0, Class<?> arg1, Type arg2,
			Annotation[] arg3, MediaType arg4,
			MultivaluedMap<String, Object> arg5, OutputStream arg6)
			throws IOException {
		// TODO implement for Map
		StringBuilder sb = new StringBuilder();
		sb.append("<table style=\"width:80%\">");
		for (Iterator<String> it=arg0.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			String value = arg0.get(key);
			sb.append("<tr><td>" + key + "</td><td>" + value + "</td></tr>");
		}
		sb.append("<\table>");
		DataOutputStream dos = new DataOutputStream(arg6);
		dos.writeUTF(sb.toString());
	}

}
