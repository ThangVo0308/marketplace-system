package com.example.marketplace.controllers;

import com.example.marketplace.dtos.requests.file.FileUploadRequest;
import com.example.marketplace.dtos.requests.product.ProductCreateRequest;
import com.example.marketplace.dtos.requests.product.ProductUpdateRequest;
import com.example.marketplace.dtos.requests.product.ProductQuantityUpdateRequest;
import com.example.marketplace.dtos.requests.product.ProductRatingUpdateRequest;
import com.example.marketplace.dtos.responses.other.CommonResponse;
import com.example.marketplace.dtos.responses.product.ProductResponse;
import com.example.marketplace.entities.Product;
import com.example.marketplace.entities.User;
import com.example.marketplace.mappers.ProductMapper;
import com.example.marketplace.services.MinioClientService;
import com.example.marketplace.services.ProductService;
import com.example.marketplace.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static com.example.marketplace.components.Translator.getLocalizedMessage;


@RestController
@RequestMapping("${api.prefix}/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Product APIs")
public class ProductController {

    ProductService productService;

    UserService userService;

    MinioClientService minioClientService;

    ProductMapper productMapper = ProductMapper.INSTANCE;

    @Operation(summary = "Create Product", description = "Create new product")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody @Valid ProductCreateRequest request) {
        User user = userService.findById(request.userId());
        Product product = productMapper.toProduct(request);
        product.setUser(user);

        Product createdProduct = productService.create(product);
        return ResponseEntity.ok(productMapper.toProductResponse(createdProduct));
    }

    @Operation(summary = "Get Product by ID", description = "Get product details by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> findById(@PathVariable String id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(productMapper.toProductResponse(product));
    }

    @Operation(summary = "Update Product", description = "Update product information", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable String id,
                                                  @RequestBody @Valid ProductUpdateRequest request) {
        Product updatedProduct = productMapper.toProduct(request);
        Product result = productService.update(id, updatedProduct);
        return ResponseEntity.ok(productMapper.toProductResponse(result));
    }

    @Operation(summary = "Get All Products", description = "Get all products with pagination")
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> findAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<Product> products = productService.findAll(offset, limit);
        Page<ProductResponse> productResponses = products.map(productMapper::toProductResponse);
        return ResponseEntity.ok(productResponses);
    }

    @Operation(summary = "Get Products by User", description = "Get all products of a specific user", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<ProductResponse>> findAllByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        User user = userService.findById(userId);
        Page<Product> products = productService.findAllByUser(user, offset, limit);
        Page<ProductResponse> productResponses = products.map(productMapper::toProductResponse);
        return ResponseEntity.ok(productResponses);
    }

    @Operation(summary = "Search Products", description = "Search products by name")
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<Product> products = productService.searchByName(name, offset, limit);
        Page<ProductResponse> productResponses = products.map(productMapper::toProductResponse);
        return ResponseEntity.ok(productResponses);
    }

    @Operation(summary = "Get Products by Category", description = "Get products filtered by category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> findByCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<Product> products = productService.findByCategory(categoryId, offset, limit);
        Page<ProductResponse> productResponses = products.map(productMapper::toProductResponse);
        return ResponseEntity.ok(productResponses);
    }

    @Operation(summary = "Get Products by Rating", description = "Get products with rating greater than specified value")
    @GetMapping("/rating/{rating}")
    public ResponseEntity<Page<ProductResponse>> findByRatingGreaterThan(
            @PathVariable Double rating,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<Product> products = productService.findByRatingGreaterThan(rating, offset, limit);
        Page<ProductResponse> productResponses = products.map(productMapper::toProductResponse);
        return ResponseEntity.ok(productResponses);
    }

    @Operation(summary = "Get Top Rated Products", description = "Get top rated products")
    @GetMapping("/top-rated")
    public ResponseEntity<List<ProductResponse>> findTopRatedProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.findTopRatedProducts(limit);
        List<ProductResponse> productResponses = products.stream()
                .map(productMapper::toProductResponse)
                .toList();
        return ResponseEntity.ok(productResponses);
    }

    @Operation(summary = "Get Recent Products", description = "Get recently created products")
    @GetMapping("/recent")
    public ResponseEntity<List<ProductResponse>> findRecentProducts(
            @RequestParam(defaultValue = "10") int limit) {
        List<Product> products = productService.findRecentProducts(limit);
        List<ProductResponse> productResponses = products.stream()
                .map(productMapper::toProductResponse)
                .toList();
        return ResponseEntity.ok(productResponses);
    }

    @Operation(summary = "Update Product Rating", description = "Update product rating", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/rating")
    public ResponseEntity<Void> updateRating(@PathVariable String id,
                                             @RequestBody @Valid ProductRatingUpdateRequest request) {
        productService.updateRating(id, request.rating());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update Product Quantity", description = "Update product quantity", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/quantity")
    public ResponseEntity<Void> updateQuantity(@PathVariable String id,
                                               @RequestBody @Valid ProductQuantityUpdateRequest request) {
        productService.updateQuantity(id, request.quantity());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete Product", description = "Delete product by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get Product Info", description = "Get basic product info for quick access")
    @GetMapping("/info/{id}")
    public ResponseEntity<Map<String, Object>> getProductInfo(@PathVariable String id) {
        Product product = productService.findById(id);
        return ResponseEntity.ok(
                Map.of(
                        "name", product.getName(),
                        "quantity", product.getQuantity(),
                        "rating", product.getRating() != null ? product.getRating() : 0.0,
                        "productInfo", productMapper.toProductResponse(product)
                )
        );
    }

    @Operation(summary = "Upload images", description = "Upload images", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/images")
    ResponseEntity<CommonResponse<Long, ?>> uploadAvatar(@RequestPart(name = "file") MultipartFile file,
                                                         @RequestPart(name = "request") @Valid FileUploadRequest request) {

        long uploadedSize = minioClientService.uploadChunk(
                file,
                request.fileMetadataId(),
                request.chunkHash(),
                request.startByte(),
                request.totalSize(),
                request.contentType(),
                request.ownerId(),
                request.fileMetadataType());

        HttpStatus httpStatus = uploadedSize == request.totalSize() ? CREATED : OK;

        String message = uploadedSize == request.totalSize() ? "file_upload_success" : "chunk_uploaded";

        return ResponseEntity.status(httpStatus).body(
                CommonResponse.<Long, Object>builder()
                        .message(getLocalizedMessage(message))
                        .results(uploadedSize)
                        .build()
        );
    }
}