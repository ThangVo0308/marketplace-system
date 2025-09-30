package com.example.marketplace.services.impls;

import com.example.marketplace.entities.Booking;
import com.example.marketplace.entities.User;
import com.example.marketplace.enums.BookingStatus;
import com.example.marketplace.enums.UserStatus;
import com.example.marketplace.exceptions.AppException;
import com.example.marketplace.exceptions.CommonErrorCode;
import com.example.marketplace.exceptions.booking.BookingErrorCode;
import com.example.marketplace.exceptions.booking.BookingException;
import com.example.marketplace.repositories.BookingRepository;
import com.example.marketplace.repositories.UserRepository;
import com.example.marketplace.services.BookingService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.marketplace.components.Translator.getLocalizedMessage; // Giả định có Translator

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    BookingRepository bookingRepository;
    UserRepository userRepository;

    @Override
    public Booking findById(String id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        CommonErrorCode.OBJECT_NOT_FOUND,
                        HttpStatus.NOT_FOUND
                ));
    }

    @Override
    @Transactional
    public Booking create(Booking request) {
        User user = request.getUser();
        if (user.getUserStatus() == UserStatus.BANNED)
            throw new BookingException(BookingErrorCode.USER_BANNED, HttpStatus.UNPROCESSABLE_ENTITY);

        Booking booking = Booking.builder()
                .updatedAt(new Date())
                .status(BookingStatus.PENDING)
                .user(user)
                .build();

        Booking createBooking = bookingRepository.save(booking);
        createBooking.setCreatedAt(new Date());

//        Notification notification = Notification.builder()
//                .user(user)
//                .booking(createBooking)
//                .type(NotificationType.ORDER_STATUS_UPDATE)
//                .message(getLocalizedMessage("booking_confirmed"))
//                .build();
//
//        notificationRepository.save(notification);

        return bookingRepository.save(createBooking);

    }

    @Override
    public Page<Booking> findAll(int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return bookingRepository.findAll(pageable);
    }

    @Override
    public Page<Booking> findAllByUser(User user, int offset, int limit) {
        return bookingRepository.findAllByUser(user, PageRequest.of(offset, limit, Sort.by("createdAt").descending()));
    }

    @Override
    public Page<Booking> findAllByStatus(BookingStatus status, int offset, int limit) {
        Pageable pageable = PageRequest.of(offset, limit);
        return bookingRepository.findByStatus(status, pageable);
    }

    @Override
    public Booking updateStatus(String bookingId, BookingStatus status) {
        Booking booking = findById(bookingId);
        switch (status) {
            case CANCELED:
                if (booking.getStatus().equals(BookingStatus.PENDING))
                    booking.setStatus(BookingStatus.CANCELED);
                else
                    throw new BookingException(BookingErrorCode.CANCEL_FAILED, HttpStatus.UNPROCESSABLE_ENTITY);
                break;

            case RESCHEDULED, REFUND_REQUESTED:
                booking.setStatus(status);
                break;

            case ACCEPTED:
                booking.setStatus(BookingStatus.ACCEPTED);
                break;

            case PENDING:
                booking.setStatus(BookingStatus.PENDING);
                break;

            case REJECTED:
                booking.setStatus(BookingStatus.REJECTED);
                break;
        }
        return bookingRepository.save(booking);
    }

    @Override
    public void deleteBooking(String id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public List<String> getBookingStatus() {
        return Arrays.stream(BookingStatus.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}