package com.example.marketplace.controllers;

import com.example.marketplace.dtos.requests.booking.BookingRequest;
import com.example.marketplace.dtos.requests.booking.BookingStatusRequest;
import com.example.marketplace.dtos.responses.booking.BookingResponse;
import com.example.marketplace.entities.Booking;
import com.example.marketplace.entities.User;
import com.example.marketplace.enums.BookingStatus;
import com.example.marketplace.mappers.BookingMapper;
import com.example.marketplace.services.BookingService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Booking APIs")
public class BookingController {

    BookingService bookingService;

    UserService userService;

    BookingMapper bookingMapper = BookingMapper.INSTANCE;

    @Operation(summary = "Get booking by ID", description = "Get booking by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> findById(@PathVariable String id) {
        Booking booking = bookingService.findById(id);
        return ResponseEntity.ok(bookingMapper.toBookingResponse(booking));
    }

    @Operation(summary = "Create booking", description = "Create new booking", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping
    public ResponseEntity<BookingResponse> create(@RequestBody @Valid BookingRequest request) {
        Booking booking = bookingMapper.toBooking(request);
        Booking createdBooking = bookingService.create(booking);
        return ResponseEntity.ok(bookingMapper.toBookingResponse(createdBooking));
    }

    @Operation(summary = "Get all bookings", description = "Get all bookings with pagination", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<Page<BookingResponse>> findAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<Booking> bookings = bookingService.findAll(offset, limit);
        Page<BookingResponse> bookingResponses = bookings.map(bookingMapper::toBookingResponse);
        return ResponseEntity.ok(bookingResponses);
    }

    @Operation(summary = "Get bookings by user", description = "Get bookings by user with pagination", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BookingResponse>> findAllByUser(@PathVariable String userId,
                                                               @RequestParam(defaultValue = "0") int offset,
                                                               @RequestParam(defaultValue = "10") int limit) {
        User user = userService.findById(userId);
        Page<Booking> bookings = bookingService.findAllByUser(user, offset, limit);
        Page<BookingResponse> bookingResponses = bookings.map(bookingMapper::toBookingResponse);
        return ResponseEntity.ok(bookingResponses);
    }

    @Operation(summary = "Get bookings by status", description = "Get bookings by status with pagination", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<BookingResponse>> findAllByStatus(@PathVariable BookingStatus status,
                                                                 @RequestParam(defaultValue = "0") int offset,
                                                                 @RequestParam(defaultValue = "10") int limit) {
        Page<Booking> bookings = bookingService.findAllByStatus(status, offset, limit);
        Page<BookingResponse> bookingResponses = bookings.map(bookingMapper::toBookingResponse);
        return ResponseEntity.ok(bookingResponses);
    }

    @Operation(summary = "Update booking status", description = "Update booking status", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateStatus(@PathVariable String id,
                                                        @RequestBody @Valid BookingStatusRequest request) {
        Booking updatedBooking = bookingService.updateStatus(id, request.status());
        return ResponseEntity.ok(bookingMapper.toBookingResponse(updatedBooking));
    }

    @Operation(summary = "Delete booking", description = "Delete booking by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable String id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get booking status list", description = "Get all available booking status")
    @GetMapping("/status")
    public ResponseEntity<List<String>> getBookingStatus() {
        List<String> statuses = bookingService.getBookingStatus();
        return ResponseEntity.ok(statuses);
    }
}