package com.epam.gym.dao;

import com.epam.gym.model.User;

import java.util.Optional;

public interface UserDao {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String username);
}
