package com.msy.projects.flightsystem.config;

import com.msy.projects.flightsystem.entity.User;
import com.msy.projects.flightsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataLoader(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only load data if no users exist
        if (userRepository.count() == 0) {
            loadUsers();
        }
    }

    private void loadUsers() {
        // Create admin user
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setRole("ADMIN");
        userRepository.save(adminUser);

        // Create agency user
        User agencyUser = new User();
        agencyUser.setUsername("agency");
        agencyUser.setPassword(passwordEncoder.encode("agency123"));
        agencyUser.setRole("AGENCY");
        userRepository.save(agencyUser);
        
        System.out.println("Created initial users: admin, agency");
    }
}
