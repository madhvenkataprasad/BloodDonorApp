package com.blooddonor.repository;

import com.blooddonor.entity.DonorDocument;
import com.blooddonor.entity.DonorProfile;
import com.blooddonor.model.DocumentType;
import com.blooddonor.model.VerificationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorDocumentRepository extends JpaRepository<DonorDocument, Long> {
    List<DonorDocument> findByDonor(DonorProfile donor);
    List<DonorDocument> findByStatus(VerificationStatus status);
    Optional<DonorDocument> findByDonorAndDocumentType(DonorProfile donor, DocumentType documentType);
}
