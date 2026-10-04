package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.dto.Credentials;
import com.epam.gym.exception.AuthenticationFailedException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.epam.gym.TestEntities.trainee;
import static com.epam.gym.TestEntities.trainer;
import static com.epam.gym.TestEntities.user;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UserDao userDao;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl();
        authenticationService.setTraineeDao(traineeDao);
        authenticationService.setTrainerDao(trainerDao);
        authenticationService.setUserDao(userDao);
    }

    @Test
    void traineeCredentialsMatch_correctPassword_returnsTrue() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee("John", "Smith", "John.Smith")));

        assertTrue(authenticationService.traineeCredentialsMatch("John.Smith", "password12"));
    }

    @Test
    void traineeCredentialsMatch_wrongPassword_returnsFalse() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee("John", "Smith", "John.Smith")));

        assertFalse(authenticationService.traineeCredentialsMatch("John.Smith", "wrong"));
    }

    @Test
    void traineeCredentialsMatch_unknownUser_returnsFalse() {
        when(traineeDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        assertFalse(authenticationService.traineeCredentialsMatch("Nobody.Here", "password12"));
    }

    @Test
    void trainerCredentialsMatch_correctPassword_returnsTrue() {
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga"));
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(trainer));

        assertTrue(authenticationService.trainerCredentialsMatch("Nodira.Karimova", "password12"));
    }

    @Test
    void authenticateTrainee_correctCredentials_returnsTrainee() {
        Trainee trainee = trainee("John", "Smith", "John.Smith");
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        assertSame(trainee, authenticationService.authenticateTrainee(new Credentials("John.Smith", "password12")));
    }

    @Test
    void authenticateTrainee_wrongPassword_throwsException() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee("John", "Smith", "John.Smith")));

        assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateTrainee(new Credentials("John.Smith", "wrong")));
    }

    @Test
    void authenticateTrainee_trainerUsername_throwsException() {
        when(traineeDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateTrainee(new Credentials("Nodira.Karimova", "password12")));
    }

    @Test
    void authenticateTrainee_nullCredentials_throwsException() {
        assertThrows(AuthenticationFailedException.class, () -> authenticationService.authenticateTrainee(null));
    }

    @Test
    void authenticateTrainer_correctCredentials_returnsTrainer() {
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga"));
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(trainer));

        assertSame(trainer, authenticationService.authenticateTrainer(new Credentials("Nodira.Karimova", "password12")));
    }

    @Test
    void authenticateTrainer_wrongPassword_throwsException() {
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga"));
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(trainer));

        assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateTrainer(new Credentials("Nodira.Karimova", "wrong")));
    }

    @Test
    void authenticateUser_correctCredentials_returnsUser() {
        User user = user("John", "Smith", "John.Smith");
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        assertSame(user, authenticationService.authenticateUser(new Credentials("John.Smith", "password12")));
    }

    @Test
    void authenticateUser_unknownUser_throwsException() {
        when(userDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateUser(new Credentials("Nobody.Here", "password12")));
    }
}
