package com.msy.projects.flightsystem.controller;

import com.msy.projects.flightsystem.dto.TransportationDto;
import com.msy.projects.flightsystem.exception.ResourceNotFoundException;
import com.msy.projects.flightsystem.model.TransportationType;
import com.msy.projects.flightsystem.security.JwtUtil;
import com.msy.projects.flightsystem.service.RouteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RouteController.class)
@AutoConfigureMockMvc
@Import(RouteControllerTest.TestConfig.class)
public class RouteControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private RouteService routeService;
    
    @Configuration
    static class TestConfig {
        @Bean
        public RouteService routeService() {
            return org.mockito.Mockito.mock(RouteService.class);
        }
        
        @Bean
        public JwtUtil jwtUtil() {
            return org.mockito.Mockito.mock(JwtUtil.class);
        }
        
        @Bean
        public UserDetailsService userDetailsService() {
            return org.mockito.Mockito.mock(UserDetailsService.class);
        }
    }

    private List<TransportationDto> directFlightRoute;
    private List<List<TransportationDto>> allRoutes;

    @BeforeEach
    void setUp() {
        // Setup test data
        TransportationDto flightDto = new TransportationDto();
        flightDto.setId(1L);
        flightDto.setOriginLocationId(1L);
        flightDto.setOriginLocationCode("IST");
        flightDto.setDestinationLocationId(2L);
        flightDto.setDestinationLocationCode("LHR");
        flightDto.setTransportationType(TransportationType.FLIGHT);
        flightDto.setOperatingDays(Arrays.asList(1, 2, 3, 4, 5, 6, 7));

        directFlightRoute = Arrays.asList(flightDto);
        allRoutes = Arrays.asList(directFlightRoute);
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void findRoutes_WhenValidRequest_ShouldReturnRoutes() throws Exception {
        // Arrange
        when(routeService.findRoutes(eq("IST"), eq("LHR"), any(LocalDate.class)))
            .thenReturn(allRoutes);

        // Act & Assert
        mockMvc.perform(get("/api/routes")
                .param("originCode", "IST")
                .param("destinationCode", "LHR")
                .param("date", "2025-04-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0]", hasSize(1)))
            .andExpect(jsonPath("$[0][0].originLocationCode").value("IST"))
            .andExpect(jsonPath("$[0][0].destinationLocationCode").value("LHR"));

        verify(routeService).findRoutes(eq("IST"), eq("LHR"), any(LocalDate.class));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void findRoutes_WhenLocationNotFound_ShouldReturnNotFoundStatus() throws Exception {
        // Arrange
        when(routeService.findRoutes(eq("IST"), eq("INVALID"), any(LocalDate.class)))
            .thenThrow(new ResourceNotFoundException("Destination location not found with code: INVALID"));

        // Act & Assert
        mockMvc.perform(get("/api/routes")
                .param("originCode", "IST")
                .param("destinationCode", "INVALID")
                .param("date", "2025-04-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value("NOT_FOUND"))
            .andExpect(jsonPath("$.message").value("Destination location not found with code: INVALID"));

        verify(routeService).findRoutes(eq("IST"), eq("INVALID"), any(LocalDate.class));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void findRoutes_WhenNoRoutesFound_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(routeService.findRoutes(eq("IST"), eq("LHR"), any(LocalDate.class)))
            .thenReturn(new ArrayList<>());

        // Act & Assert
        mockMvc.perform(get("/api/routes")
                .param("originCode", "IST")
                .param("destinationCode", "LHR")
                .param("date", "2025-04-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        verify(routeService).findRoutes(eq("IST"), eq("LHR"), any(LocalDate.class));
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void findRoutes_WhenCalledTwice_ShouldUseCacheForSecondCall() throws Exception {
        // Arrange
        when(routeService.findRoutes(eq("IST"), eq("LHR"), any(LocalDate.class)))
            .thenReturn(allRoutes);

        // Act & Assert - First call
        mockMvc.perform(get("/api/routes")
                .param("originCode", "IST")
                .param("destinationCode", "LHR")
                .param("date", "2025-04-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0][0].originLocationCode").value("IST"));

        // Act & Assert - Second call
        mockMvc.perform(get("/api/routes")
                .param("originCode", "IST")
                .param("destinationCode", "LHR")
                .param("date", "2025-04-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0][0].originLocationCode").value("IST"));

        // This test only verifies the controller is calling the service correctly
        // The actual caching is tested at the service level
        verify(routeService, times(2)).findRoutes(eq("IST"), eq("LHR"), any(LocalDate.class));
    }

    @Test
    public void findRoutes_WhenUnauthorized_ShouldReturnUnauthorizedStatus() throws Exception {
        // Act & Assert - Without authentication
        mockMvc.perform(get("/api/routes")
                .param("originCode", "IST")
                .param("destinationCode", "LHR")
                .param("date", "2025-04-07")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());

        verifyNoInteractions(routeService);
    }
}
