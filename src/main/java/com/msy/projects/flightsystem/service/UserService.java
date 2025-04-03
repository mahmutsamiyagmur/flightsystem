package com.msy.projects.flightsystem.service;

import com.msy.projects.flightsystem.dto.UserRequestDto;
import com.msy.projects.flightsystem.dto.UserResponseDto;
import com.msy.projects.flightsystem.entity.User;
import com.msy.projects.flightsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Create a user from the User entity directly
     * @param user the user entity
     * @return the created user entity
     * @deprecated Use createUser(UserRequestDto) instead
     */
    @Deprecated
    public User createUser(User user) {
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
    
    /**
     * Create a user from DTO
     * @param userRequestDto the user request data
     * @return the created user response data
     */
    public UserResponseDto createUser(UserRequestDto userRequestDto) {
        User user = new User();
        user.setUsername(userRequestDto.getUsername());
        user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        user.setRole(userRequestDto.getRole());
        user.setEnabled(true);
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    /**
     * Get all users
     * @return list of user entities
     * @deprecated Use getAllUsersDto() instead
     */
    @Deprecated
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Get all users as DTOs
     * @return list of user response DTOs
     */
    public List<UserResponseDto> getAllUsersDto() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     * @param id the user ID
     * @return optional user entity
     * @deprecated Use getUserDtoById() instead
     */
    @Deprecated
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Get user by ID as DTO
     * @param id the user ID
     * @return user response DTO or throws exception if not found
     */
    public UserResponseDto getUserDtoById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(Long id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setUsername(userDetails.getUsername());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        user.setRole(userDetails.getRole());
        user.setEnabled(userDetails.isEnabled());
        user.setAccountNonLocked(userDetails.isAccountNonLocked());
        user.setAccountNonExpired(userDetails.isAccountNonExpired());
        user.setCredentialsNonExpired(userDetails.isCredentialsNonExpired());

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    /**
     * Update a user from DTO
     * @param id the user ID
     * @param userRequestDto the user request data
     * @return the updated user response data
     */
    public UserResponseDto updateUser(Long id, UserRequestDto userRequestDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setUsername(userRequestDto.getUsername());
        if (userRequestDto.getPassword() != null && !userRequestDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        }
        user.setRole(userRequestDto.getRole());

        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    /**
     * Convert User entity to UserResponseDto
     * @param user the user entity
     * @return user response DTO
     */
    private UserResponseDto convertToDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled());
    }
}
