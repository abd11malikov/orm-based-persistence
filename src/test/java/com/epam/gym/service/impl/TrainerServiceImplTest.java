package com.epam.gym.service.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.model.Trainer;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerServiceImpl trainerService;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void create_setsGeneratedUsernamePasswordAndActive() {
        Trainer trainer = trainer(null, "Robert", "Taylor");
        when(usernameGenerator.generate("Robert", "Taylor")).thenReturn("Robert.Taylor");
        when(passwordGenerator.generate()).thenReturn("sT7uV1wX3y");
        when(trainerDao.create(trainer)).thenReturn(trainer);

        Trainer created = trainerService.create(trainer);

        assertEquals("Robert.Taylor", created.getUsername());
        assertEquals("sT7uV1wX3y", created.getPassword());
        assertTrue(created.isActive());
        verify(trainerDao).create(trainer);
    }

    @Test
    void update_existingTrainer_keepsUsernameAndPassword() {
        Trainer existing = trainer(3L, "Robert", "Taylor");
        existing.setUsername("Robert.Taylor");
        existing.setPassword("oldPass123");
        Trainer changes = trainer(3L, "Robert", "Taylor");
        when(trainerDao.findById(3L)).thenReturn(Optional.of(existing));
        when(trainerDao.update(changes)).thenReturn(changes);

        Trainer updated = trainerService.update(changes);

        assertEquals("Robert.Taylor", updated.getUsername());
        assertEquals("oldPass123", updated.getPassword());
        verify(trainerDao).update(changes);
    }

    @Test
    void update_notExistingTrainer_throwsException() {
        Trainer trainer = trainer(99L, "Robert", "Taylor");
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainerService.update(trainer));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void update_nullId_throwsException() {
        Trainer trainer = trainer(null, "Robert", "Taylor");

        assertThrows(IllegalArgumentException.class, () -> trainerService.update(trainer));
        verify(trainerDao, never()).update(any());
    }

    @Test
    void select_existingTrainer_returnsIt() {
        Trainer trainer = trainer(3L, "Robert", "Taylor");
        when(trainerDao.findById(3L)).thenReturn(Optional.of(trainer));

        Optional<Trainer> result = trainerService.select(3L);

        assertTrue(result.isPresent());
        assertSame(trainer, result.get());
    }

    @Test
    void select_notExistingTrainer_returnsEmpty() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(trainerService.select(99L).isEmpty());
    }

    private Trainer trainer(Long id, String firstName, String lastName) {
        Trainer trainer = new Trainer();
        trainer.setUserId(id);
        trainer.setFirstName(firstName);
        trainer.setLastName(lastName);
        return trainer;
    }
}
