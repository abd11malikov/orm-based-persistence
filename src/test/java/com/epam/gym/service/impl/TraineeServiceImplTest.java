package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.User;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.epam.gym.TestEntities.trainee;
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
        Trainee trainee = new Trainee(new User("John", "Smith"));
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("aB3dE5gH7j");
        when(traineeDao.create(trainee)).thenReturn(trainee);

        Trainee created = traineeService.create(trainee);

        assertEquals("John.Smith", created.getUser().getUsername());
        assertEquals("aB3dE5gH7j", created.getUser().getPassword());
        assertTrue(created.getUser().isActive());
        verify(traineeDao).create(trainee);
    }

    @Test
    void create_withoutUser_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.create(new Trainee()));
        verify(traineeDao, never()).create(any());
    }

    @Test
    void update_existingTrainee_copiesFieldsAndKeepsCredentials() {
        Trainee existing = trainee("John", "Smith", "John.Smith");
        existing.setId(1L);
        Trainee changes = trainee("Johnny", "Smith", null);
        changes.setId(1L);
        changes.getUser().setPassword(null);
        changes.setAddress("Samarkand");
        changes.setDateOfBirth(LocalDate.of(1999, 5, 5));
        when(traineeDao.findById(1L)).thenReturn(Optional.of(existing));

        Trainee updated = traineeService.update(changes);

        assertSame(existing, updated);
        assertEquals("Johnny", updated.getUser().getFirstName());
        assertEquals("Samarkand", updated.getAddress());
        assertEquals(LocalDate.of(1999, 5, 5), updated.getDateOfBirth());
        assertEquals("John.Smith", updated.getUser().getUsername());
        assertEquals("password12", updated.getUser().getPassword());
    }

    @Test
    void update_notExistingTrainee_throwsException() {
        Trainee trainee = trainee("John", "Smith", "John.Smith");
        trainee.setId(99L);
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> traineeService.update(trainee));
    }

    @Test
    void update_nullId_throwsException() {
        Trainee trainee = trainee("John", "Smith", "John.Smith");

        assertThrows(IllegalArgumentException.class, () -> traineeService.update(trainee));
    }

    @Test
    void delete_existingTrainee_deletesItAndReturnsTrue() {
        Trainee trainee = trainee("John", "Smith", "John.Smith");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        assertTrue(traineeService.delete(1L));
        verify(traineeDao).delete(trainee);
    }

    @Test
    void delete_notExistingTrainee_returnsFalse() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertFalse(traineeService.delete(99L));
        verify(traineeDao, never()).delete(any());
    }

    @Test
    void select_existingTrainee_returnsIt() {
        Trainee trainee = trainee("John", "Smith", "John.Smith");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(trainee));

        assertSame(trainee, traineeService.select(1L).orElseThrow());
    }

    @Test
    void select_notExistingTrainee_returnsEmpty() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(traineeService.select(99L).isEmpty());
    }
}
