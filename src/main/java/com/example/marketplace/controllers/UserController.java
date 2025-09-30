package com.example.marketplace.controllers;

import com.example.marketplace.dtos.requests.authentication.SignUpRequest;
import com.example.marketplace.dtos.requests.file.FileUploadRequest;
import com.example.marketplace.dtos.requests.user.UserPasswordUpdateRequest;
import com.example.marketplace.dtos.requests.user.UserUpdateRequest;
import com.example.marketplace.dtos.responses.other.CommonResponse;
import com.example.marketplace.dtos.responses.user.UserResponse;
import com.example.marketplace.entities.User;
import com.example.marketplace.enums.UserStatus;
import com.example.marketplace.mappers.UserMapper;
import com.example.marketplace.services.MinioClientService;
import com.example.marketplace.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import static com.example.marketplace.components.Translator.getLocalizedMessage;


@RestController
@RequestMapping("${api.prefix}/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "User APIs")
public class UserController {

    UserService userService;

    MinioClientService minioClientService;

    UserMapper userMapper = UserMapper.INSTANCE;

    @Operation(summary = "Get all users", description = "Get all users with pagination", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping
    public ResponseEntity<Page<UserResponse>> findAll(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<User> users = userService.findAll(offset, limit);
        Page<UserResponse> userResponses = users.map(userMapper::toUserResponse);
        return ResponseEntity.ok(userResponses);
    }

    @Operation(summary = "Get user by email", description = "Get user by email", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> findByEmail(@PathVariable String email) {
        User user = userService.findByEmail(email);
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    @Operation(summary = "Get user by ID", description = "Get user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    @Operation(summary = "Check if email exists", description = "Check if email exists")
    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @Operation(summary = "Create user", description = "Create new user")
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody @Valid SignUpRequest request) {
        User user = userMapper.toUser(request);
        User createdUser = userService.create(user);
        return ResponseEntity.ok(userMapper.toUserResponse(createdUser));
    }

    @Operation(summary = "Update user password", description = "Update user password", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(@PathVariable String id,
                                               @RequestBody @Valid UserPasswordUpdateRequest request) {
        User user = userService.findById(id);
        userService.updatePassword(user, request.newPassword());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Activate user", description = "Activate user account", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable String id) {
        User user = userService.findById(id);
        userService.activateUser(user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update user", description = "Update user information", security = @SecurityRequirement(name = "bearerAuth"))
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable String id,
                                               @RequestBody @Valid SignUpRequest request) {
        User user = userMapper.toUser(request);
        user.setId(id);
        userService.update(user);
        User updatedUser = userService.findById(id);
        return ResponseEntity.ok(userMapper.toUserResponse(updatedUser));
    }

    @Operation(summary = "Delete user", description = "Delete user by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Search users", description = "Search users by keyword and status", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/search")
    public ResponseEntity<Page<UserResponse>> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {
        Page<User> users = userService.searchUsers(keyword, status, offset, limit);
        Page<UserResponse> userResponses = users.map(userMapper::toUserResponse);
        return ResponseEntity.ok(userResponses);
    }

    @Operation(summary = "Upload avatar", description = "Upload avatar", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/avatar")
    ResponseEntity<CommonResponse<Long, ?>> uploadAvatar(@RequestParam(name = "file")MultipartFile file
            , @RequestPart(name = "request")FileUploadRequest request) {
        User user = userService.findById(request.ownerId());

        long uploadedSize = minioClientService.uploadChunk(
                file,
                request.fileMetadataId(),
                request.chunkHash(),
                request.startByte(),
                request.totalSize(),
                request.contentType(),
                user.getId(),
                request.fileMetadataType());

        HttpStatus httpStatus = uploadedSize == request.totalSize() ? HttpStatus.CREATED : HttpStatus.OK;

        String message = uploadedSize == request.totalSize() ? "file_updated_success" :"chunk_uploaded";

        return ResponseEntity.status(httpStatus).body(
                CommonResponse.<Long, Object>builder()
                        .message(getLocalizedMessage(message))
                        .results(uploadedSize)
                        .build());

    }
}