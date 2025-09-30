// File: com/example/marketplace/dtos/responses/category/CategoryResponse.java
package com.example.marketplace.dtos.responses.category;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    Integer id;

    String name;

}