package com.blooddonor.service;

import com.blooddonor.dto.AuthDtos;
import com.blooddonor.entity.UserAccount;
import com.blooddonor.model.Role;
import com.blooddonor.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        UserAccount existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {

            if (!existingUser.isEmailVerified()) {
                throw new IllegalArgumentException(
                        "Email already registered but not verified. Please use Resend OTP.");
            }

            throw new IllegalArgumentException("Email already registered");
        }

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setEmailVerified(false);

        userRepository.save(user);

        return new AuthDtos.AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                "Registration successful. Please verify your email with OTP.");
    }

    @Transactional
    public AuthDtos.AuthResponse verifyEmail(AuthDtos.VerifyEmailRequest request) {

        UserAccount user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email already verified");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        System.out.println("Email verified successfully for user: " + user.getEmail());

        return new AuthDtos.AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                "Email verified successfully. You can now login.");
    }

    @Transactional
    public void changeRole(Long userId, Role role) {

        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin role cannot be changed");
        }

        user.setRole(role);

        userRepository.save(user);
    }

    public boolean isEmailVerified(String email) {

        UserAccount user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return user.isEmailVerified();
    }
}