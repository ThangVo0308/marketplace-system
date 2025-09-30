package com.example.marketplace.entities;

import com.example.marketplace.enums.PromotionStatus;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "promotions")
public class Promotion extends AbstractEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.UUID)
        String id;

        @Column(nullable = false, length = 100)
        String name;

        @Column
        String description;

        @Column(name = "discount_percentage", nullable = false)
        Double discountPercentage;

        @Temporal(TemporalType.DATE)
        @Column(name = "start_date", nullable = false)
        Date startDate;

        @Temporal(TemporalType.DATE)
        @Column(name = "end_date", nullable = false)
        Date endDate;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false)
        PromotionStatus status;

        @ManyToMany
        @JoinTable(
                name = "product_promotion",
                joinColumns = @JoinColumn(name = "product_id"),
                inverseJoinColumns = @JoinColumn(name = "promotion_id")
        )
        private Set<Product> products;
}
