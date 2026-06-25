package com.blooddonor.controller.config;

import com.blooddonor.entity.UserAccount;
import com.blooddonor.model.Role;
import com.blooddonor.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataInitializer(
            UserAccountRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email}") String adminEmail,
            @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        // Check if admin exists with configured email
        UserAccount existingAdmin = userRepository.findByEmail(adminEmail).orElse(null);
        
        if (existingAdmin == null) {
            // Create new admin
            UserAccount admin = new UserAccount();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEmailVerified(true);
            userRepository.save(admin);
        } else {
            // Update existing admin to ensure it has admin role and correct password
            if (existingAdmin.getRole() != Role.ADMIN) {
                existingAdmin.setRole(Role.ADMIN);
            }
            existingAdmin.setPasswordHash(passwordEncoder.encode(adminPassword));
            existingAdmin.setEmailVerified(true);
            userRepository.save(existingAdmin);
        }
    }
}
