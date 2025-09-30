package com.example.marketplace.mappers;

import com.example.marketplace.dtos.requests.booking.BookingRequest;
import com.example.marketplace.dtos.responses.booking.BookingResponse;
import com.example.marketplace.entities.Booking;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookingMapper {
        BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

        Booking toBooking(BookingRequest dto);

        BookingResponse toBookingResponse(Booking entity);

        @AfterMapping
        default void customizeDto(Booking entity, @MappingTarget BookingResponse dto) {
                dto.setMUser(UserMapper.INSTANCE.toUserResponse(entity.getUser()));
                dto.setCreatedAt(entity.getCreatedAt());
                if (entity.getBookingItems() != null) {
                        dto.setMBookingItems(entity.getBookingItems().stream()
                                .map(BookingItemMapper.INSTANCE::toBookingItemResponse)
                                .collect(Collectors.toList()));
                } else {
                        dto.setMBookingItems(Collections.emptyList());
                }
        }
}