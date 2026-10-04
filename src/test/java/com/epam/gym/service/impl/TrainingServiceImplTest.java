package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    private TrainingServiceImpl trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl();
        trainingService.setTrainingDao(trainingDao);
        trainingService.setTraineeDao(traineeDao);
        trainingService.setTrainerDao(trainerDao);
        trainingService.setTrainingTypeDao(trainingTypeDao);
    }

    @Test
    void create_allReferencesExist_loadsThemAndSavesTraining() {
        Trainee traineeFromDb = new Trainee();
        Trainer trainerFromDb = new Trainer();
        TrainingType yogaFromDb = new TrainingType("Yoga");
        Training training = training(1L, 2L, "Yoga");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(traineeFromDb));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(trainerFromDb));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(yogaFromDb));
        when(trainingDao.create(training)).thenReturn(training);

        Training created = trainingService.create(training);

        assertSame(traineeFromDb, created.getTrainee());
        assertSame(trainerFromDb, created.getTrainer());
        assertSame(yogaFromDb, created.getTrainingType());
        verify(trainingDao).create(training);
    }

    @Test
    void create_traineeNotFound_throwsException() {
        Training training = training(99L, 2L, "Yoga");
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.create(training));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void create_trainerNotFound_throwsException() {
        Training training = training(1L, 99L, "Yoga");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.create(training));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void create_unknownTrainingType_throwsException() {
        Training training = training(1L, 2L, "Swimming");
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(2L)).thenReturn(Optional.of(new Trainer()));
        when(trainingTypeDao.findByName("Swimming")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.create(training));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void create_withoutTrainee_throwsException() {
        Training training = training(1L, 2L, "Yoga");
        training.setTrainee(null);

        assertThrows(IllegalArgumentException.class, () -> trainingService.create(training));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void select_existingTraining_returnsIt() {
        Training training = training(1L, 2L, "Yoga");
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        assertSame(training, trainingService.select(1L).orElseThrow());
    }

    @Test
    void select_notExistingTraining_returnsEmpty() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(trainingService.select(99L).isEmpty());
    }

    private Training training(Long traineeId, Long trainerId, String typeName) {
        Trainee trainee = new Trainee();
        trainee.setId(traineeId);
        Trainer trainer = new Trainer();
        trainer.setId(trainerId);

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(new TrainingType(typeName));
        training.setTrainingName("Morning Session");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(60);
        return training;
    }
}
