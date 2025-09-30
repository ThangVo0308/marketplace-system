package com.example.marketplace.entities;

import com.example.marketplace.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "bookings")
public class Booking extends AbstractEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        String id;

        @Column(nullable = false, length = 20)
        @Enumerated(EnumType.STRING)
        BookingStatus status;

        @ManyToOne
        @JoinColumn(name = "user_id", referencedColumnName = "id",
                foreignKey = @ForeignKey(name = "fk_bookings_users",
                        foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE"),
                nullable = false, updatable = false)
        @JsonManagedReference
        User user;

        @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonBackReference
        Payment payment;

        @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonBackReference
        List<BookingItem> bookingItems;

        @Override
        public String toString() {
                return "Booking{" +
                        "id='" + id + '\'' +
                        ", status=" + status +
                        ", user=" + user +
                        ", payment=" + payment +
                        ", bookingItems=" + bookingItems +
                        ", createAt=" + getCreatedAt() +
                        '}';
        }
}
