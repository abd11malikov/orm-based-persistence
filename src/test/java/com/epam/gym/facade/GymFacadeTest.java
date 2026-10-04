package com.epam.gym.facade;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    private GymFacade facade;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService);
    }

    @Test
    void createTrainee_delegatesToTraineeService() {
        Trainee trainee = new Trainee();
        when(traineeService.create(trainee)).thenReturn(trainee);

        assertSame(trainee, facade.createTrainee(trainee));
        verify(traineeService).create(trainee);
    }

    @Test
    void updateTrainee_delegatesToTraineeService() {
        Trainee trainee = new Trainee();
        when(traineeService.update(trainee)).thenReturn(trainee);

        assertSame(trainee, facade.updateTrainee(trainee));
        verify(traineeService).update(trainee);
    }

    @Test
    void deleteTrainee_delegatesToTraineeService() {
        when(traineeService.delete(1L)).thenReturn(true);

        assertTrue(facade.deleteTrainee(1L));
        verify(traineeService).delete(1L);
    }

    @Test
    void selectTrainee_delegatesToTraineeService() {
        Trainee trainee = new Trainee();
        when(traineeService.select(1L)).thenReturn(Optional.of(trainee));

        assertSame(trainee, facade.selectTrainee(1L).orElseThrow());
        verify(traineeService).select(1L);
    }

    @Test
    void createTrainer_delegatesToTrainerService() {
        Trainer trainer = new Trainer();
        when(trainerService.create(trainer)).thenReturn(trainer);

        assertSame(trainer, facade.createTrainer(trainer));
        verify(trainerService).create(trainer);
    }

    @Test
    void updateTrainer_delegatesToTrainerService() {
        Trainer trainer = new Trainer();
        when(trainerService.update(trainer)).thenReturn(trainer);

        assertSame(trainer, facade.updateTrainer(trainer));
        verify(trainerService).update(trainer);
    }

    @Test
    void selectTrainer_delegatesToTrainerService() {
        Trainer trainer = new Trainer();
        when(trainerService.select(3L)).thenReturn(Optional.of(trainer));

        assertSame(trainer, facade.selectTrainer(3L).orElseThrow());
        verify(trainerService).select(3L);
    }

    @Test
    void createTraining_delegatesToTrainingService() {
        Training training = new Training();
        when(trainingService.create(training)).thenReturn(training);

        assertSame(training, facade.createTraining(training));
        verify(trainingService).create(training);
    }

    @Test
    void selectTraining_delegatesToTrainingService() {
        Training training = new Training();
        when(trainingService.select(1L)).thenReturn(Optional.of(training));

        assertSame(training, facade.selectTraining(1L).orElseThrow());
        verify(trainingService).select(1L);
    }
}
