package com.example.marketplace.services.impls;

import com.example.marketplace.entities.Booking;
import com.example.marketplace.entities.BookingItem;
import com.example.marketplace.entities.User;
import com.example.marketplace.enums.BookingItemStatus;
import com.example.marketplace.enums.BookingStatus;
import com.example.marketplace.exceptions.AppException;
import com.example.marketplace.exceptions.CommonErrorCode;
import com.example.marketplace.repositories.BookingItemRepository;
import com.example.marketplace.services.BookingItemsService;
import com.example.marketplace.services.BookingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BookingItemServiceImpl implements BookingItemsService {
    BookingItemRepository bookingItemRepository;

    @Override
    public BookingItem findById(String id) {
        return bookingItemRepository.findById(id)
                .orElseThrow(() -> new AppException(CommonErrorCode.OBJECT_NOT_FOUND, HttpStatus.NOT_FOUND, "Booking Items"));
    }

    @Override
    public BookingItem create(BookingItem item) {
        BookingItem createBookingItem = bookingItemRepository.save(item);
        createBookingItem.setCreatedAt(new Date());
        return bookingItemRepository.save(createBookingItem);
    }


    @Override
    public Page<BookingItem> findAll(int offset, int limit) {
        return bookingItemRepository.findAll(PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public BookingItem updateStatus(String bookingItemId, BookingItemStatus status) {
        BookingItem bookingItem = bookingItemRepository.findById(bookingItemId).orElseThrow(() ->
                new IllegalArgumentException("Booking item not found: "+bookingItemId));

        bookingItem.setStatus(status);
        return bookingItemRepository.save(bookingItem);
    }
}
