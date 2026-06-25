package com.blooddonor.service;

import com.blooddonor.entity.OtpVerification;
import com.blooddonor.repository.OtpVerificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;

@Service
public class OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;

    public OtpService(OtpVerificationRepository otpRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    @Transactional
    public String generateAndSendOtp(String email, String otpType) {
        // Delete any existing unverified OTPs for this email and type
        otpRepository.deleteByEmailAndOtpType(email, otpType);

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // Set expiration to 10 minutes from now
        Instant expiresAt = Instant.now().plusSeconds(600);

        // Save OTP
        OtpVerification otp = new OtpVerification();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(expiresAt);
        otp.setOtpType(otpType);
        otpRepository.save(otp);

        // Send email with OTP
        String subject = otpType.equals("REGISTRATION") ? "Verify your email - LifeLink Blood Donor" : "Login OTP - LifeLink Blood Donor";
        String message = otpType.equals("REGISTRATION") 
            ? "Your verification code is: " + otpCode + "\n\nThis code will expire in 10 minutes."
            : "Your login OTP is: " + otpCode + "\n\nThis code will expire in 10 minutes.";
        
        emailService.sendEmail(email, subject, message);

        return otpCode;
    }

    @Transactional
    public boolean verifyOtp(String email, String otpCode, String otpType) {
        OtpVerification otp = otpRepository
                .findByEmailAndOtpTypeAndVerifiedFalseAndExpiresAtAfter(email, otpType, Instant.now())
                .orElse(null);

        if (otp == null) {
            System.out.println("OTP not found for email: " + email + ", type: " + otpType);
            return false;
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            System.out.println("OTP code mismatch for email: " + email);
            return false;
        }

        otp.setVerified(true);
        otpRepository.save(otp);
        System.out.println("OTP verified successfully for email: " + email);
        return true;
    }
}
