package com.example.marketplace.services;

import com.example.marketplace.entities.User;
import com.example.marketplace.enums.UserStatus;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<User> findAll(int offset, int limit);

    User findByEmail(String email);

    User findById(String id);

    boolean existsByEmail(String email);

    User create(User user);

    void updatePassword(User user, String password);

    void activateUser(User user);

    void update(User user);

    void deleteUser(String id);

    Page<User> searchUsers(String keyword, UserStatus status, int offset, int limit);
}