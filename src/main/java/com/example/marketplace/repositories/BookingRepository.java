package com.example.marketplace.repositories;

import com.example.marketplace.entities.Booking;
import com.example.marketplace.entities.User;
import com.example.marketplace.enums.BookingStatus;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Observed
public interface BookingRepository extends JpaRepository<Booking, String> {

        Page<Booking> findByUser(User user, Pageable pageable);

        Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

        Page<Booking> findAllByUser(User user, Pageable pageable);
}