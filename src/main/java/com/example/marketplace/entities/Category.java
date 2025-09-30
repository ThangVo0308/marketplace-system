package com.example.marketplace.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "categories")
public class Category extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @Column(nullable = false, length = 100)
    String name;

    @ManyToMany(mappedBy = "categories", fetch = FetchType.LAZY)
    @JsonBackReference
    List<Product> products;

    // @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval =
    // true)
    // @JsonBackReference
    // List<FileMetadata> images;
}
