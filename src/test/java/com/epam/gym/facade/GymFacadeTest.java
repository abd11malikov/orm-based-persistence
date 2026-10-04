package com.epam.gym.facade;

import com.epam.gym.dto.Credentials;
import com.epam.gym.dto.TrainingRequest;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymFacadeTest {

    private static final Credentials LOGIN = new Credentials("John.Smith", "password12");
    private static final LocalDate DATE = LocalDate.of(2026, 3, 10);

    @Mock
    private TraineeService traineeService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private TrainingService trainingService;

    @Mock
    private TrainingTypeService trainingTypeService;

    private GymFacade facade;

    @BeforeEach
    void setUp() {
        facade = new GymFacade(traineeService, trainerService, trainingService, trainingTypeService);
    }

    @Test
    void createTrainer_delegatesToTrainerService() {
        Trainer trainer = new Trainer();
        when(trainerService.create(trainer)).thenReturn(trainer);

        assertSame(trainer, facade.createTrainer(trainer));
    }

    @Test
    void createTrainee_delegatesToTraineeService() {
        Trainee trainee = new Trainee();
        when(traineeService.create(trainee)).thenReturn(trainee);

        assertSame(trainee, facade.createTrainee(trainee));
    }

    @Test
    void traineeCredentialsMatch_delegatesToTraineeService() {
        when(traineeService.credentialsMatch("John.Smith", "password12")).thenReturn(true);

        assertTrue(facade.traineeCredentialsMatch("John.Smith", "password12"));
    }

    @Test
    void trainerCredentialsMatch_delegatesToTrainerService() {
        when(trainerService.credentialsMatch("Nodira.Karimova", "password12")).thenReturn(true);

        assertTrue(facade.trainerCredentialsMatch("Nodira.Karimova", "password12"));
    }

    @Test
    void selectTrainer_delegatesToTrainerService() {
        Trainer trainer = new Trainer();
        when(trainerService.selectByUsername(LOGIN, "Nodira.Karimova")).thenReturn(trainer);

        assertSame(trainer, facade.selectTrainer(LOGIN, "Nodira.Karimova"));
    }

    @Test
    void selectTrainee_delegatesToTraineeService() {
        Trainee trainee = new Trainee();
        when(traineeService.selectByUsername(LOGIN, "John.Smith")).thenReturn(trainee);

        assertSame(trainee, facade.selectTrainee(LOGIN, "John.Smith"));
    }

    @Test
    void changeTraineePassword_delegatesToTraineeService() {
        facade.changeTraineePassword(LOGIN, "newSecret1");

        verify(traineeService).changePassword(LOGIN, "newSecret1");
    }

    @Test
    void changeTrainerPassword_delegatesToTrainerService() {
        facade.changeTrainerPassword(LOGIN, "newSecret1");

        verify(trainerService).changePassword(LOGIN, "newSecret1");
    }

    @Test
    void updateTrainer_delegatesToTrainerService() {
        Trainer changes = new Trainer();
        when(trainerService.updateProfile(LOGIN, changes)).thenReturn(changes);

        assertSame(changes, facade.updateTrainer(LOGIN, changes));
    }

    @Test
    void updateTrainee_delegatesToTraineeService() {
        Trainee changes = new Trainee();
        when(traineeService.updateProfile(LOGIN, changes)).thenReturn(changes);

        assertSame(changes, facade.updateTrainee(LOGIN, changes));
    }

    @Test
    void activateAndDeactivateTrainee_delegateToTraineeService() {
        facade.activateTrainee(LOGIN);
        facade.deactivateTrainee(LOGIN);

        verify(traineeService).activate(LOGIN);
        verify(traineeService).deactivate(LOGIN);
    }

    @Test
    void activateAndDeactivateTrainer_delegateToTrainerService() {
        facade.activateTrainer(LOGIN);
        facade.deactivateTrainer(LOGIN);

        verify(trainerService).activate(LOGIN);
        verify(trainerService).deactivate(LOGIN);
    }

    @Test
    void deleteTrainee_delegatesToTraineeService() {
        facade.deleteTrainee(LOGIN);

        verify(traineeService).delete(LOGIN);
    }

    @Test
    void getTraineeTrainings_delegatesToTrainingService() {
        List<Training> trainings = List.of(new Training());
        when(trainingService.getTraineeTrainings(LOGIN, DATE, DATE, "Nodira.Karimova", "Yoga")).thenReturn(trainings);

        assertEquals(trainings, facade.getTraineeTrainings(LOGIN, DATE, DATE, "Nodira.Karimova", "Yoga"));
    }

    @Test
    void getTrainerTrainings_delegatesToTrainingService() {
        List<Training> trainings = List.of(new Training());
        when(trainingService.getTrainerTrainings(LOGIN, DATE, DATE, "John.Smith")).thenReturn(trainings);

        assertEquals(trainings, facade.getTrainerTrainings(LOGIN, DATE, DATE, "John.Smith"));
    }

    @Test
    void addTraining_delegatesToTrainingService() {
        TrainingRequest request = new TrainingRequest("John.Smith", "Nodira.Karimova", "Yoga", "Yoga", DATE, 60);
        Training training = new Training();
        when(trainingService.addTraining(LOGIN, request)).thenReturn(training);

        assertSame(training, facade.addTraining(LOGIN, request));
    }

    @Test
    void getUnassignedTrainers_delegatesToTraineeService() {
        List<Trainer> trainers = List.of(new Trainer());
        when(traineeService.getUnassignedTrainers(LOGIN)).thenReturn(trainers);

        assertEquals(trainers, facade.getUnassignedTrainers(LOGIN));
    }

    @Test
    void updateTraineeTrainers_delegatesToTraineeService() {
        List<Trainer> trainers = List.of(new Trainer());
        when(traineeService.updateTrainers(LOGIN, List.of("Nodira.Karimova"))).thenReturn(trainers);

        assertEquals(trainers, facade.updateTraineeTrainers(LOGIN, List.of("Nodira.Karimova")));
    }

    @Test
    void getTrainingTypes_delegatesToTrainingTypeService() {
        List<TrainingType> types = List.of(new TrainingType("Yoga"));
        when(trainingTypeService.findAll()).thenReturn(types);

        assertEquals(types, facade.getTrainingTypes());
    }
}
