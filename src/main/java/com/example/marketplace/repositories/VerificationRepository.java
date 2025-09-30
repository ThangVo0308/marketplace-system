package com.example.marketplace.repositories;

import com.example.marketplace.entities.User;
import com.example.marketplace.entities.Verification;
import com.example.marketplace.enums.VerificationType;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Observed
public interface VerificationRepository extends JpaRepository<Verification, String> {

    Optional<Verification> findByCode(String code);

    List<Verification> findByUserAndVerificationType(User user, VerificationType type);

}
