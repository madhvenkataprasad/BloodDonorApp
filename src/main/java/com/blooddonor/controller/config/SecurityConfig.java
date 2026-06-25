package com.blooddonor.config;

import com.blooddonor.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

            .authorizeHttpRequests(auth -> auth

                // Static frontend files
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/css/**",
                        "/js/**",
                        "/pages/**")
                .permitAll()

                // All auth endpoints are public (registration, login, OTP steps)
                .requestMatchers(
                        HttpMethod.POST,
                        "/api/auth/register",
                        "/api/auth/verify-email",
                        "/api/auth/login",
                        "/api/auth/verify-login-otp",
                        "/api/auth/resend-registration-otp",
                        "/api/auth/resend-login-otp")
                .permitAll()

                .requestMatchers(HttpMethod.GET, "/api/auth/me")
                .permitAll()

                // Admin only
                .requestMatchers("/api/admin/**")
                .hasRole("ADMIN")

                // Any logged-in user can manage their donor profile OR search for donors
                // Role is always USER (set at registration) — same person can do both
                .requestMatchers("/api/donor/**")
                .hasAnyRole("USER", "ADMIN")

                .requestMatchers("/api/search/**")
                .hasAnyRole("USER", "ADMIN")

                .anyRequest()
                .authenticated())

            .httpBasic(basic -> {})
            .formLogin(form -> form.disable())

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID"));

        return http.build();
    }
}