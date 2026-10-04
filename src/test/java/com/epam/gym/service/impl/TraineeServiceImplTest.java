package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeServiceImpl traineeService;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void create_setsGeneratedUsernamePasswordAndActive() {
        Trainee trainee = trainee(null, "John", "Smith");

        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("aB3dE5gH7j");
        when(traineeDao.create(trainee)).thenReturn(trainee);

        Trainee created = traineeService.create(trainee);

        assertEquals("John.Smith", created.getUsername());
        assertEquals("aB3dE5gH7j", created.getPassword());
        assertTrue(created.isActive());
        verify(traineeDao).create(trainee);
    }

    @Test
    void update_existingTrainee_keepsUsernameAndPassword() {
        Trainee existing = trainee(1L, "John", "Smith");
        existing.setUsername("John.Smith");
        existing.setPassword("oldPass123");
        Trainee changes = trainee(1L, "John", "Smith");
        changes.setAddress("New address");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(existing));
        when(traineeDao.update(changes)).thenReturn(changes);

        Trainee updated = traineeService.update(changes);

        assertEquals("John.Smith", updated.getUsername());
        assertEquals("oldPass123", updated.getPassword());
        assertEquals("New address", updated.getAddress());
        verify(traineeDao).update(changes);
    }

    @Test
    void update_notExistingTrainee_throwsException() {
        Trainee trainee = trainee(99L, "John", "Smith");
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> traineeService.update(trainee));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void update_nullId_throwsException() {
        Trainee trainee = trainee(null, "John", "Smith");

        assertThrows(IllegalArgumentException.class, () -> traineeService.update(trainee));
        verify(traineeDao, never()).update(any());
    }

    @Test
    void delete_existingTrainee_returnsTrue() {
        when(traineeDao.delete(1L)).thenReturn(true);

        assertTrue(traineeService.delete(1L));
    }

    @Test
    void delete_notExistingTrainee_returnsFalse() {
        when(traineeDao.delete(99L)).thenReturn(false);

        assertFalse(traineeService.delete(99L));
    }

    @Test
    void select_existingTrainee_returnsIt() {
        Trainee trainee = trainee(1L, "John", "Smith");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        Optional<Trainee> result = traineeService.select(1L);

        assertTrue(result.isPresent());
        assertSame(trainee, result.get());
    }

    @Test
    void select_notExistingTrainee_returnsEmpty() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(traineeService.select(99L).isEmpty());
    }

    private Trainee trainee(Long id, String firstName, String lastName) {
        Trainee trainee = new Trainee();
        trainee.setUserId(id);
        trainee.setFirstName(firstName);
        trainee.setLastName(lastName);
        return trainee;
    }
}
