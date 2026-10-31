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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    private static final String INVALID_CREDENTIALS = "Invalid username or password";

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private UserDao userDao;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationServiceImpl(traineeDao, trainerDao, userDao);
    }

    @Test
    void traineeCredentialsMatch_correctPassword_returnsTrue() {
        // given
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee("John", "Smith", "John.Smith")));

        // when
        boolean matches = authenticationService.traineeCredentialsMatch("John.Smith", "password12");

        // then
        assertTrue(matches);
    }

    @Test
    void traineeCredentialsMatch_wrongPassword_returnsFalse() {
        // given
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee("John", "Smith", "John.Smith")));

        // when
        boolean matches = authenticationService.traineeCredentialsMatch("John.Smith", "wrong");

        // then
        assertFalse(matches);
    }

    @Test
    void traineeCredentialsMatch_unknownUser_returnsFalse() {
        // given
        when(traineeDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        // when
        boolean matches = authenticationService.traineeCredentialsMatch("Nobody.Here", "password12");

        // then
        assertFalse(matches);
    }

    @Test
    void trainerCredentialsMatch_correctPassword_returnsTrue() {
        // given
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga"));
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(trainer));

        // when
        boolean matches = authenticationService.trainerCredentialsMatch("Nodira.Karimova", "password12");

        // then
        assertTrue(matches);
    }

    @Test
    void authenticateTrainee_correctCredentials_returnsTrainee() {
        // given
        Trainee trainee = trainee("John", "Smith", "John.Smith");
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee));

        // when
        Trainee result = authenticationService.authenticateTrainee(new Credentials("John.Smith", "password12"));

        // then
        assertSame(trainee, result);
    }

    @Test
    void authenticateTrainee_wrongPassword_throwsException() {
        // given
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(trainee("John", "Smith", "John.Smith")));
        Credentials credentials = new Credentials("John.Smith", "wrong");

        // when
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateTrainee(credentials));

        // then
        assertEquals(INVALID_CREDENTIALS, exception.getMessage());
    }

    @Test
    void authenticateTrainee_trainerUsername_throwsException() {
        // given
        when(traineeDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.empty());
        Credentials credentials = new Credentials("Nodira.Karimova", "password12");

        // when
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateTrainee(credentials));

        // then
        assertEquals(INVALID_CREDENTIALS, exception.getMessage());
    }

    @Test
    void authenticateTrainee_nullCredentials_throwsException() {
        // given
        Credentials credentials = null;

        // when
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateTrainee(credentials));

        // then
        assertEquals(INVALID_CREDENTIALS, exception.getMessage());
    }

    @Test
    void authenticateTrainer_correctCredentials_returnsTrainer() {
        // given
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga"));
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(trainer));

        // when
        Trainer result = authenticationService.authenticateTrainer(new Credentials("Nodira.Karimova", "password12"));

        // then
        assertSame(trainer, result);
    }

    @Test
    void authenticateTrainer_wrongPassword_throwsException() {
        // given
        Trainer trainer = trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga"));
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(trainer));
        Credentials credentials = new Credentials("Nodira.Karimova", "wrong");

        // when
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateTrainer(credentials));

        // then
        assertEquals(INVALID_CREDENTIALS, exception.getMessage());
    }

    @Test
    void authenticateUser_correctCredentials_returnsUser() {
        // given
        User user = user("John", "Smith", "John.Smith");
        when(userDao.findByUsername("John.Smith")).thenReturn(Optional.of(user));

        // when
        User result = authenticationService.authenticateUser(new Credentials("John.Smith", "password12"));

        // then
        assertSame(user, result);
    }

    @Test
    void authenticateUser_unknownUser_throwsException() {
        // given
        when(userDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());
        Credentials credentials = new Credentials("Nobody.Here", "password12");

        // when
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> authenticationService.authenticateUser(credentials));

        // then
        assertEquals(INVALID_CREDENTIALS, exception.getMessage());
    }
}
