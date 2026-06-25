package com.blooddonor.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {}

    // Registration request
    public record RegisterRequest(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6, max = 100) String password) {}

    // Email verification OTP
    public record VerifyEmailRequest(
            @NotBlank @Email String email,
            @NotBlank String otpCode) {}

    // Login with email and password
    public record LoginRequest(
            @NotBlank @Email String email,
            @NotBlank String password) {}

    // Login OTP verification
    public record LoginOtpRequest(
            @NotBlank @Email String email,
            @NotBlank String otpCode) {}

    // Resend OTP request
    public record ResendOtpRequest(
            @NotBlank @Email String email) {}

    // Authentication response
    public record AuthResponse(
            Long userId,
            String email,
            String role,
            String message) {}
}