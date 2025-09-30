package com.example.marketplace.dtos.requests.file;

import com.example.marketplace.enums.FileMetadataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileUploadRequest (

    @NotNull(message = "null_field")
    @NotBlank(message = "blank_field")
    String fileMetadataId,

    @NotNull(message = "null_field")
    @NotBlank(message = "blank_field")
    String ownerId,

    FileMetadataType fileMetadataType,

    String chunkHash,

    long startByte,

    long totalSize,

    @NotNull(message = "null_field")
    @NotBlank(message = "blank_field")
    String contentType

)
{ }
