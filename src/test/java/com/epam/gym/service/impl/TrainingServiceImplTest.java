package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dto.Credentials;
import com.epam.gym.dto.TrainingRequest;
import com.epam.gym.exception.AuthenticationFailedException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static com.epam.gym.TestEntities.trainee;
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
class TrainingServiceImplTest {

    private static final Credentials TRAINEE_LOGIN = new Credentials("John.Smith", "password12");
    private static final Credentials TRAINER_LOGIN = new Credentials("Nodira.Karimova", "password12");
    private static final Credentials STRANGER_LOGIN = new Credentials("Jane.Doe", "password12");
    private static final LocalDate DATE = LocalDate.of(2026, 3, 10);

    @Mock
    private TrainingDao trainingDao;

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private AuthenticationService authenticationService;

    private TrainingServiceImpl trainingService;

    private final TrainingType yoga = new TrainingType("Yoga");
    private Trainee john;
    private Trainer nodira;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingServiceImpl();
        trainingService.setTrainingDao(trainingDao);
        trainingService.setTraineeDao(traineeDao);
        trainingService.setTrainerDao(trainerDao);
        trainingService.setTrainingTypeDao(trainingTypeDao);
        trainingService.setAuthenticationService(authenticationService);

        john = trainee("John", "Smith", "John.Smith");
        nodira = trainer("Nodira", "Karimova", "Nodira.Karimova", yoga);
    }

    @Test
    void addTraining_byTrainee_savesTrainingAndLinksTrainer() {
        when(authenticationService.authenticateUser(TRAINEE_LOGIN)).thenReturn(john.getUser());
        stubReferences();

        Training training = trainingService.addTraining(TRAINEE_LOGIN, request(60));

        assertSame(john, training.getTrainee());
        assertSame(nodira, training.getTrainer());
        assertSame(yoga, training.getTrainingType());
        assertEquals("Morning Yoga", training.getTrainingName());
        assertEquals(DATE, training.getTrainingDate());
        assertEquals(60, training.getTrainingDuration());
        assertTrue(john.getTrainers().contains(nodira));
        verify(trainingDao).create(training);
    }

    @Test
    void addTraining_byTrainer_isAllowed() {
        when(authenticationService.authenticateUser(TRAINER_LOGIN)).thenReturn(nodira.getUser());
        stubReferences();

        trainingService.addTraining(TRAINER_LOGIN, request(60));

        verify(trainingDao).create(any());
    }

    @Test
    void addTraining_byOtherUser_throwsException() {
        when(authenticationService.authenticateUser(STRANGER_LOGIN))
                .thenReturn(trainee("Jane", "Doe", "Jane.Doe").getUser());

        assertThrows(AuthenticationFailedException.class, () -> trainingService.addTraining(STRANGER_LOGIN, request(60)));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void addTraining_zeroDuration_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(TRAINEE_LOGIN, request(0)));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void addTraining_missingName_throwsException() {
        TrainingRequest request = new TrainingRequest("John.Smith", "Nodira.Karimova", " ", "Yoga", DATE, 60);

        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(TRAINEE_LOGIN, request));
    }

    @Test
    void addTraining_missingDate_throwsException() {
        TrainingRequest request = new TrainingRequest("John.Smith", "Nodira.Karimova", "Morning Yoga", "Yoga", null, 60);

        assertThrows(IllegalArgumentException.class, () -> trainingService.addTraining(TRAINEE_LOGIN, request));
    }

    @Test
    void addTraining_unknownTrainingType_throwsException() {
        when(authenticationService.authenticateUser(TRAINEE_LOGIN)).thenReturn(john.getUser());
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(john));
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(nodira));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainingService.addTraining(TRAINEE_LOGIN, request(60)));
        verify(trainingDao, never()).create(any());
    }

    @Test
    void getTraineeTrainings_authenticatesAndPassesCriteriaToDao() {
        List<Training> trainings = List.of(new Training());
        when(trainingDao.findTraineeTrainings("John.Smith", DATE, null, "Nodira.Karimova", "Yoga"))
                .thenReturn(trainings);

        assertEquals(trainings,
                trainingService.getTraineeTrainings(TRAINEE_LOGIN, DATE, null, "Nodira.Karimova", "Yoga"));
        verify(authenticationService).authenticateTrainee(TRAINEE_LOGIN);
    }

    @Test
    void getTraineeTrainings_failedAuthentication_doesNotQuery() {
        when(authenticationService.authenticateTrainee(TRAINEE_LOGIN))
                .thenThrow(new AuthenticationFailedException("bad"));

        assertThrows(AuthenticationFailedException.class,
                () -> trainingService.getTraineeTrainings(TRAINEE_LOGIN, null, null, null, null));
        verify(trainingDao, never()).findTraineeTrainings(any(), any(), any(), any(), any());
    }

    @Test
    void getTrainerTrainings_authenticatesAndPassesCriteriaToDao() {
        List<Training> trainings = List.of(new Training());
        when(trainingDao.findTrainerTrainings("Nodira.Karimova", null, DATE, "John.Smith")).thenReturn(trainings);

        assertEquals(trainings, trainingService.getTrainerTrainings(TRAINER_LOGIN, null, DATE, "John.Smith"));
        verify(authenticationService).authenticateTrainer(TRAINER_LOGIN);
    }

    private void stubReferences() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(john));
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(nodira));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(yoga));
    }

    private TrainingRequest request(int duration) {
        return new TrainingRequest("John.Smith", "Nodira.Karimova", "Morning Yoga", "Yoga", DATE, duration);
    }
}
