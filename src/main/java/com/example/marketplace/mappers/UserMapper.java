package com.example.marketplace.mappers;

import com.example.marketplace.dtos.requests.authentication.SignUpRequest;
import com.example.marketplace.dtos.responses.user.UserResponse;
import com.example.marketplace.entities.User;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toUser(SignUpRequest request);

    UserResponse toUserResponse(User user);

    @AfterMapping
    default void customizeDto(User entity, @MappingTarget UserResponse dto) {
        if (entity.getRole() != null) {
            dto.setMRole(entity.getRole().toString());
        }
    }
}
