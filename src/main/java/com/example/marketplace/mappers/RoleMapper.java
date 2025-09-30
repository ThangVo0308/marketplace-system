package com.example.marketplace.mappers;

import com.example.marketplace.dtos.requests.role.RoleRequest;
import com.example.marketplace.dtos.responses.role.RoleResponse;
import com.example.marketplace.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    RoleResponse toRoleResponse(Role entity);

    Role toRole(RoleRequest dto);
}