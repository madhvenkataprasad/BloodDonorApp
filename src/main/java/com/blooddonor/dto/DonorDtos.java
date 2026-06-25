package com.blooddonor.dto;

import com.blooddonor.model.BloodGroup;
import com.blooddonor.model.DocumentType;
import com.blooddonor.model.VerificationStatus;

import jakarta.validation.constraints.*;

public final class DonorDtos {

    private DonorDtos() {}

    public record DonorProfileRequest(

            @NotBlank
            @Size(max = 100)
            String fullName,

            @Min(18)
            @Max(70)
            int age,

            @NotNull
            BloodGroup bloodGroup,

            @Size(max = 500)
            String diseases,

            @NotBlank
            @Pattern(
                    regexp = "^[0-9]{10}$",
                    message = "Phone number must contain exactly 10 digits"
            )
            String phone,

            @NotBlank
            @Size(max = 300)
            String address,

            @NotNull
            @DecimalMin("-90")
            @DecimalMax("90")
            Double latitude,

            @NotNull
            @DecimalMin("-180")
            @DecimalMax("180")
            Double longitude,

            boolean available

    ) {}


    public record DocumentInfo(
        Long id,
        DocumentType documentType,
        String originalFileName,
        VerificationStatus status,
        String rejectionReason,
        String fileUrl
) {}

    

    public record DonorProfileResponse(
            Long id,
            String fullName,
            int age,
            String bloodGroup,
            String diseases,
            String phone,
            String address,
            double latitude,
            double longitude,
            boolean available,
            String verificationStatus,
            String adminNotes,
            java.util.List<DocumentInfo> documents
    ) {}
}