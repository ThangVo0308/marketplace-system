package com.example.marketplace.services;

import com.example.marketplace.entities.Promotion;
import com.example.marketplace.enums.PromotionStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface PromotionService {
    Promotion findById(String id);
    Promotion create(Promotion promotion);
    Promotion update(String id, Promotion promotion);
    Page<Promotion> findAll(int offset, int limit);
    Page<Promotion> findAllByStatus(PromotionStatus status, int offset, int limit);
    void delete(String id);
}