package com.blooddonor.repository;

import com.blooddonor.entity.UserAccount;
import com.blooddonor.model.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    Optional<UserAccount> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
}
