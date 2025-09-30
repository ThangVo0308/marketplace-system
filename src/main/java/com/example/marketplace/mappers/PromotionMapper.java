package com.example.marketplace.mappers;

import com.example.marketplace.dtos.requests.promotion.NewPromotionRequest;
import com.example.marketplace.dtos.responses.promotion.PromotionResponse;
import com.example.marketplace.entities.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    PromotionMapper INSTANCE = Mappers.getMapper(PromotionMapper.class);

    PromotionResponse toPromotionResponse(Promotion entity);

    Promotion toPromotion(NewPromotionRequest dto);
}