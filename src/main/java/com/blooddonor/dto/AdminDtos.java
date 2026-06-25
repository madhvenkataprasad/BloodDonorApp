package com.blooddonor.dto;

import com.blooddonor.model.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AdminDtos {

    private AdminDtos() {}

    public record VerifyDonorRequest(
            @NotNull VerificationStatus status,
            @Size(max = 500) String adminNotes) {}

    public record VerifyDocumentRequest(
            @NotNull VerificationStatus status,
            @Size(max = 300) String rejectionReason) {}

    public record AdminStats(
            long totalUsers,
            long totalDonors,
            long totalReceivers,
            long pendingDonors,
            long pendingDocuments) {}

    public record AdminDonorSummary(
            Long donorId,
            Long userId,
            String email,
            String fullName,
            int age,
            String bloodGroup,
            String verificationStatus,
            String address,
            String phone,
            int documentCount) {}
}
