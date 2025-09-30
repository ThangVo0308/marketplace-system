package com.example.marketplace.repositories;

import com.example.marketplace.entities.Product;
import com.example.marketplace.entities.User;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Repository
@Observed
public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findAllByUser(User user, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Product> findByRatingGreaterThan(Double rating, Pageable pageable);

    @Query("select p from Product p join p.categories c where c.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);

    @Query("select p from Product p order by p.rating desc")
    List<Product> findTopByOrderByRatingDesc(Pageable pageable);

    @Query("select p from Product p order by p.createdAt desc")
    List<Product> findTopByOrderByCreatedAtDesc(Pageable pageable);

}
