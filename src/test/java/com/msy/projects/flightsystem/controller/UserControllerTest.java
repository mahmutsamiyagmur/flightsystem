package com.msy.projects.flightsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msy.projects.flightsystem.dto.UserRequestDto;
import com.msy.projects.flightsystem.dto.UserResponseDto;
import com.msy.projects.flightsystem.security.JwtUtil;
import com.msy.projects.flightsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({UserControllerTest.TestConfig.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserService userService;
    
    @BeforeEach
    public void setUp() {
        Mockito.reset(userService);
    }
    

    
    @Configuration
    @EnableMethodSecurity
    static class TestConfig {
        @Bean
        public UserService userService() {
            return org.mockito.Mockito.mock(UserService.class);
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

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        // Arrange
        UserResponseDto user1 = new UserResponseDto(1L, "admin", "ADMIN");
        UserResponseDto user2 = new UserResponseDto(2L, "agency", "AGENCY");
        List<UserResponseDto> users = Arrays.asList(user1, user2);

        when(userService.getAllUsersDto()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[1].username").value("agency"));

        verify(userService).getAllUsersDto();
    }

    @Test
    @WithMockUser(roles = "AGENCY")
    void getAllUsers_WithAgencyRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_ShouldReturnUser() throws Exception {
        // Arrange
        UserResponseDto user = new UserResponseDto(1L, "admin", "ADMIN");
        when(userService.getUserDtoById(1L)).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(userService).getUserDtoById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldCreateAndReturnUser() throws Exception {
        // Arrange
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("newuser");
        requestDto.setPassword("password");
        requestDto.setRole("AGENCY");

        UserResponseDto responseDto = new UserResponseDto(3L, "newuser", "AGENCY");
        when(userService.createUser(any(UserRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("AGENCY"));

        verify(userService).createUser(any(UserRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ShouldUpdateAndReturnUser() throws Exception {
        // Arrange
        UserRequestDto requestDto = new UserRequestDto();
        requestDto.setUsername("updateduser");
        requestDto.setPassword("newpassword");
        requestDto.setRole("AGENCY");

        UserResponseDto responseDto = new UserResponseDto(1L, "updateduser", "AGENCY");
        when(userService.updateUser(anyLong(), any(UserRequestDto.class))).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.role").value("AGENCY"));

        verify(userService).updateUser(eq(1L), any(UserRequestDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldDeleteUser() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUsers_WithInvalidRole_ShouldReturnForbidden() throws Exception {
        // Act & Assert - With invalid role
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }
}
