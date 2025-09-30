package com.example.marketplace.services;

import com.example.marketplace.entities.BookingItem;
import com.example.marketplace.enums.BookingItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookingItemsService {

    BookingItem findById(String id);

    BookingItem create(BookingItem item);

    Page<BookingItem> findAll(int offset, int limit);

    public BookingItem updateStatus(String bookingItemId, BookingItemStatus status);
}
