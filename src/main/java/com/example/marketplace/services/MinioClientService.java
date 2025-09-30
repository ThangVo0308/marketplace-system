package com.example.marketplace.services;

import com.example.marketplace.enums.FileMetadataType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
public interface MinioClientService {
    long uploadChunk(MultipartFile file, String fileMetadataId, String chunkHash, long startByte, long totalSize, String contentType, String ownerId, FileMetadataType fileMetadataType);

    void storeObject(File file, String fileName, String contentType, String bucketName);

    String getObjectUrl(String objectKey);

    void deleteObject(String objectKey, String bucketName);
}
