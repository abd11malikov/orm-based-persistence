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
        // given
        Trainer trainer = new Trainer();
        when(trainerService.create(trainer)).thenReturn(trainer);

        // when
        Trainer result = facade.createTrainer(trainer);

        // then
        assertSame(trainer, result);
    }

    @Test
    void createTrainee_delegatesToTraineeService() {
        // given
        Trainee trainee = new Trainee();
        when(traineeService.create(trainee)).thenReturn(trainee);

        // when
        Trainee result = facade.createTrainee(trainee);

        // then
        assertSame(trainee, result);
    }

    @Test
    void traineeCredentialsMatch_delegatesToTraineeService() {
        // given
        when(traineeService.credentialsMatch("John.Smith", "password12")).thenReturn(true);

        // when
        boolean matches = facade.traineeCredentialsMatch("John.Smith", "password12");

        // then
        assertTrue(matches);
    }

    @Test
    void trainerCredentialsMatch_delegatesToTrainerService() {
        // given
        when(trainerService.credentialsMatch("Nodira.Karimova", "password12")).thenReturn(true);

        // when
        boolean matches = facade.trainerCredentialsMatch("Nodira.Karimova", "password12");

        // then
        assertTrue(matches);
    }

    @Test
    void selectTrainer_delegatesToTrainerService() {
        // given
        Trainer trainer = new Trainer();
        when(trainerService.selectByUsername(LOGIN, "Nodira.Karimova")).thenReturn(trainer);

        // when
        Trainer result = facade.selectTrainer(LOGIN, "Nodira.Karimova");

        // then
        assertSame(trainer, result);
    }

    @Test
    void selectTrainee_delegatesToTraineeService() {
        // given
        Trainee trainee = new Trainee();
        when(traineeService.selectByUsername(LOGIN, "John.Smith")).thenReturn(trainee);

        // when
        Trainee result = facade.selectTrainee(LOGIN, "John.Smith");

        // then
        assertSame(trainee, result);
    }

    @Test
    void changeTraineePassword_delegatesToTraineeService() {
        // given
        String newPassword = "newSecret1";

        // when
        facade.changeTraineePassword(LOGIN, newPassword);

        // then
        verify(traineeService).changePassword(LOGIN, newPassword);
    }

    @Test
    void changeTrainerPassword_delegatesToTrainerService() {
        // given
        String newPassword = "newSecret1";

        // when
        facade.changeTrainerPassword(LOGIN, newPassword);

        // then
        verify(trainerService).changePassword(LOGIN, newPassword);
    }

    @Test
    void updateTrainer_delegatesToTrainerService() {
        // given
        Trainer changes = new Trainer();
        when(trainerService.updateProfile(LOGIN, changes)).thenReturn(changes);

        // when
        Trainer result = facade.updateTrainer(LOGIN, changes);

        // then
        assertSame(changes, result);
    }

    @Test
    void updateTrainee_delegatesToTraineeService() {
        // given
        Trainee changes = new Trainee();
        when(traineeService.updateProfile(LOGIN, changes)).thenReturn(changes);

        // when
        Trainee result = facade.updateTrainee(LOGIN, changes);

        // then
        assertSame(changes, result);
    }

    @Test
    void activateTrainee_delegatesToTraineeService() {
        // given
        Credentials credentials = LOGIN;

        // when
        facade.activateTrainee(credentials);

        // then
        verify(traineeService).activate(credentials);
    }

    @Test
    void deactivateTrainee_delegatesToTraineeService() {
        // given
        Credentials credentials = LOGIN;

        // when
        facade.deactivateTrainee(credentials);

        // then
        verify(traineeService).deactivate(credentials);
    }

    @Test
    void activateTrainer_delegatesToTrainerService() {
        // given
        Credentials credentials = LOGIN;

        // when
        facade.activateTrainer(credentials);

        // then
        verify(trainerService).activate(credentials);
    }

    @Test
    void deactivateTrainer_delegatesToTrainerService() {
        // given
        Credentials credentials = LOGIN;

        // when
        facade.deactivateTrainer(credentials);

        // then
        verify(trainerService).deactivate(credentials);
    }

    @Test
    void deleteTrainee_delegatesToTraineeService() {
        // given
        Credentials credentials = LOGIN;

        // when
        facade.deleteTrainee(credentials);

        // then
        verify(traineeService).delete(credentials);
    }

    @Test
    void getTraineeTrainings_delegatesToTrainingService() {
        // given
        List<Training> trainings = List.of(new Training());
        when(trainingService.getTraineeTrainings(LOGIN, DATE, DATE, "Nodira.Karimova", "Yoga")).thenReturn(trainings);

        // when
        List<Training> result = facade.getTraineeTrainings(LOGIN, DATE, DATE, "Nodira.Karimova", "Yoga");

        // then
        assertEquals(trainings, result);
    }

    @Test
    void getTrainerTrainings_delegatesToTrainingService() {
        // given
        List<Training> trainings = List.of(new Training());
        when(trainingService.getTrainerTrainings(LOGIN, DATE, DATE, "John.Smith")).thenReturn(trainings);

        // when
        List<Training> result = facade.getTrainerTrainings(LOGIN, DATE, DATE, "John.Smith");

        // then
        assertEquals(trainings, result);
    }

    @Test
    void addTraining_delegatesToTrainingService() {
        // given
        TrainingRequest request = new TrainingRequest("John.Smith", "Nodira.Karimova", "Yoga", "Yoga", DATE, 60);
        Training training = new Training();
        when(trainingService.addTraining(LOGIN, request)).thenReturn(training);

        // when
        Training result = facade.addTraining(LOGIN, request);

        // then
        assertSame(training, result);
    }

    @Test
    void getUnassignedTrainers_delegatesToTraineeService() {
        // given
        List<Trainer> trainers = List.of(new Trainer());
        when(traineeService.getUnassignedTrainers(LOGIN)).thenReturn(trainers);

        // when
        List<Trainer> result = facade.getUnassignedTrainers(LOGIN);

        // then
        assertEquals(trainers, result);
    }

    @Test
    void updateTraineeTrainers_delegatesToTraineeService() {
        // given
        List<Trainer> trainers = List.of(new Trainer());
        when(traineeService.updateTrainers(LOGIN, List.of("Nodira.Karimova"))).thenReturn(trainers);

        // when
        List<Trainer> result = facade.updateTraineeTrainers(LOGIN, List.of("Nodira.Karimova"));

        // then
        assertEquals(trainers, result);
    }

    @Test
    void getTrainingTypes_delegatesToTrainingTypeService() {
        // given
        List<TrainingType> types = List.of(new TrainingType("Yoga"));
        when(trainingTypeService.findAll()).thenReturn(types);

        // when
        List<TrainingType> result = facade.getTrainingTypes();

        // then
        assertEquals(types, result);
    }
}
