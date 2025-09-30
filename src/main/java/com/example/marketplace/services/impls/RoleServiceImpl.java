package com.example.marketplace.services.impls;

import com.example.marketplace.entities.Role;
import com.example.marketplace.exceptions.AppException;
import com.example.marketplace.exceptions.CommonErrorCode;
import com.example.marketplace.repositories.RoleRepository;
import com.example.marketplace.services.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleServiceImpl implements RoleService {

    RoleRepository roleRepository;

    @Override
    public Role findById(String id) {
        return roleRepository.findById(id).orElseThrow(() ->
                new AppException(CommonErrorCode.ROLE_NOT_FOUND, NOT_FOUND));
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName(name).orElseThrow(() ->
                new AppException(CommonErrorCode.ROLE_NOT_FOUND, NOT_FOUND));
    }

}
