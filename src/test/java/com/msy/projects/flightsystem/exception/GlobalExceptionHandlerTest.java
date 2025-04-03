package com.msy.projects.flightsystem.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msy.projects.flightsystem.security.JwtUtil;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {
    GlobalExceptionHandler.class,
    GlobalExceptionHandlerTest.TestExceptionController.class
})
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private UserDetailsService userDetailsService;
    
    @MockBean
    private JwtUtil jwtUtil;

    @RestController
    @RequestMapping("/test")
    public static class TestExceptionController {
        
        @GetMapping("/resource-not-found")
        public void throwResourceNotFoundException() {
            throw new ResourceNotFoundException("Resource not found test");
        }
        
        @GetMapping("/bad-request")
        public void throwBadRequestException() {
            throw new BadRequestException("Bad request test");
        }
        
        @GetMapping("/authentication")
        public void throwAuthenticationException() {
            throw new AuthenticationException("Authentication error test");
        }
        
        @GetMapping("/access-denied")
        public void throwAccessDeniedException() {
            throw new AccessDeniedException("Access denied test");
        }
        
        @GetMapping("/bad-credentials")
        public void throwBadCredentialsException() {
            throw new BadCredentialsException("Bad credentials test");
        }
        
        @GetMapping("/generic-error")
        public void throwGenericException() {
            throw new RuntimeException("Unexpected error test");
        }
    }

    @Test
    @WithMockUser
    public void whenResourceNotFound_thenReturns404() throws Exception {
        mockMvc.perform(get("/test/resource-not-found")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Resource not found test"));
    }

    @Test
    @WithMockUser
    public void whenBadRequest_thenReturns400() throws Exception {
        mockMvc.perform(get("/test/bad-request")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Bad request test"));
    }

    @Test
    @WithMockUser
    public void whenAuthenticationException_thenReturns401() throws Exception {
        mockMvc.perform(get("/test/authentication")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Authentication error test"));
    }

    @Test
    @WithMockUser
    public void whenAccessDenied_thenReturns403() throws Exception {
        mockMvc.perform(get("/test/access-denied")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Access denied: You don't have permission to access this resource"));
    }

    @Test
    @WithMockUser
    public void whenBadCredentials_thenReturns401() throws Exception {
        mockMvc.perform(get("/test/bad-credentials")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Bad credentials test"));
    }

    @Test
    @WithMockUser
    public void whenGenericError_thenReturns500() throws Exception {
        mockMvc.perform(get("/test/generic-error")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}
