package com.skndan.rdp.client;

import java.io.IOException;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class GuacamoleAuthRequestFilter implements ClientRequestFilter {

    private static String token;

    public static void setToken(String newToken) {
        token = newToken;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        if (token != null) {
            requestContext.getHeaders().add("Guacamole-Token", token);
        }
    }
}