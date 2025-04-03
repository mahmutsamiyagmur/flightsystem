package com.msy.projects.flightsystem.dto;

/**
 * DTO for User entity responses
 * Used to return user data without sensitive information like passwords
 */
public class UserResponseDto {
    
    private Long id;
    private String username;
    private String role;
    private boolean enabled;
    
    public UserResponseDto() {
    }
    
    public UserResponseDto(Long id, String username, String role) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.enabled = true;
    }
    
    public UserResponseDto(Long id, String username, String role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.enabled = enabled;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
