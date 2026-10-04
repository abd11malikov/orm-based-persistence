package com.epam.gym.dao.impl;

import com.epam.gym.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainingDaoImplTest {

    private Map<Long, Training> storage;
    private TrainingDaoImpl trainingDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainingDao = new TrainingDaoImpl();
        trainingDao.setTrainings(storage);
    }

    @Test
    void create_emptyStorage_assignsIdOne() {
        Training created = trainingDao.create(new Training());

        assertEquals(1L, created.getTrainingId());
        assertSame(created, storage.get(1L));
    }

    @Test
    void create_nonEmptyStorage_assignsNextIdAfterMax() {
        storage.put(1L, training(1L));
        storage.put(2L, training(2L));

        Training created = trainingDao.create(new Training());

        assertEquals(3L, created.getTrainingId());
    }

    @Test
    void findById_existingTraining_returnsIt() {
        Training training = training(1L);
        storage.put(1L, training);

        Optional<Training> result = trainingDao.findById(1L);

        assertTrue(result.isPresent());
        assertSame(training, result.get());
    }

    @Test
    void findById_notExistingTraining_returnsEmpty() {
        assertTrue(trainingDao.findById(99L).isEmpty());
    }

    @Test
    void findAll_returnsAllTrainings() {
        storage.put(1L, training(1L));
        storage.put(2L, training(2L));

        assertEquals(2, trainingDao.findAll().size());
    }

    private Training training(Long id) {
        Training training = new Training();
        training.setTrainingId(id);
        return training;
    }
}
