package com.example.marketplace.services;

import com.example.marketplace.entities.Role;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {

    Role findById(String id);

    Role findByName(String name);

}
