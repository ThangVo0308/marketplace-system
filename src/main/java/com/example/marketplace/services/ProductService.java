package com.example.marketplace.services;

import com.example.marketplace.entities.Product;
import com.example.marketplace.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {

    Product findById(String id);

    Product create(Product product);

    Product update(String id, Product updatedProduct);

    Page<Product> findAll(int offset, int limit);

    Page<Product> findAllByUser(User user, int offset, int limit);

    Page<Product> searchByName(String name, int offset, int limit);

    Page<Product> findByCategory(Integer categoryId, int offset, int limit);

    Page<Product> findByRatingGreaterThan(Double rating, int offset, int limit);

    List<Product> findTopRatedProducts(int limit);

    List<Product> findRecentProducts(int limit);

    void delete(String id);

    void updateRating(String productId, Double rating);

    void updateQuantity(String productId, Integer quantity);
}