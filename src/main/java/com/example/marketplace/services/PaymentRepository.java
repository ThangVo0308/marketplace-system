package com.example.marketplace.services;

import com.example.marketplace.entities.Payment;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Observed
public interface PaymentRepository extends JpaRepository<Payment, String> {

}