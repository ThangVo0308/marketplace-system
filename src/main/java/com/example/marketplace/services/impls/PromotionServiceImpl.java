package com.example.marketplace.services.impls;

import com.example.marketplace.entities.Promotion;
import com.example.marketplace.enums.PromotionStatus;
import com.example.marketplace.exceptions.AppException;
import com.example.marketplace.exceptions.CommonErrorCode;
import com.example.marketplace.repositories.PromotionRepository;
import com.example.marketplace.services.PromotionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PromotionServiceImpl implements PromotionService {
    PromotionRepository promotionRepository;

    @Override
    public Promotion findById(String id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Promotion"));
    }

    @Override
    public Promotion create(Promotion promotion) {
        return promotionRepository.save(promotion);
    }

    @Override
    public Promotion update(String id, Promotion updatedPromotion) {
        Promotion existingPromotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Promotion"));

        if (updatedPromotion.getStartDate().after(updatedPromotion.getEndDate())) {
            throw new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.BAD_REQUEST, "Invalid promotion dates");
        }

        existingPromotion.setName(updatedPromotion.getName());
        existingPromotion.setDescription(updatedPromotion.getDescription());
        existingPromotion.setDiscountPercentage(updatedPromotion.getDiscountPercentage());
        existingPromotion.setStartDate(updatedPromotion.getStartDate());
        existingPromotion.setEndDate(updatedPromotion.getEndDate());
        existingPromotion.setStatus(updatedPromotion.getStatus());
        return promotionRepository.save(existingPromotion);
    }

    @Override
    public Page<Promotion> findAll(int offset, int limit) {
        return promotionRepository.findAll(PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public Page<Promotion> findAllByStatus(PromotionStatus status, int offset, int limit) {
        return promotionRepository.findByStatus(status, PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public void delete(String id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Promotion"));

        promotionRepository.delete(promotion);
    }
}