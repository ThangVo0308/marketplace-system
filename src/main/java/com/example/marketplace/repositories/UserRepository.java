package com.example.marketplace.repositories;

import com.example.marketplace.entities.User;
import com.example.marketplace.enums.UserStatus;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Observed
public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Page<User> findAllById(String id, Pageable pageable);

    @Query("""
             SELECT u
             FROM User u
             WHERE (:keyword IS NULL OR
                    CAST(u.id AS string) LIKE CONCAT('%', :keyword, '%') OR
                    u.username LIKE CONCAT('%', :keyword, '%') OR
                    u.email LIKE CONCAT('%', :keyword, '%') OR
                    u.mobileNumber LIKE CONCAT('%', :keyword, '%') OR
                    u.firstName LIKE CONCAT('%', :keyword, '%'))
               AND (:status IS NULL OR u.userStatus = :status)
            """)
    Page<User> searchUsers(@Param("keyword") String keyword, @Param("status") UserStatus status, Pageable pageable);
}
