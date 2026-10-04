package com.epam.gym.dao.impl;

import com.epam.gym.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerDaoImplTest {

    private Map<Long, Trainer> storage;
    private TrainerDaoImpl trainerDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        trainerDao = new TrainerDaoImpl();
        trainerDao.setTrainers(storage);
    }

    @Test
    void create_emptyStorage_assignsIdOne() {
        Trainer created = trainerDao.create(new Trainer());

        assertEquals(1L, created.getUserId());
        assertSame(created, storage.get(1L));
    }

    @Test
    void create_nonEmptyStorage_assignsNextIdAfterMax() {
        storage.put(3L, trainer(3L));
        storage.put(4L, trainer(4L));

        Trainer created = trainerDao.create(new Trainer());

        assertEquals(5L, created.getUserId());
    }

    @Test
    void update_existingTrainer_replacesIt() {
        storage.put(3L, trainer(3L));
        Trainer changed = trainer(3L);
        changed.setLastName("Changed");

        trainerDao.update(changed);

        assertEquals("Changed", storage.get(3L).getLastName());
    }

    @Test
    void update_notExistingTrainer_throwsException() {
        assertThrows(NoSuchElementException.class, () -> trainerDao.update(trainer(99L)));
        assertTrue(storage.isEmpty());
    }

    @Test
    void update_nullId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> trainerDao.update(new Trainer()));
    }

    @Test
    void findById_existingTrainer_returnsIt() {
        Trainer trainer = trainer(3L);
        storage.put(3L, trainer);

        Optional<Trainer> result = trainerDao.findById(3L);

        assertTrue(result.isPresent());
        assertSame(trainer, result.get());
    }

    @Test
    void findById_notExistingTrainer_returnsEmpty() {
        assertTrue(trainerDao.findById(99L).isEmpty());
    }

    @Test
    void findAll_returnsAllTrainers() {
        storage.put(3L, trainer(3L));
        storage.put(4L, trainer(4L));

        assertEquals(2, trainerDao.findAll().size());
    }

    private Trainer trainer(Long id) {
        Trainer trainer = new Trainer();
        trainer.setUserId(id);
        return trainer;
    }
}
