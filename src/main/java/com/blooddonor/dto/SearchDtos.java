package com.blooddonor.dto;

import com.blooddonor.model.BloodGroup;
import jakarta.validation.constraints.*;

public final class SearchDtos {

    private SearchDtos() {}

    public record DonorSearchRequest(
            @NotNull BloodGroup bloodGroup,
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
            @NotNull @DecimalMin("0.5") @DecimalMax("500") Double radiusKm) {}

    public record DonorSearchResult(
            Long donorId,
            String fullName,
            int age,
            String bloodGroup,
            String diseases,
            String phone,
            String address,
            double latitude,
            double longitude,
            double distanceKm,
            boolean available) {}
}
