package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dto.Credentials;
import com.epam.gym.exception.AuthenticationFailedException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    private static final Credentials LOGIN = new Credentials("John.Smith", "password12");

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    private TraineeServiceImpl traineeService;

    private Trainee john;

    @BeforeEach
    void setUp() {
        traineeService = new TraineeServiceImpl(
                traineeDao, trainerDao, authenticationService, usernameGenerator, passwordGenerator);
        john = trainee("John", "Smith", "John.Smith");
    }

    @Test
    void create_setsGeneratedUsernamePasswordAndActive() {
        // given
        Trainee trainee = new Trainee(new User("John", "Smith"));
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("aB3dE5gH7j");
        when(traineeDao.create(trainee)).thenReturn(trainee);

        // when
        Trainee created = traineeService.create(trainee);

        // then
        assertEquals("John.Smith", created.getUser().getUsername());
        assertEquals("aB3dE5gH7j", created.getUser().getPassword());
        assertTrue(created.getUser().isActive());
        verify(traineeDao).create(trainee);
    }

    @Test
    void create_missingFirstName_throwsExceptionAndSavesNothing() {
        // given
        Trainee trainee = new Trainee(new User(" ", "Smith"));

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.create(trainee));

        // then
        assertEquals("First name is required", exception.getMessage());
        verify(traineeDao, never()).create(any());
    }

    @Test
    void create_withoutUser_throwsException() {
        // given
        Trainee trainee = new Trainee();

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.create(trainee));

        // then
        assertEquals("Trainee user data is required", exception.getMessage());
    }

    @Test
    void credentialsMatch_delegatesToAuthenticationService() {
        // given
        when(authenticationService.traineeCredentialsMatch("John.Smith", "password12")).thenReturn(true);

        // when
        boolean matches = traineeService.credentialsMatch("John.Smith", "password12");

        // then
        assertTrue(matches);
    }

    @Test
    void selectByUsername_authenticatedUser_returnsTrainee() {
        // given
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(john));

        // when
        Trainee result = traineeService.selectByUsername(LOGIN, "John.Smith");

        // then
        assertSame(john, result);
        verify(authenticationService).authenticateUser(LOGIN);
    }

    @Test
    void selectByUsername_unknownTrainee_throwsException() {
        // given
        when(traineeDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        // when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> traineeService.selectByUsername(LOGIN, "Nobody.Here"));

        // then
        assertEquals("Trainee Nobody.Here not found", exception.getMessage());
    }

    @Test
    void selectByUsername_failedAuthentication_doesNotQueryTrainee() {
        // given
        when(authenticationService.authenticateUser(LOGIN)).thenThrow(new AuthenticationFailedException("bad"));

        // when
        assertThrows(AuthenticationFailedException.class, () -> traineeService.selectByUsername(LOGIN, "John.Smith"));

        // then
        verify(traineeDao, never()).findByUsername(any());
    }

    @Test
    void changePassword_setsNewPassword() {
        // given
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        // when
        traineeService.changePassword(LOGIN, "newSecret1");

        // then
        assertEquals("newSecret1", john.getUser().getPassword());
    }

    @Test
    void changePassword_blankNewPassword_throwsExceptionAndKeepsOldPassword() {
        // given
        String newPassword = " ";

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.changePassword(LOGIN, newPassword));

        // then
        assertEquals("New password is required", exception.getMessage());
        assertEquals("password12", john.getUser().getPassword());
    }

    @Test
    void updateProfile_copiesFieldsAndKeepsCredentials() {
        // given
        Trainee changes = new Trainee(new User("Johnny", "Smith"));
        changes.setAddress("Samarkand");
        changes.setDateOfBirth(LocalDate.of(1999, 5, 5));
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        // when
        Trainee updated = traineeService.updateProfile(LOGIN, changes);

        // then
        assertSame(john, updated);
        assertEquals("Johnny", updated.getUser().getFirstName());
        assertEquals("Samarkand", updated.getAddress());
        assertEquals(LocalDate.of(1999, 5, 5), updated.getDateOfBirth());
        assertEquals("John.Smith", updated.getUser().getUsername());
        assertEquals("password12", updated.getUser().getPassword());
    }

    @Test
    void updateProfile_missingLastName_throwsException() {
        // given
        Trainee changes = new Trainee(new User("John", null));

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> traineeService.updateProfile(LOGIN, changes));

        // then
        assertEquals("Last name is required", exception.getMessage());
    }

    @Test
    void deactivate_activeTrainee_makesItInactive() {
        // given
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        // when
        traineeService.deactivate(LOGIN);

        // then
        assertFalse(john.getUser().isActive());
    }

    @Test
    void deactivate_alreadyInactiveTrainee_throwsException() {
        // given
        john.getUser().setActive(false);
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> traineeService.deactivate(LOGIN));

        // then
        assertEquals("Trainee John.Smith is already inactive", exception.getMessage());
    }

    @Test
    void activate_inactiveTrainee_makesItActive() {
        // given
        john.getUser().setActive(false);
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        // when
        traineeService.activate(LOGIN);

        // then
        assertTrue(john.getUser().isActive());
    }

    @Test
    void activate_alreadyActiveTrainee_throwsException() {
        // given
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> traineeService.activate(LOGIN));

        // then
        assertEquals("Trainee John.Smith is already active", exception.getMessage());
    }

    @Test
    void delete_authenticatedTrainee_deletesIt() {
        // given
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        // when
        traineeService.delete(LOGIN);

        // then
        verify(traineeDao).delete(john);
    }

    @Test
    void delete_failedAuthentication_deletesNothing() {
        // given
        when(authenticationService.authenticateTrainee(LOGIN)).thenThrow(new AuthenticationFailedException("bad"));

        // when
        assertThrows(AuthenticationFailedException.class, () -> traineeService.delete(LOGIN));

        // then
        verify(traineeDao, never()).delete(any());
    }

    @Test
    void getUnassignedTrainers_returnsTrainersFromDao() {
        // given
        List<Trainer> trainers = List.of(trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga")));
        when(trainerDao.findNotAssignedToTrainee("John.Smith")).thenReturn(trainers);

        // when
        List<Trainer> result = traineeService.getUnassignedTrainers(LOGIN);

        // then
        assertEquals(trainers, result);
        verify(authenticationService).authenticateTrainee(LOGIN);
    }

    @Test
    void updateTrainers_replacesTraineeTrainers() {
        // given
        Trainer oldTrainer = trainer("Old", "Trainer", "Old.Trainer", new TrainingType("Yoga"));
        Trainer nodira = trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga"));
        john.getTrainers().add(oldTrainer);
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(nodira));

        // when
        List<Trainer> result = traineeService.updateTrainers(LOGIN, List.of("Nodira.Karimova"));

        // then
        assertEquals(List.of(nodira), result);
        assertEquals(1, john.getTrainers().size());
        assertTrue(john.getTrainers().contains(nodira));
    }

    @Test
    void updateTrainers_unknownTrainer_throwsExceptionAndKeepsOldList() {
        // given
        Trainer oldTrainer = trainer("Old", "Trainer", "Old.Trainer", new TrainingType("Yoga"));
        john.getTrainers().add(oldTrainer);
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);
        when(trainerDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        // when
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> traineeService.updateTrainers(LOGIN, List.of("Nobody.Here")));

        // then
        assertEquals("Trainer Nobody.Here not found", exception.getMessage());
        assertTrue(john.getTrainers().contains(oldTrainer));
    }
}
