package com.skndan.rdp.exception;
 
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GenericExceptionMapper implements ExceptionMapper<GenericException> {

    @Override
    public Response toResponse(GenericException exception) {
        return Response.status(exception.getStatusCode())
                .entity(exception.getMessage())
                .build();
    }
}