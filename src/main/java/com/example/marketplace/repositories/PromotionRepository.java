package com.example.marketplace.repositories;

import com.example.marketplace.entities.Promotion;
import com.example.marketplace.enums.PromotionStatus;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Observed
public interface PromotionRepository extends JpaRepository<Promotion, String> {
    Page<Promotion> findByStatus(PromotionStatus status, Pageable pageable);
}