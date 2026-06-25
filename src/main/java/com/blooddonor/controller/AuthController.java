package com.blooddonor.controller;

import com.blooddonor.dto.AuthDtos;
import com.blooddonor.security.CustomUserDetails;
import com.blooddonor.security.CustomUserDetailsService;
import com.blooddonor.service.AuthService;
import com.blooddonor.service.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          OtpService otpService,
                          CustomUserDetailsService userDetailsService) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.userDetailsService = userDetailsService;
    }

    // ─── STEP 1: Register → sends email OTP ────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthDtos.AuthResponse> register(
            @Valid @RequestBody AuthDtos.RegisterRequest request) {

        AuthDtos.AuthResponse response = authService.register(request);
        otpService.generateAndSendOtp(request.email(), "REGISTRATION");
        return ResponseEntity.ok(response);
    }

    // ─── STEP 2: Verify email OTP after registration ────────────────────────────
    @PostMapping("/verify-email")
    public ResponseEntity<AuthDtos.AuthResponse> verifyEmail(
            @Valid @RequestBody AuthDtos.VerifyEmailRequest request) {

        if (!otpService.verifyOtp(request.email(), request.otpCode(), "REGISTRATION")) {
            throw new IllegalArgumentException("Invalid or expired OTP. Please try again.");
        }

        return ResponseEntity.ok(authService.verifyEmail(request));
    }

    // ─── STEP 3: Login with password ────────────────────────────────────────────
    // Validates password, then sends a login OTP — session is NOT created yet.
    @PostMapping("/login")
    public ResponseEntity<AuthDtos.AuthResponse> login(
            @Valid @RequestBody AuthDtos.LoginRequest request,
            HttpServletRequest httpRequest) {

        if (!authService.isEmailVerified(request.email())) {
            throw new IllegalArgumentException("Email not verified. Please check your inbox and verify first.");
        }

        // Validate password — throws BadCredentialsException if wrong
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()));

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();

        // Password correct — now send OTP before completing the session
        otpService.generateAndSendOtp(request.email(), "LOGIN");

        return ResponseEntity.ok(
                new AuthDtos.AuthResponse(
                        user.getUser().getId(),
                        user.getUsername(),
                        user.getUser().getRole().name(),
                        "Password verified. OTP sent to your email — please enter it to complete login."));
    }

    // ─── STEP 4: Verify login OTP → creates session ─────────────────────────────
    @PostMapping("/verify-login-otp")
    public ResponseEntity<AuthDtos.AuthResponse> verifyLoginOtp(
            @Valid @RequestBody AuthDtos.LoginOtpRequest request,
            HttpServletRequest httpRequest) {

        if (!otpService.verifyOtp(request.email(), request.otpCode(), "LOGIN")) {
            throw new IllegalArgumentException("Invalid or expired OTP. Please try again.");
        }

        // FIX: Load user directly instead of re-authenticating with null password
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context);

        CustomUserDetails user = (CustomUserDetails) userDetails;

        return ResponseEntity.ok(
                new AuthDtos.AuthResponse(
                        user.getUser().getId(),
                        user.getUsername(),
                        user.getUser().getRole().name(),
                        "Login successful. Welcome back!"));
    }

    // ─── Resend OTP (registration) ───────────────────────────────────────────────
    @PostMapping("/resend-registration-otp")
    public ResponseEntity<String> resendRegistrationOtp(
            @Valid @RequestBody AuthDtos.ResendOtpRequest request) {

        if (authService.isEmailVerified(request.email())) {
            throw new IllegalArgumentException("Email is already verified.");
        }
        otpService.generateAndSendOtp(request.email(), "REGISTRATION");
        return ResponseEntity.ok("OTP resent to your email.");
    }

    // ─── Resend OTP (login) ──────────────────────────────────────────────────────
    @PostMapping("/resend-login-otp")
    public ResponseEntity<String> resendLoginOtp(
            @Valid @RequestBody AuthDtos.ResendOtpRequest request) {

        if (!authService.isEmailVerified(request.email())) {
            throw new IllegalArgumentException("Email not verified.");
        }
        otpService.generateAndSendOtp(request.email(), "LOGIN");
        return ResponseEntity.ok("OTP resent to your email.");
    }

    // ─── Current user info ───────────────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }

        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "userId",        user.getUser().getId(),
                "email",         user.getUsername(),
                "role",          user.getUser().getRole().name()));
    }
}