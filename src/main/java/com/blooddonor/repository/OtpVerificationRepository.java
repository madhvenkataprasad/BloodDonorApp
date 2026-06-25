package com.blooddonor.repository;

import com.blooddonor.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmailAndOtpTypeAndVerifiedFalseAndExpiresAtAfter(
            String email, String otpType, Instant now);
    
    void deleteByEmailAndOtpType(String email, String otpType);
}
