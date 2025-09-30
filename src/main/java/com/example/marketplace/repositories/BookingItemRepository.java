package com.example.marketplace.repositories;

import com.example.marketplace.entities.Booking;
import com.example.marketplace.entities.BookingItem;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Observed
public interface BookingItemRepository extends JpaRepository<BookingItem, String> {

    List<BookingItem> findAllByBooking(Booking booking);
}
