package com.example.marketplace.dtos.responses.file;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileUploadResponse {

    long uploadedChunksSize;

}
