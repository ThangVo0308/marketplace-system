package com.example.marketplace.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "file_metadata")
public class FileMetadata extends AbstractEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(name = "object_key", nullable = false)
    String objectKey;

    @Column(name = "content_type", nullable = false)
    String contentType;

    @Column(nullable = false)
    long size;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_file_metadata_users", foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE"))
    @JsonBackReference
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_file_metadata_products", foreignKeyDefinition = "FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE")
            )
    @JsonBackReference
    Product product;
}
