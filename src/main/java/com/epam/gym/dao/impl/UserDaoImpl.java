package com.epam.gym.dao.impl;

import com.epam.gym.dao.UserDao;
import com.epam.gym.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDaoImpl implements UserDao {
    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public boolean existsByUsername(String username) {
        Long count = entityManager
                .createQuery("SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        log.debug("Username {} exists: {}", username, count > 0);
        return count > 0;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        Optional<User> user = entityManager
                .createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
        log.debug("Find user by username {}: {}", username, user.isPresent() ? "found" : "not found");
        return user;
    }
}
