package com.blooddonor.service;

import com.blooddonor.dto.DonorDtos;
import com.blooddonor.entity.DonorDocument;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.UserAccount;
import com.blooddonor.model.DocumentType;
import com.blooddonor.model.VerificationStatus;
import com.blooddonor.repository.DonorDocumentRepository;
import com.blooddonor.repository.DonorProfileRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DonorService {

    private final DonorProfileRepository donorRepository;
    private final DonorDocumentRepository documentRepository;
    private final FileStorageService fileStorage;

    public DonorService(
            DonorProfileRepository donorRepository,
            DonorDocumentRepository documentRepository,
            FileStorageService fileStorage) {
        this.donorRepository = donorRepository;
        this.documentRepository = documentRepository;
        this.fileStorage = fileStorage;
    }

    @Transactional
    public DonorDtos.DonorProfileResponse saveProfile(UserAccount user, DonorDtos.DonorProfileRequest request) {
        if (request.age() < 18) {
            throw new IllegalArgumentException("Donor must be at least 18 years old");
        }
        DonorProfile profile = donorRepository.findByUser(user).orElse(new DonorProfile());
        boolean isNew = profile.getId() == null;
        profile.setUser(user);
        profile.setFullName(request.fullName().trim());
        profile.setAge(request.age());
        profile.setBloodGroup(request.bloodGroup());
        profile.setDiseases(request.diseases() != null ? request.diseases().trim() : "None");
        profile.setPhone(request.phone().trim());
        profile.setAddress(request.address().trim());
        profile.setLatitude(request.latitude());
        profile.setLongitude(request.longitude());
        profile.setAvailable(request.available());
        if (isNew) {
            profile.setVerificationStatus(VerificationStatus.PENDING);
        } else if (profile.getVerificationStatus() == VerificationStatus.APPROVED) {
            profile.setVerificationStatus(VerificationStatus.PENDING);
        }
        donorRepository.save(profile);
        return toResponse(profile);
    }

    @Transactional(readOnly = true)
    public DonorDtos.DonorProfileResponse getProfile(UserAccount user) {
        DonorProfile profile = donorRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Donor profile not found. Please complete your profile."));
        return toResponse(profile);
    }

    @Transactional
    public DonorDtos.DocumentInfo uploadDocument(UserAccount user, DocumentType type, MultipartFile file)
            throws IOException {
        DonorProfile profile = donorRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Create your profile before uploading documents"));
        String stored = fileStorage.store(file, "donor-" + profile.getId());
        DonorDocument doc = documentRepository.findByDonorAndDocumentType(profile, type).orElse(new DonorDocument());
        doc.setDonor(profile);
        doc.setDocumentType(type);
        doc.setStoredFileName(stored);
        doc.setOriginalFileName(file.getOriginalFilename());
        doc.setStatus(VerificationStatus.PENDING);
        doc.setRejectionReason(null);
        documentRepository.save(doc);
        if (profile.getVerificationStatus() == VerificationStatus.APPROVED) {
            profile.setVerificationStatus(VerificationStatus.PENDING);
            donorRepository.save(profile);
        }
        return toDocInfo(doc);
    }

    private DonorDtos.DonorProfileResponse toResponse(DonorProfile profile) {
        List<DonorDtos.DocumentInfo> docs = documentRepository.findByDonor(profile).stream()
                .map(this::toDocInfo)
                .toList();
        return new DonorDtos.DonorProfileResponse(
                profile.getId(),
                profile.getFullName(),
                profile.getAge(),
                profile.getBloodGroup().name(),
                profile.getDiseases(),
                profile.getPhone(),
                profile.getAddress(),
                profile.getLatitude(),
                profile.getLongitude(),
                profile.isAvailable(),
                profile.getVerificationStatus().name(),
                profile.getAdminNotes(),
                docs);
    }

    private DonorDtos.DocumentInfo toDocInfo(DonorDocument doc) {
    return new DonorDtos.DocumentInfo(
            doc.getId(),
            doc.getDocumentType(),
            doc.getOriginalFileName(),
            doc.getStatus(),
            doc.getRejectionReason(),
            doc.getStoredFileName()
    );
}
}
