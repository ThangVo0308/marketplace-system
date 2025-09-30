package com.example.marketplace.controllers;

import com.example.marketplace.dtos.requests.file.FileUploadRequest;
import com.example.marketplace.dtos.responses.other.CommonResponse;
import com.example.marketplace.entities.User;
import com.example.marketplace.services.MinioClientService;
import com.example.marketplace.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static com.example.marketplace.components.Translator.getLocalizedMessage;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("${api.prefix}/file")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "File APIs")
public class FileController {

    MinioClientService minioClientService;

    UserService userService;

    @Value("${minio.bucket-name}")
    @NonFinal
    String bucketName;

    @Operation(summary = "Get file metadata by user ID", description = "Retrieve file metadata by user ID")
    @GetMapping("/metadata-by-user")
    ResponseEntity<CommonResponse<String, ?>> getFileMetadataByUserId(@RequestParam String userId) {
        User current = userService.findById(userId);
        String avatarUrl = minioClientService.getObjectUrl(current.getAvatar().getObjectKey());

        return ResponseEntity.ok(
                CommonResponse.<String, Object>builder()
                        .message(getLocalizedMessage("file_metadata_retrieved"))
                        .results(avatarUrl)
                        .build()
        );
    }

    @Operation(summary = "Upload chunk file", description = "Upload chunk file")
    @PostMapping(value = "/upload-chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<CommonResponse<Long, ?>> uploadChunk(@RequestPart(name = "file") MultipartFile file,
                                                        @RequestPart(name = "request") @Valid FileUploadRequest request) {

        long uploadedSize = minioClientService.uploadChunk(
                file,
                request.fileMetadataId(),
                request.chunkHash(),
                request.startByte(),
                request.totalSize(),
                request.contentType(),
                request.ownerId(),
                request.fileMetadataType());

        HttpStatus httpStatus = uploadedSize == request.totalSize() ? CREATED : OK;

        String message = uploadedSize == request.totalSize() ? "file_upload_success" : "chunk_uploaded";

        return ResponseEntity.status(httpStatus).body(
                CommonResponse.<Long, Object>builder()
                        .message(getLocalizedMessage(message))
                        .results(uploadedSize)
                        .build()
        );
    }

    @Operation(summary = "Delete file", description = "Delete file")
    @DeleteMapping("/delete-file/{id}")
    ResponseEntity<CommonResponse<String, Object>> deleteObject(@PathVariable String id) {
        User current = userService.findById(id);
        current.setAvatar(null);
        userService.update(current);

        return ResponseEntity.ok(
                CommonResponse.<String, Object>builder()
                        .message(getLocalizedMessage("file_metadata_retrieved"))
                        .build()
        );
    }


}
