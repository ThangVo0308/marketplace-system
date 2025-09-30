package com.example.marketplace.mappers;

import com.example.marketplace.dtos.requests.booking.BookingItemRequest;
import com.example.marketplace.dtos.responses.booking.BookingItemResponse;
import com.example.marketplace.entities.BookingItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingItemMapper {

        BookingItemMapper INSTANCE = Mappers.getMapper(BookingItemMapper.class);

        BookingItemResponse toBookingItemResponse(BookingItem entity);

        BookingItem toBookingItem(BookingItemRequest request);

        @AfterMapping
        default void customizeDto(BookingItem entity, @MappingTarget BookingItemResponse dto) {
                dto.setStatus(entity.getStatus());
                dto.setCreatedAt(entity.getCreatedAt());
        }
}
