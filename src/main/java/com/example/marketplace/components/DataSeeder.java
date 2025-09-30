package com.example.marketplace.components;

import com.example.marketplace.exceptions.file.FileErrorCode;
import com.example.marketplace.exceptions.file.FileException;
import com.example.marketplace.helpers.Constants;
import com.example.marketplace.services.PaymentRepository;
import com.github.javafaker.Faker;
import com.example.marketplace.entities.*;
import com.example.marketplace.enums.*;
import com.example.marketplace.repositories.*;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.example.marketplace.helpers.Utils.*;

@Component
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DataSeeder {

    RoleRepository roleRepository;
    UserRepository userRepository;
    CategoryRepository categoryRepository;
    ProductRepository productRepository;
    BookingRepository bookingRepository;
    BookingItemRepository bookingItemRepository;
    PaymentRepository paymentRepository;
    PromotionRepository promotionRepository;
    FileMetadataRepository fileMetadataRepository;
    PasswordEncoder passwordEncoder;

    Faker faker = new Faker();

    @PostConstruct
    @Transactional
    public void seed() {
        seedRoles();
        seedUsers();
        seedCategories();
        seedProducts();
        seedBookings();
        seedBookingItems();
        seedPayments();
        seedPromotions();
        seedFileMetadata();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            Role user = Role.builder().name("USER").build();
            Role admin = Role.builder().name("ADMIN").build();
            Role seller = Role.builder().name("SELLER").build();
            roleRepository.saveAll(List.of(user, admin, seller));
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            List<User> users = new ArrayList<>();
            List<Role> roles = roleRepository.findAll();
            IntStream.range(0, 10).forEach(index -> {
                User user = User.builder()
                        .username(faker.name().username())
                        .email(faker.internet().emailAddress())
                        .password(passwordEncoder.encode("123456"))
                        .firstName(faker.name().firstName())
                        .lastName(faker.name().lastName())
                        .middleName(faker.name().firstName())
                        .mobileNumber(faker.phoneNumber().subscriberNumber(10))
                        .birthdate(LocalDate.now().minusYears(faker.number().numberBetween(18, 60)))
                        .gender(getRandomEnum(Gender.class))
                        .userStatus(getRandomEnum(UserStatus.class))
                        .role(roles.get(faker.number().numberBetween(0, roles.size())))
                        .isActivated(true)
                        .build();
                users.add(user);
            });
            userRepository.saveAll(users);
        }
    }

    private void seedCategories() {
        if (categoryRepository.count() == 0) {
            List<Category> categories = new ArrayList<>();
            String[] categoryNames = {"Electronics", "Clothing", "Home & Garden", "Sports", "Books", "Beauty", "Automotive", "Food"};
            Arrays.stream(categoryNames).forEach(name -> {
                Category category = Category.builder()
                        .name(name)
                        .build();
                categories.add(category);
            });
            categoryRepository.saveAll(categories);
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            List<User> users = userRepository.findAll();
            List<Category> categories = categoryRepository.findAll();

            List<Category> productCategories = new ArrayList<>(categories.stream()
                    .filter(category -> faker.bool().bool())
                    .toList());

            if (productCategories.isEmpty()) {
                productCategories.add(categories.get(faker.number().numberBetween(0, categories.size())));
            }

            List<Product> products = new ArrayList<>();
            IntStream.range(0, 50).forEach(index -> {
                Product product = Product.builder()
                        .name(faker.commerce().productName())
                        .description(faker.lorem().sentence(20))
                        .quantity(faker.number().numberBetween(1, 100))
                        .rating(faker.number().randomDouble(1, 1, 5))
                        .user(users.get(faker.number().numberBetween(0, users.size())))
                        .categories(new ArrayList<>(productCategories))
                        .build();
                products.add(product);
            });
            productRepository.saveAll(products);
        }
    }

    private void seedBookings() {
        if (bookingRepository.count() == 0) {
            List<User> users = userRepository.findAll();
            List<Booking> bookings = new ArrayList<>();
            IntStream.range(0, 20).forEach(index -> {
                Booking booking = Booking.builder()
                        .status(getRandomEnum(BookingStatus.class))
                        .user(users.get(faker.number().numberBetween(0, users.size())))
                        .build();
                bookings.add(booking);
            });
            bookingRepository.saveAll(bookings);
        }
    }

    private void seedBookingItems() {
        if (bookingItemRepository.count() == 0) {
            List<Booking> bookings = bookingRepository.findAll();
            List<Product> products = productRepository.findAll();
            List<BookingItem> bookingItems = new ArrayList<>();
            bookings.forEach(booking -> {
                int itemCount = faker.number().numberBetween(1, 5);
                IntStream.range(0, itemCount).forEach(index -> {
                    Date baseDate = faker.date().future(30, java.util.concurrent.TimeUnit.DAYS);
                    Date startTime = new Date(baseDate.getTime() + faker.number().numberBetween(8, 18) * 60 * 60 * 1000);
                    Date endTime = new Date(startTime.getTime() + faker.number().numberBetween(1, 4) * 60 * 60 * 1000);

                    BookingItem bookingItem = BookingItem.builder()
                            .availableDate(baseDate)
                            .startTime(startTime)
                            .endTime(endTime)
                            .price(faker.number().randomDouble(2, 10, 500))
                            .booking(booking)
                            .product(products.get(faker.number().numberBetween(0, products.size())))
                            .status(getRandomEnum(BookingItemStatus.class))
                            .build();
                    bookingItems.add(bookingItem);
                });
            });
            bookingItemRepository.saveAll(bookingItems);
        }
    }

    private void seedPayments() {
        if (paymentRepository.count() == 0) {
            List<Booking> bookings = bookingRepository.findAll();

            Map<Booking, List<BookingItem>> bookingItemsMap = bookingItemRepository.findAll()
                    .stream()
                    .collect(Collectors.groupingBy(BookingItem::getBooking));

            List<Payment> payments = new ArrayList<>();
            bookings.forEach(booking -> {
                List<BookingItem> bookingItems = bookingItemsMap.getOrDefault(booking, Collections.emptyList());
                Double totalPrice = bookingItems
                        .stream()
                        .mapToDouble(BookingItem::getPrice)
                        .sum();

                if (totalPrice > 0) {
                    Payment payment = Payment.builder()
                            .method(getRandomEnum(PaymentMethod.class))
                            .price(totalPrice)
                            .status(getRandomEnum(PaymentStatus.class))
                            .booking(booking)
                            .build();
                    payments.add(payment);
                }
            });
            paymentRepository.saveAll(payments);
        }
    }

    private void seedPromotions() {
        if (promotionRepository.count() == 0) {
            List<Product> products = productRepository.findAll();
            List<Promotion> promotions = new ArrayList<>();
            IntStream.range(0, 10).forEach(index -> {
                Date startDate = faker.date().past(10, java.util.concurrent.TimeUnit.DAYS);
                Date endDate = faker.date().future(30, java.util.concurrent.TimeUnit.DAYS);

                Promotion promotion = Promotion.builder()
                        .name(faker.commerce().promotionCode())
                        .description(faker.lorem().sentence(10))
                        .discountPercentage(faker.number().randomDouble(2, 5, 50))
                        .startDate(startDate)
                        .endDate(endDate)
                        .status(getRandomEnum(PromotionStatus.class))
                        .build();
                promotions.add(promotion);
            });
            promotionRepository.saveAll(promotions);
        }
    }

    private void seedFileMetadata() {
        if (fileMetadataRepository.count() == 0) {
            List<Product> products = productRepository.findAll();
            for (Product product : products) {
                int imageCount = faker.number().numberBetween(1, 5);
                for (int i = 0; i < imageCount; i++) {
                    File randomFile = getRandomFile(Constants.PRODUCT_FAKE_IMAGES_FOLDER);
                    String contentType = getContentType(randomFile);
                    long size = randomFile.length();
                    String fileName = generateFileName(contentType.split("/")[0], contentType.split("/")[1]);
                    FileMetadata fileMetadata = FileMetadata.builder()
                            .objectKey(fileName)
                            .size(size)
                            .contentType(contentType)
                            .product(product)
                            .createdBy(product.getUser().getId())
                            .createdAt(product.getCreatedAt())
                            .build();
                    fileMetadataRepository.save(fileMetadata);
                    // minioClientService.storeObject(randomFile, fileName, contentType, bucketName);
                }
            }

            // Seed avatars for Users (keep this part)
            List<User> users = userRepository.findAll();
            for (User user : users) {
                File randomFile = getRandomFile(Constants.USER_FAKE_AVATARS_FOLDER);
                String contentType = getContentType(randomFile);
                long size = randomFile.length();
                String fileName = generateFileName(contentType.split("/")[0], contentType.split("/")[1]);
                FileMetadata fileMetadata = FileMetadata.builder()
                        .objectKey(fileName)
                        .size(size)
                        .contentType(contentType)
                        .user(user)
                        .createdBy(user.getId())
                        .createdAt(user.getCreatedAt())
                        .build();
                fileMetadataRepository.save(fileMetadata);
                // minioClientService.storeObject(randomFile, fileName, contentType, bucketName);
            }

            // Uncomment if you want Category images
            // List<Category> categories = categoryRepository.findAll();
            // for (Category category : categories) {
            //     File randomFile = getRandomFile(Constants.CATEGORY_FAKE_IMAGES_FOLDER);
            //     String contentType = getContentType(randomFile);
            //     long size = randomFile.length();
            //     String fileName = generateFileName(contentType.split("/")[0], contentType.split("/")[1]);
            //     FileMetadata fileMetadata = FileMetadata.builder()
            //             .objectKey(fileName)
            //             .size(size)
            //             .contentType(contentType)
            //             .category(category)
            //             .createdBy(category.getCreatedBy())
            //             .createdAt(category.getCreatedAt())
            //             .build();
            //     fileMetadataRepository.save(fileMetadata);
            //     // minioClientService.storeObject(randomFile, fileName, contentType, bucketName);
            // }
        }
    }
    private String getContentType(File randomFile) {
        try {
            return Files.probeContentType(randomFile.toPath());
        } catch(IOException e) {
            throw new FileException(FileErrorCode.FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
        }
    }

    // Helper method to get random enum value
    private <T extends Enum<T>> T getRandomEnum(Class<T> clazz) {
        int x = new Random().nextInt(clazz.getEnumConstants().length);
        return clazz.getEnumConstants()[x];
    }
}