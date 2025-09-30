package com.example.marketplace.services;

import com.example.marketplace.entities.Booking;
import com.example.marketplace.entities.User;
import com.example.marketplace.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookingService {
    Booking findById(String id);

    Booking create(Booking request);

    Page<Booking> findAll(int offset, int limit);

    Page<Booking> findAllByUser(User user, int offset, int limit);

    Page<Booking> findAllByStatus(BookingStatus status, int offset, int limit);

    Booking updateStatus(String bookingId, BookingStatus status);

    void deleteBooking(String id);
    List<String> getBookingStatus();

}
