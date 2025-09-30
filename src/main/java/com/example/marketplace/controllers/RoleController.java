package com.example.marketplace.controllers;

import com.example.marketplace.dtos.responses.role.RoleResponse;
import com.example.marketplace.entities.Role;
import com.example.marketplace.mappers.RoleMapper;
import com.example.marketplace.services.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@Tag(name = "Role APIs")
public class RoleController {

    RoleService roleService;

    RoleMapper roleMapper = RoleMapper.INSTANCE;

    @Operation(summary = "Get role by ID", description = "Get role by ID", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> findById(@PathVariable String id) {
        Role role = roleService.findById(id);
        return ResponseEntity.ok(roleMapper.toRoleResponse(role));
    }

    @Operation(summary = "Get role by name", description = "Get role by name", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/name/{name}")
    public ResponseEntity<RoleResponse> findByName(@PathVariable String name) {
        Role role = roleService.findByName(name);
        return ResponseEntity.ok(roleMapper.toRoleResponse(role));
    }
}