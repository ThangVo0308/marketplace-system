package com.example.marketplace.dtos.responses.promotion;

import com.example.marketplace.enums.PromotionStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionResponse {
    String id;
    String name;
    String description;
    Double discountPercentage;
    Date startDate;
    Date endDate;
    PromotionStatus status;
    String createdBy;
}
