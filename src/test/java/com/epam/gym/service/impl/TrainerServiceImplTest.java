package com.epam.gym.service.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static com.epam.gym.TestEntities.trainer;
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
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerServiceImpl trainerService;

    private final TrainingType yoga = new TrainingType("Yoga");
    private final TrainingType cardio = new TrainingType("Cardio");

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setTrainingTypeDao(trainingTypeDao);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);
    }

    @Test
    void create_setsCredentialsAndLoadsSpecializationFromDatabase() {
        TrainingType yogaFromDb = new TrainingType("Yoga");
        Trainer trainer = new Trainer(new User("Nodira", "Karimova"), yoga);
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(yogaFromDb));
        when(usernameGenerator.generate("Nodira", "Karimova")).thenReturn("Nodira.Karimova");
        when(passwordGenerator.generate()).thenReturn("sT7uV1wX3y");
        when(trainerDao.create(trainer)).thenReturn(trainer);

        Trainer created = trainerService.create(trainer);

        assertSame(yogaFromDb, created.getSpecialization());
        assertEquals("Nodira.Karimova", created.getUser().getUsername());
        assertEquals("sT7uV1wX3y", created.getUser().getPassword());
        assertTrue(created.getUser().isActive());
        verify(trainerDao).create(trainer);
    }

    @Test
    void create_unknownSpecialization_throwsException() {
        Trainer trainer = new Trainer(new User("Nodira", "Karimova"), new TrainingType("Swimming"));
        when(trainingTypeDao.findByName("Swimming")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainerService.create(trainer));
        verify(trainerDao, never()).create(any());
    }

    @Test
    void create_withoutSpecialization_throwsException() {
        Trainer trainer = new Trainer(new User("Nodira", "Karimova"), null);

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(trainer));
        verify(trainerDao, never()).create(any());
    }

    @Test
    void update_existingTrainer_copiesFieldsAndKeepsCredentials() {
        Trainer existing = trainer("Nodira", "Karimova", "Nodira.Karimova", yoga);
        existing.setId(1L);
        Trainer changes = trainer("Nodira", "Aliyeva", null, cardio);
        changes.setId(1L);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(existing));
        when(trainingTypeDao.findByName("Cardio")).thenReturn(Optional.of(cardio));

        Trainer updated = trainerService.update(changes);

        assertSame(existing, updated);
        assertEquals("Aliyeva", updated.getUser().getLastName());
        assertEquals("Cardio", updated.getSpecialization().getTrainingTypeName());
        assertEquals("Nodira.Karimova", updated.getUser().getUsername());
    }

    @Test
    void update_notExistingTrainer_throwsException() {
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", yoga);
        trainer.setId(99L);
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainerService.update(trainer));
    }

    @Test
    void update_nullId_throwsException() {
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", yoga);

        assertThrows(IllegalArgumentException.class, () -> trainerService.update(trainer));
    }

    @Test
    void select_existingTrainer_returnsIt() {
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", yoga);
        when(trainerDao.findById(1L)).thenReturn(Optional.of(trainer));

        assertSame(trainer, trainerService.select(1L).orElseThrow());
    }

    @Test
    void select_notExistingTrainer_returnsEmpty() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(trainerService.select(99L).isEmpty());
    }
}
