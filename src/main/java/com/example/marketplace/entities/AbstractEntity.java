package com.example.marketplace.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class AbstractEntity {
    @CreatedBy
    @Column(name = "created_by")
    String createdBy;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    Date createdAt;

    @LastModifiedBy
    @Column(name = "updated_by")
    String updatedBy;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    Date updatedAt;

    @Builder.Default
    @Column(name = "is_deleted" , nullable = false)
    Boolean isDeleted = false;

    @Column(name = "deleted_by")
    String deletedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    Date deletedAt;

    @Version
    @Column(name = "version")
    Long version;
}
