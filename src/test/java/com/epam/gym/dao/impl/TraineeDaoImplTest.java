package com.epam.gym.dao.impl;

import com.epam.gym.model.Trainee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeDaoImplTest {

    private Map<Long, Trainee> storage;
    private TraineeDaoImpl traineeDao;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        traineeDao = new TraineeDaoImpl();
        traineeDao.setTrainees(storage);
    }

    @Test
    void create_emptyStorage_assignsIdOne() {
        Trainee created = traineeDao.create(new Trainee());

        assertEquals(1L, created.getUserId());
        assertSame(created, storage.get(1L));
    }

    @Test
    void create_nonEmptyStorage_assignsNextIdAfterMax() {
        storage.put(1L, trainee(1L));
        storage.put(5L, trainee(5L));

        Trainee created = traineeDao.create(new Trainee());

        assertEquals(6L, created.getUserId());
        assertEquals(3, storage.size());
    }

    @Test
    void update_existingTrainee_replacesIt() {
        storage.put(1L, trainee(1L));
        Trainee changed = trainee(1L);
        changed.setAddress("New address");

        traineeDao.update(changed);

        assertEquals("New address", storage.get(1L).getAddress());
    }

    @Test
    void update_notExistingTrainee_throwsException() {
        assertThrows(NoSuchElementException.class, () -> traineeDao.update(trainee(99L)));
        assertTrue(storage.isEmpty());
    }

    @Test
    void update_nullId_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> traineeDao.update(new Trainee()));
    }

    @Test
    void delete_existingTrainee_returnsTrueAndRemovesIt() {
        storage.put(1L, trainee(1L));

        assertTrue(traineeDao.delete(1L));
        assertTrue(storage.isEmpty());
    }

    @Test
    void delete_notExistingTrainee_returnsFalse() {
        assertFalse(traineeDao.delete(99L));
    }

    @Test
    void findById_existingTrainee_returnsIt() {
        Trainee trainee = trainee(1L);
        storage.put(1L, trainee);

        Optional<Trainee> result = traineeDao.findById(1L);

        assertTrue(result.isPresent());
        assertSame(trainee, result.get());
    }

    @Test
    void findById_notExistingTrainee_returnsEmpty() {
        assertTrue(traineeDao.findById(99L).isEmpty());
    }

    @Test
    void findAll_returnsAllTrainees() {
        storage.put(1L, trainee(1L));
        storage.put(2L, trainee(2L));

        assertEquals(2, traineeDao.findAll().size());
    }

    @Test
    void findAll_returnedListCannotChangeStorage() {
        storage.put(1L, trainee(1L));
        List<Trainee> all = traineeDao.findAll();

        assertThrows(UnsupportedOperationException.class, () -> all.add(trainee(2L)));
        assertEquals(1, storage.size());
    }

    private Trainee trainee(Long id) {
        Trainee trainee = new Trainee();
        trainee.setUserId(id);
        return trainee;
    }
}
