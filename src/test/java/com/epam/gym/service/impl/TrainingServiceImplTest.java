package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private TrainingServiceImpl trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl();
        trainingService.setTrainingDao(trainingDao);
        trainingService.setTraineeDao(traineeDao);
        trainingService.setTrainerDao(trainerDao);
    }

    @Test
    void create_traineeAndTrainerExist_savesTraining() {
        Training training = training(1L, 3L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(3L)).thenReturn(Optional.of(new Trainer()));
        when(trainingDao.create(training)).thenReturn(training);

        Training created = trainingService.create(training);

        assertSame(training, created);
        verify(trainingDao).create(training);
    }

    @Test
    void create_traineeNotFound_throwsException() {
        Training training = training(99L, 3L);
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.create(training));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void create_trainerNotFound_throwsException() {
        Training training = training(1L, 99L);
        when(traineeDao.findById(1L)).thenReturn(Optional.of(new Trainee()));
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.create(training));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void create_nullTraineeId_throwsException() {
        Training training = training(null, 3L);

        assertThrows(NoSuchElementException.class, () -> trainingService.create(training));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void select_existingTraining_returnsIt() {
        Training training = training(1L, 3L);
        when(trainingDao.findById(1L)).thenReturn(Optional.of(training));

        Optional<Training> result = trainingService.select(1L);

        assertTrue(result.isPresent());
        assertSame(training, result.get());
    }

    @Test
    void select_notExistingTraining_returnsEmpty() {
        when(trainingDao.findById(99L)).thenReturn(Optional.empty());

        assertTrue(trainingService.select(99L).isEmpty());
    }

    private Training training(Long traineeId, Long trainerId) {
        Training training = new Training();
        training.setTraineeId(traineeId);
        training.setTrainerId(trainerId);
        training.setTrainingName("Morning Fitness");
        return training;
    }
}
