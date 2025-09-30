package com.example.marketplace.controllers;

import com.example.marketplace.dtos.requests.booking.BookingItemRequest;
import com.example.marketplace.dtos.requests.booking.BookingItemStatusRequest;
import com.example.marketplace.dtos.responses.booking.BookingItemResponse;
import com.example.marketplace.entities.BookingItem;
import com.example.marketplace.enums.BookingItemStatus;
import com.example.marketplace.mappers.BookingItemMapper;
import com.example.marketplace.services.BookingItemsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/booking-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Booking Item APIs")
public class BookingItemController {

    BookingItemsService bookingItemsService;

    BookingItemMapper bookingItemMapper = BookingItemMapper.INSTANCE;

    @Operation(summary = "Get booking item by ID", description = "Get booking item by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<BookingItemResponse> findById(@PathVariable String id) {
        BookingItem bookingItem = bookingItemsService.findById(id);
        return ResponseEntity.ok(bookingItemMapper.toBookingItemResponse(bookingItem));
    }

    @Operation(summary = "Create booking item", description = "Create new booking item", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<BookingItemResponse> create(@RequestBody @Valid BookingItemRequest request) {
        BookingItem bookingItem = bookingItemMapper.toBookingItem(request);
        BookingItem createdBookingItem = bookingItemsService.create(bookingItem);
        return ResponseEntity.ok(bookingItemMapper.toBookingItemResponse(createdBookingItem));
    }

    @Operation(summary = "Get all booking items", description = "Get all booking items with pagination", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<Page<BookingItemResponse>> findAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<BookingItem> bookingItems = bookingItemsService.findAll(offset, limit);
        Page<BookingItemResponse> bookingItemResponses = bookingItems.map(bookingItemMapper::toBookingItemResponse);
        return ResponseEntity.ok(bookingItemResponses);
    }

    @Operation(summary = "Update booking item status", description = "Update booking item status", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/status")
    public ResponseEntity<BookingItemResponse> updateStatus(@PathVariable String id,
                                                            @RequestBody @Valid BookingItemStatusRequest request) {
        BookingItem updatedBookingItem = bookingItemsService.updateStatus(id, BookingItemStatus.valueOf(request.status().toString().toUpperCase()));
        return ResponseEntity.ok(bookingItemMapper.toBookingItemResponse(updatedBookingItem));
    }
}