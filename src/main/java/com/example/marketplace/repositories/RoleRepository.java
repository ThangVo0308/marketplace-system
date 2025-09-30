package com.example.marketplace.repositories;

import com.example.marketplace.entities.Role;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Observed
public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
}
