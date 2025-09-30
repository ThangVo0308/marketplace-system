package com.example.marketplace.services.impls;

import com.example.marketplace.entities.Product;
import com.example.marketplace.entities.User;
import com.example.marketplace.exceptions.AppException;
import com.example.marketplace.exceptions.CommonErrorCode;
import com.example.marketplace.repositories.ProductRepository;
import com.example.marketplace.services.ProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ProductServiceImpl implements ProductService {
    ProductRepository productRepository;

    @Override
    public Product findById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Product"));
    }

    @Override
    public Product create(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product update(String id, Product updatedProduct) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Product"));

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setQuantity(updatedProduct.getQuantity());
        existingProduct.setRating(updatedProduct.getRating());
        return productRepository.save(existingProduct);
    }

    @Override
    public Page<Product> findAll(int offset, int limit) {
        return productRepository.findAll(PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public Page<Product> findAllByUser(User user, int offset, int limit) {
        return productRepository.findAllByUser(user, PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public Page<Product> searchByName(String name, int offset, int limit) {
        return productRepository.findByNameContainingIgnoreCase(name,
                PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public Page<Product> findByCategory(Integer categoryId, int offset, int limit) {
        return productRepository.findByCategoryId(categoryId,
                PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public Page<Product> findByRatingGreaterThan(Double rating, int offset, int limit) {
        return productRepository.findByRatingGreaterThan(rating,
                PageRequest.of(offset, limit, Sort.by("rating").descending()));
    }

    @Override
    public List<Product> findTopRatedProducts(int limit) {
        return productRepository.findTopByOrderByRatingDesc(PageRequest.of(0, limit));
    }

    @Override
    public List<Product> findRecentProducts(int limit) {
        return productRepository.findTopByOrderByCreatedAtDesc(PageRequest.of(0, limit));
    }

    @Override
    public void delete(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Product"));
        productRepository.delete(product);
    }

    @Override
    public void updateRating(String productId, Double rating) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Product"));
        product.setRating(rating);
        productRepository.save(product);
    }

    @Override
    public void updateQuantity(String productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Product"));
        product.setQuantity(quantity);
        productRepository.save(product);
    }
}