package com.example.marketplace.controllers;

import com.example.marketplace.dtos.requests.promotion.NewPromotionRequest;
import com.example.marketplace.dtos.responses.promotion.PromotionResponse;
import com.example.marketplace.entities.Promotion;
import com.example.marketplace.enums.PromotionStatus;
import com.example.marketplace.mappers.PromotionMapper;
import com.example.marketplace.services.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/promotions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Promotion APIs")
public class PromotionController {

    PromotionService promotionService;

    PromotionMapper promotionMapper = PromotionMapper.INSTANCE;

    @Operation(summary = "Get promotion by ID", description = "Get promotion by ID")
    @GetMapping("/{id}")
    public ResponseEntity<PromotionResponse> findById(@PathVariable String id) {
        Promotion promotion = promotionService.findById(id);
        return ResponseEntity.ok(promotionMapper.toPromotionResponse(promotion));
    }

    @Operation(summary = "Create promotion", description = "Create new promotion", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<PromotionResponse> create(@RequestBody @Valid NewPromotionRequest request) {
        Promotion promotion = promotionMapper.toPromotion(request);
        Promotion createdPromotion = promotionService.create(promotion);
        return ResponseEntity.ok(promotionMapper.toPromotionResponse(createdPromotion));
    }

    @Operation(summary = "Update promotion", description = "Update promotion", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<PromotionResponse> update(@PathVariable String id,
                                                    @RequestBody @Valid NewPromotionRequest request) {
        Promotion promotion = promotionMapper.toPromotion(request);
        Promotion updatedPromotion = promotionService.update(id, promotion);
        return ResponseEntity.ok(promotionMapper.toPromotionResponse(updatedPromotion));
    }

    @Operation(summary = "Get all promotions", description = "Get all promotions with pagination")
    @GetMapping
    public ResponseEntity<Page<PromotionResponse>> findAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<Promotion> promotions = promotionService.findAll(offset, limit);
        Page<PromotionResponse> promotionResponses = promotions.map(promotionMapper::toPromotionResponse);
        return ResponseEntity.ok(promotionResponses);
    }

    @Operation(summary = "Get promotions by status", description = "Get promotions by status with pagination")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<PromotionResponse>> findAllByStatus(@PathVariable PromotionStatus status,
                                                                   @RequestParam(defaultValue = "0") int offset,
                                                                   @RequestParam(defaultValue = "10") int limit) {
        Page<Promotion> promotions = promotionService.findAllByStatus(status, offset, limit);
        Page<PromotionResponse> promotionResponses = promotions.map(promotionMapper::toPromotionResponse);
        return ResponseEntity.ok(promotionResponses);
    }

    @Operation(summary = "Delete promotion", description = "Delete promotion by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        promotionService.delete(id);
        return ResponseEntity.ok().build();
    }
}