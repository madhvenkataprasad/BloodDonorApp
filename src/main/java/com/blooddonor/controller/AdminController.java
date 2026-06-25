package com.blooddonor.controller;

import com.blooddonor.dto.AdminDtos;
import com.blooddonor.dto.DonorDtos;
import com.blooddonor.model.VerificationStatus;
import com.blooddonor.service.AdminService;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminDtos.AdminStats> stats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    @GetMapping("/donors")
    public ResponseEntity<List<AdminDtos.AdminDonorSummary>> donors(
            @RequestParam(required = false) VerificationStatus status) {
        return ResponseEntity.ok(adminService.listDonors(status));
    }

    @GetMapping("/donors/{donorId}/documents")
    public ResponseEntity<List<DonorDtos.DocumentInfo>> donorDocuments(
            @PathVariable Long donorId) {
        return ResponseEntity.ok(adminService.listDonorDocuments(donorId));
    }

    @GetMapping("/documents/{documentId}/file")
    public ResponseEntity<Resource> documentFile(
            @PathVariable Long documentId) throws Exception {

        Resource resource = adminService.loadDocumentFile(documentId);

        String filename = resource.getFilename();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        if (filename != null) {
            String lower = filename.toLowerCase();

            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (lower.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (lower.endsWith(".pdf")) {
                mediaType = MediaType.APPLICATION_PDF;
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(mediaType)
                .body(resource);
    }

    @PutMapping("/donors/{donorId}/verify")
    public ResponseEntity<Void> verifyDonor(
            @PathVariable Long donorId,
            @Valid @RequestBody AdminDtos.VerifyDonorRequest request) {

        adminService.verifyDonor(donorId, request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/documents/{documentId}/verify")
    public ResponseEntity<Void> verifyDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody AdminDtos.VerifyDocumentRequest request) {

        adminService.verifyDocument(documentId, request);
        return ResponseEntity.ok().build();
    }
}