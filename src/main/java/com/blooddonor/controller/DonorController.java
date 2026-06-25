package com.blooddonor.controller;

import com.blooddonor.dto.DonorDtos;
import com.blooddonor.model.DocumentType;
import com.blooddonor.security.CustomUserDetails;
import com.blooddonor.service.DonorService;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/donor")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @GetMapping("/profile")
    public ResponseEntity<DonorDtos.DonorProfileResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(donorService.getProfile(user.getUser()));
    }

    @PostMapping("/profile")
    public ResponseEntity<DonorDtos.DonorProfileResponse> saveProfile(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody DonorDtos.DonorProfileRequest request) {
        return ResponseEntity.ok(donorService.saveProfile(user.getUser(), request));
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DonorDtos.DocumentInfo> uploadDocument(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam DocumentType documentType,
            @RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(donorService.uploadDocument(user.getUser(), documentType, file));
    }
}
