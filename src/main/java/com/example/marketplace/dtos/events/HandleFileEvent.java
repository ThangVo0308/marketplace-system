package com.example.marketplace.dtos.events;

import com.example.marketplace.enums.FileMetadataType;
import com.example.marketplace.enums.HandleFileAction;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HandleFileEvent {
    String id;

    String objectKey;

    long size;

    String contentType;

    String sellerId;

    HandleFileAction action;

    FileMetadataType type;
}
