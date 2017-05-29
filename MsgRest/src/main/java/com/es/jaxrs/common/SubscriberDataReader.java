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

import org.apache.cxf.jaxrs.impl.ResponseBuilderImpl;

import jpa.model.SubscriberData;

@Consumes("text/html") //"application/x-www-form-urlencoded")
@Produces("text/html")
@Provider
public class SubscriberDataReader implements MessageBodyReader<SubscriberData> {

	ResponseBuilder rb = new ResponseBuilderImpl();
	
	@Override
	public boolean isReadable(Class<?> arg0, Type arg1, Annotation[] arg2,
			MediaType arg3) {
		return arg0 == SubscriberData.class;
	}

	@Override
	public SubscriberData readFrom(Class<SubscriberData> arg0,
			Type arg1, Annotation[] arg2, MediaType arg3,
			MultivaluedMap<String, String> arg4, InputStream arg5)
			throws IOException {
		// TODO implement
		try {
	        JAXBContext jaxbContext = JAXBContext.newInstance(SubscriberData.class);
	        SubscriberData sbsrData = (SubscriberData) jaxbContext.createUnmarshaller()
	            .unmarshal(arg5);
	        return sbsrData;
	    } catch (JAXBException jaxbException) {
	        throw new IOException(jaxbException);
	    }
	}

}
