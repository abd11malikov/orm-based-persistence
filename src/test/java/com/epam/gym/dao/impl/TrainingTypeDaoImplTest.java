package com.epam.gym.dao.impl;

import com.epam.gym.config.AppConfig;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.TrainingType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@TestPropertySource("classpath:application-test.properties")
@Transactional
class TrainingTypeDaoImplTest {

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Test
    void findAll_returnsSeededTypesSortedByName() {
        List<TrainingType> types = trainingTypeDao.findAll();

        assertEquals(6, types.size());
        assertEquals("Cardio", types.get(0).getTrainingTypeName());
    }

    @Test
    void findByName_existingType_returnsIt() {
        TrainingType yoga = trainingTypeDao.findByName("Yoga").orElseThrow();

        assertNotNull(yoga.getId());
        assertEquals("Yoga", yoga.getTrainingTypeName());
    }

    @Test
    void findByName_unknownType_returnsEmpty() {
        assertTrue(trainingTypeDao.findByName("Swimming").isEmpty());
    }
}
