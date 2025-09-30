package com.example.marketplace.mappers;

import com.example.marketplace.dtos.responses.booking.PaymentResponse;
import com.example.marketplace.entities.Payment;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentMapper INSTANCE = Mappers.getMapper(PaymentMapper.class);

    PaymentResponse toPaymentResponse(Payment entity);
    @AfterMapping
    default void customizeDto(Payment entity, @MappingTarget PaymentResponse dto) {
        dto.setMBooking(BookingMapper.INSTANCE.toBookingResponse(entity.getBooking()));
    }

}