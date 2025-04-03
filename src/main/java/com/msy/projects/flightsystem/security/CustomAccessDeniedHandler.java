package com.msy.projects.flightsystem.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        String errorMessage = "Access denied: You do not have permission to access this resource";
        
        response.getWriter().write(objectMapper.writeValueAsString(
            new ErrorResponse(HttpStatus.FORBIDDEN.value(), errorMessage)
        ));
    }
    
    // Simple error response class
    private static class ErrorResponse {
        private final int status;
        private final String message;
        
        public ErrorResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }
        
        // Used by Jackson for JSON serialization
        @com.fasterxml.jackson.annotation.JsonProperty
        public int getStatus() {
            return status;
        }
        
        // Used by Jackson for JSON serialization
        @com.fasterxml.jackson.annotation.JsonProperty
        public String getMessage() {
            return message;
        }
    }
}
