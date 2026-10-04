package com.epam.gym.dao.impl;

import com.epam.gym.config.AppConfig;
import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static com.epam.gym.TestEntities.trainee;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@TestPropertySource("classpath:application-test.properties")
@Transactional
class UserDaoImplTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private TraineeDao traineeDao;

    @Test
    void existsByUsername_existingUser_returnsTrue() {
        traineeDao.create(trainee("John", "Smith", "John.Smith"));

        assertTrue(userDao.existsByUsername("John.Smith"));
    }

    @Test
    void existsByUsername_unknownUser_returnsFalse() {
        assertFalse(userDao.existsByUsername("Nobody.Here"));
    }

    @Test
    void findByUsername_existingUser_returnsIt() {
        traineeDao.create(trainee("John", "Smith", "John.Smith"));

        assertEquals("John", userDao.findByUsername("John.Smith").orElseThrow().getFirstName());
    }

    @Test
    void findByUsername_unknownUser_returnsEmpty() {
        assertTrue(userDao.findByUsername("Nobody.Here").isEmpty());
    }
}
