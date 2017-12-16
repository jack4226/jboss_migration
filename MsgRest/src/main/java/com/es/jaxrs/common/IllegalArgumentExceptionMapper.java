package com.es.jaxrs.common;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<java.lang.IllegalArgumentException> {

    @Override
    public Response toResponse(final java.lang.IllegalArgumentException throwable) {
        return Response.ok(throwable.getMessage()).build();
    }
}
