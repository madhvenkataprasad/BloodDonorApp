package com.blooddonor.service;

import com.blooddonor.dto.AdminDtos;
import com.blooddonor.dto.DonorDtos;
import com.blooddonor.entity.DonorDocument;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.model.Role;
import com.blooddonor.model.VerificationStatus;
import com.blooddonor.repository.DonorDocumentRepository;
import com.blooddonor.repository.DonorProfileRepository;
import com.blooddonor.repository.UserAccountRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class AdminService {

    private final UserAccountRepository userRepository;
    private final DonorProfileRepository donorRepository;
    private final DonorDocumentRepository documentRepository;

    public AdminService(UserAccountRepository userRepository,
                       DonorProfileRepository donorRepository,
                       DonorDocumentRepository documentRepository) {
        this.userRepository = userRepository;
        this.donorRepository = donorRepository;
        this.documentRepository = documentRepository;
    }

    @Transactional(readOnly = true)
    public AdminDtos.AdminStats getStats() {
        return new AdminDtos.AdminStats(
                userRepository.count(),
                userRepository.countByRole(Role.USER),
                0L, // No separate receiver count anymore
                donorRepository.findByVerificationStatus(VerificationStatus.PENDING).size(),
                documentRepository.findByStatus(VerificationStatus.PENDING).size());
    }

    @Transactional(readOnly = true)
    public List<AdminDtos.AdminDonorSummary> listDonors(VerificationStatus status) {
        List<DonorProfile> profiles = status != null 
                ? donorRepository.findByVerificationStatus(status)
                : donorRepository.findAll();
        
        return profiles.stream().map(p -> new AdminDtos.AdminDonorSummary(
                p.getId(),
                p.getUser().getId(),
                p.getUser().getEmail(),
                p.getFullName(),
                p.getAge(),
                p.getBloodGroup().name(),
                p.getVerificationStatus().name(),
                p.getAddress(),
                p.getPhone(),
                documentRepository.findByDonor(p).size()
        )).toList();
    }

    @Transactional(readOnly = true)
    public List<DonorDtos.DocumentInfo> listDonorDocuments(Long donorId) {
        DonorProfile profile = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));
        
        return documentRepository.findByDonor(profile).stream().map(doc -> 
                new DonorDtos.DocumentInfo(
                        doc.getId(),
                        doc.getDocumentType(),
                        doc.getOriginalFileName(),
                        doc.getStatus(),
                        doc.getRejectionReason(),
                        doc.getStoredFileName()
                )
        ).toList();
    }

    @Transactional(readOnly = true)
    public Resource loadDocumentFile(Long documentId) throws MalformedURLException {
        DonorDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        // Since files are stored on Cloudinary, return the URL as a resource
        return new UrlResource(doc.getStoredFileName());
    }

    @Transactional
    public void verifyDonor(Long donorId, AdminDtos.VerifyDonorRequest request) {
        DonorProfile profile = donorRepository.findById(donorId)
                .orElseThrow(() -> new IllegalArgumentException("Donor not found"));
        
        profile.setVerificationStatus(request.status());
        profile.setAdminNotes(request.adminNotes());
        donorRepository.save(profile);
    }

    @Transactional
    public void verifyDocument(Long documentId, AdminDtos.VerifyDocumentRequest request) {
        DonorDocument doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        doc.setStatus(request.status());
        doc.setRejectionReason(request.rejectionReason());
        documentRepository.save(doc);
        
        // If document is rejected, set donor profile back to pending
        if (request.status() == VerificationStatus.REJECTED) {
            DonorProfile profile = doc.getDonor();
            if (profile.getVerificationStatus() == VerificationStatus.APPROVED) {
                profile.setVerificationStatus(VerificationStatus.PENDING);
                donorRepository.save(profile);
            }
        }
    }
}
