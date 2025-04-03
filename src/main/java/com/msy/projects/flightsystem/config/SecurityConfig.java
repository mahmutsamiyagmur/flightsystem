package com.msy.projects.flightsystem.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.msy.projects.flightsystem.service.CustomUserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.msy.projects.flightsystem.security.CustomAccessDeniedHandler;
import com.msy.projects.flightsystem.security.JwtRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAccessDeniedHandler accessDeniedHandler;
    
    @Autowired
    private JwtRequestFilter jwtRequestFilter;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    public SecurityConfig(CustomAccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Allow access to auth endpoints for anonymous users
                .requestMatchers("/auth/**").permitAll()
                
                // Allow access to route endpoints for both ADMIN and AGENCY roles
                .requestMatchers("/routes/**").hasAnyRole("ADMIN", "AGENCY")
                
                // Allow GET access to locations for both ADMIN and AGENCY roles
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/locations/**").hasAnyRole("ADMIN", "AGENCY")
                
                // Restrict modification operations on locations to ADMIN only
                .requestMatchers(org.springframework.http.HttpMethod.POST, "/locations/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, "/locations/**").hasRole("ADMIN")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/locations/**").hasRole("ADMIN")
                
                // Allow access to transportation endpoints for ADMIN role only
                .requestMatchers("/transportations/**").hasRole("ADMIN")
                
                // Swagger/OpenAPI endpoints accessible to all authenticated users
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").authenticated()
                
                // Any other request needs authentication
                .anyRequest().authenticated()
            )
            // Disable session management since we're using JWT tokens
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Add JWT filter before the standard authentication filter
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            // Exception handling for access denied
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler)
            );
        
        return http.build();
    }
    
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        // Create an authentication provider using non-deprecated approach
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder);
        provider.setUserDetailsService(userDetailsService);
        // The constructor taking a list is preferred over the deprecated constructor
        return new ProviderManager(java.util.Collections.singletonList(provider));
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
