package com.example.marketplace.repositories;

import com.example.marketplace.entities.FileMetadata;
import com.example.marketplace.entities.User;
import io.micrometer.observation.annotation.Observed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Observed
public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
    FileMetadata findByUser(User user);
}
