package com.epam.gym.util;

import com.epam.gym.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UsernameGenerator {
    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);

    private final UserDao userDao;

    public UsernameGenerator(UserDao userDao) {
        this.userDao = userDao;
    }

    public String generate(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name must not be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be empty");
        }

        String baseUsername = firstName.trim() + "." + lastName.trim();

        String username = baseUsername;
        int serialNumber = 1;
        while (userDao.existsByUsername(username)) {
            username = baseUsername + serialNumber;
            serialNumber++;
        }

        log.debug("Generated username {} for {} {}", username, firstName, lastName);
        return username;
    }
}
