package com.example.marketplace.services.impls;

import com.example.marketplace.dtos.requests.file.ChunkUploadProgress;
import com.example.marketplace.enums.FileMetadataType;
import com.example.marketplace.exceptions.file.FileErrorCode;
import com.example.marketplace.exceptions.file.FileException;
import com.example.marketplace.services.MinioClientService;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.example.marketplace.helpers.Utils.*;

import static com.example.marketplace.helpers.Constants.*;
import static com.example.marketplace.exceptions.file.FileErrorCode.*;
import static java.util.concurrent.TimeUnit.DAYS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class MinioClientServiceImpl implements MinioClientService {

    MinioClient minioClient;

    ConcurrentHashMap<String, List<ChunkUploadProgress>> uploadStatusMap = new ConcurrentHashMap<>();

    @Value("${minio.bucket-name}")
    @NonFinal
    String bucketName;

    @Value("${minio.temp-bucket-name}")
    @NonFinal
    String tempBucketName;

    public MinioClientServiceImpl(@Value("${minio.endpoint}") String endpoint,
                                  @Value("${minio.access-key}") String accessKey,
                                  @Value("${minio.access-key}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .credentials(accessKey, secretKey)
                .endpoint(endpoint)
                .build();
    }

    @Override
    public long uploadChunk(MultipartFile file, String fileMetadataId, String chunkHash, long startByte, long totalSize, String contentType, String ownerId, FileMetadataType fileMetadataType) {
        if (file.getSize() > MAX_CHUNK_SIZE) throw new FileException(FILE_TOO_LARGE, BAD_REQUEST);

        if (file.getSize() > MAX_FILE_SIZE) throw new FileException(FILE_TOO_LARGE, BAD_REQUEST);

        if (!isMedia(contentType)) throw new FileException(INVALID_FILE_TYPE, BAD_REQUEST);

        uploadStatusMap.putIfAbsent(fileMetadataId, new CopyOnWriteArrayList<>());
        List<ChunkUploadProgress> uploadProgresses = uploadStatusMap.get(fileMetadataId);

        if (uploadProgresses.stream().anyMatch(item -> item.getChunkPosition() == startByte && item.isUploaded()))
            throw new FileException(CHUNK_ALREADY_EXISTS, HttpStatus.OK);

        Path tempDir = Paths.get(TEMP_DIR);
        if (!Files.exists(tempDir)) {
            try {
                Files.createDirectories(tempDir);
            } catch(IOException e) {
                throw new FileException(CAN_NOT_INIT_BACKUP_FOLDER, BAD_REQUEST);
            }
        }

        File chunkFile = new File(TEMP_DIR + fileMetadataId + "_" + startByte);
        try (FileOutputStream fos = new FileOutputStream(chunkFile)) {
            fos.write(file.getBytes());
        } catch (IOException e) {
            throw new FileException(COULD_NOT_WRITE_CHUNK, BAD_REQUEST);
        }

        saveChunkProgress(startByte, uploadProgresses);

        if (startByte + file.getSize() == totalSize) {
            try {
                combineChunk(fileMetadataId, startByte);
            } catch (Exception e) {
                throw new FileException(COULD_NOT_COMBINE_CHUNKS, BAD_REQUEST);
            }
            File fileAfterCombine = new File(TEMP_DIR, fileMetadataId);
            String newFileName = generateFileName(contentType.split("/")[0], contentType.split("/")[1]);

            try {
                storeObject(fileAfterCombine, newFileName, contentType, bucketName);
                Files.delete(fileAfterCombine.toPath()); // Xóa file sau khi upload hoàn thành
            } catch (Exception e) {
                throw new FileException(FileErrorCode.CAN_NOT_STORE_FILE, BAD_REQUEST);
            }

            uploadStatusMap.remove(fileMetadataId);
            return totalSize;
        }


        return startByte + file.getSize();
    }

    private void combineChunk(String fileMetadataId, long totalSize) throws IOException {
        File outputFile = new File(TEMP_DIR + fileMetadataId);

        if (outputFile.exists()) Files.delete(outputFile.toPath());

        try (FileOutputStream fos = new FileOutputStream(outputFile, true)) {
            long bytesWritten = 0;

            while (bytesWritten < totalSize) {
                Path chunkPath = Paths.get(TEMP_DIR + fileMetadataId + "_" + bytesWritten);

                if (!Files.exists(chunkPath))
                    throw new FileException(FileErrorCode.CHUNK_MISSING, BAD_REQUEST);

                byte[] chunkBytes = Files.readAllBytes(chunkPath);
                fos.write(chunkBytes);

                // Cập nhật số byte đã ghi
                bytesWritten += chunkBytes.length;

                // Xóa chunk sau khi ghép vào file
                Files.delete(chunkPath);
            }
        } catch (IOException e) {
            throw new FileException(COULD_NOT_COMBINE_CHUNKS, BAD_REQUEST);
        }

    }

    @Override
    public void storeObject(File file, String fileName, String contentType, String bucketName) {
        try {
            ensureBucketExists(fileName);
            minioClient.putObject(PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(Files.newInputStream(file.toPath()), file.length(), -1) // cầu nối để chuyển dữ liệu từ local trên máy >> minio server
                            .contentType(contentType)
                    .build());
        } catch(Exception e) {
            throw new FileException(CAN_NOT_STORE_FILE, BAD_REQUEST);
        }
    }

    @Override
    public String getObjectUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectKey)
                    .expiry(1, DAYS) // 1 week
                    .build());

        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            throw new FileException(COULD_NOT_READ_FILE, BAD_REQUEST);
        }
    }

    @Override
    public void deleteObject(String objectKey, String bucketName) {
        GetObjectResponse response;
        try {
            response = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());
        } catch (Exception e) {
            throw new FileException(COULD_NOT_READ_FILE, BAD_REQUEST);
        }

        if (response == null) throw new FileException(FILE_NOT_FOUND, BAD_REQUEST);

        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectKey)
                    .build());

        } catch (Exception e) {
            throw new FileException(CAN_NOT_DELETE_FILE, BAD_REQUEST);
        }
    }

    private void saveChunkProgress(long startByte, List<ChunkUploadProgress> uploadProgresses) {
        synchronized (uploadProgresses) {
            ChunkUploadProgress chunkUploadProgress = uploadProgresses.stream()
                    .findFirst() // if exists
                    .orElseGet(() -> {// does not exist
                        ChunkUploadProgress newChunk = new ChunkUploadProgress(startByte, false);
                        uploadProgresses.add(newChunk);
                        return newChunk;
                    });

            chunkUploadProgress.setUploaded(true);
        }
    }

    private void ensureBucketExists(String bucketName) {
        boolean found;
        try {
            found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());

            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch(Exception e) {
            throw new FileException(CAN_NOT_CHECK_BUCKET, BAD_REQUEST);
        }
    }
}
