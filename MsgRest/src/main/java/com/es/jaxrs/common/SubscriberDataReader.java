package com.es.jaxrs.common;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;

import jpa.model.SubscriberData;

@Consumes("text/html") //"application/x-www-form-urlencoded")
@Produces("text/html")
@Provider
public class SubscriberDataReader implements MessageBodyReader<SubscriberData> {

	ResponseBuilder rb = new ResponseBuilderImpl();
	
	@Override
	public boolean isReadable(Class<?> clazz, Type type, Annotation[] anno, MediaType mediaType) {
		return clazz == SubscriberData.class;
	}

	@Override
	public SubscriberData readFrom(Class<SubscriberData> clazz, Type type, Annotation[] anno, MediaType mediaType,
			MultivaluedMap<String, String> map, InputStream is) throws IOException {
		// TODO implement
		try {
	        JAXBContext jaxbContext = JAXBContext.newInstance(SubscriberData.class);
	        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
	        SubscriberData sbsrData = (SubscriberData) unmarshaller.unmarshal(is);
	        return sbsrData;
	    } catch (JAXBException jaxbException) {
	        throw new IOException(jaxbException);
	    }
	}

}
