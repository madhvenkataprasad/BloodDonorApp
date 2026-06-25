package com.blooddonor.repository;

import com.blooddonor.entity.DonorProfile;
import com.blooddonor.entity.UserAccount;
import com.blooddonor.model.BloodGroup;
import com.blooddonor.model.VerificationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DonorProfileRepository extends JpaRepository<DonorProfile, Long> {
    Optional<DonorProfile> findByUser(UserAccount user);
    Optional<DonorProfile> findByUserId(Long userId);
    List<DonorProfile> findByVerificationStatus(VerificationStatus status);

    @Query("""
            SELECT d FROM DonorProfile d
            WHERE d.bloodGroup = :bloodGroup
              AND d.age >= 18
              AND d.available = true
              AND d.verificationStatus = com.blooddonor.model.VerificationStatus.APPROVED
            """)
    List<DonorProfile> findEligibleDonors(@Param("bloodGroup") BloodGroup bloodGroup);
}
