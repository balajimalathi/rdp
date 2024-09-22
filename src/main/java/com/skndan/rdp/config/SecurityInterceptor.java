package com.skndan.rdp.config;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SecurityInterceptor implements ContainerRequestFilter {

  @Inject
  SecurityIdentity securityIdentity; // Inject Quarkus security identity

  @Override
  public void filter(ContainerRequestContext context) { 
    if ("/secret".equals(context.getUriInfo().getPath())) {

      context.abortWith(Response.accepted("forbidden!").build());
    }
  }
}