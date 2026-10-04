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
        traineeService = new TraineeServiceImpl();
        traineeService.setTraineeDao(traineeDao);
        traineeService.setTrainerDao(trainerDao);
        traineeService.setAuthenticationService(authenticationService);
        traineeService.setUsernameGenerator(usernameGenerator);
        traineeService.setPasswordGenerator(passwordGenerator);

        john = trainee("John", "Smith", "John.Smith");
    }

    @Test
    void create_setsGeneratedUsernamePasswordAndActive() {
        Trainee trainee = new Trainee(new User("John", "Smith"));
        when(usernameGenerator.generate("John", "Smith")).thenReturn("John.Smith");
        when(passwordGenerator.generate()).thenReturn("aB3dE5gH7j");
        when(traineeDao.create(trainee)).thenReturn(trainee);

        Trainee created = traineeService.create(trainee);

        assertEquals("John.Smith", created.getUser().getUsername());
        assertEquals("aB3dE5gH7j", created.getUser().getPassword());
        assertTrue(created.getUser().isActive());
        verify(traineeDao).create(trainee);
    }

    @Test
    void create_missingFirstName_throwsExceptionAndSavesNothing() {
        Trainee trainee = new Trainee(new User(" ", "Smith"));

        assertThrows(IllegalArgumentException.class, () -> traineeService.create(trainee));
        verify(traineeDao, never()).create(any());
    }

    @Test
    void create_withoutUser_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.create(new Trainee()));
    }

    @Test
    void credentialsMatch_delegatesToAuthenticationService() {
        when(authenticationService.traineeCredentialsMatch("John.Smith", "password12")).thenReturn(true);

        assertTrue(traineeService.credentialsMatch("John.Smith", "password12"));
    }

    @Test
    void selectByUsername_authenticatedUser_returnsTrainee() {
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(john));

        assertSame(john, traineeService.selectByUsername(LOGIN, "John.Smith"));
        verify(authenticationService).authenticateUser(LOGIN);
    }

    @Test
    void selectByUsername_unknownTrainee_throwsException() {
        when(traineeDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> traineeService.selectByUsername(LOGIN, "Nobody.Here"));
    }

    @Test
    void selectByUsername_failedAuthentication_doesNotQueryTrainee() {
        when(authenticationService.authenticateUser(LOGIN)).thenThrow(new AuthenticationFailedException("bad"));

        assertThrows(AuthenticationFailedException.class, () -> traineeService.selectByUsername(LOGIN, "John.Smith"));
        verify(traineeDao, never()).findByUsername(any());
    }

    @Test
    void changePassword_setsNewPassword() {
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        traineeService.changePassword(LOGIN, "newSecret1");

        assertEquals("newSecret1", john.getUser().getPassword());
    }

    @Test
    void changePassword_blankNewPassword_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> traineeService.changePassword(LOGIN, " "));
        assertEquals("password12", john.getUser().getPassword());
    }

    @Test
    void updateProfile_copiesFieldsAndKeepsCredentials() {
        Trainee changes = new Trainee(new User("Johnny", "Smith"));
        changes.setAddress("Samarkand");
        changes.setDateOfBirth(LocalDate.of(1999, 5, 5));
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        Trainee updated = traineeService.updateProfile(LOGIN, changes);

        assertSame(john, updated);
        assertEquals("Johnny", updated.getUser().getFirstName());
        assertEquals("Samarkand", updated.getAddress());
        assertEquals(LocalDate.of(1999, 5, 5), updated.getDateOfBirth());
        assertEquals("John.Smith", updated.getUser().getUsername());
        assertEquals("password12", updated.getUser().getPassword());
    }

    @Test
    void updateProfile_missingLastName_throwsException() {
        Trainee changes = new Trainee(new User("John", null));

        assertThrows(IllegalArgumentException.class, () -> traineeService.updateProfile(LOGIN, changes));
    }

    @Test
    void deactivate_activeTrainee_makesItInactive() {
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        traineeService.deactivate(LOGIN);

        assertFalse(john.getUser().isActive());
    }

    @Test
    void deactivate_alreadyInactiveTrainee_throwsException() {
        john.getUser().setActive(false);
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        assertThrows(IllegalStateException.class, () -> traineeService.deactivate(LOGIN));
    }

    @Test
    void activate_inactiveTrainee_makesItActive() {
        john.getUser().setActive(false);
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        traineeService.activate(LOGIN);

        assertTrue(john.getUser().isActive());
    }

    @Test
    void activate_alreadyActiveTrainee_throwsException() {
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        assertThrows(IllegalStateException.class, () -> traineeService.activate(LOGIN));
    }

    @Test
    void delete_authenticatedTrainee_deletesIt() {
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);

        traineeService.delete(LOGIN);

        verify(traineeDao).delete(john);
    }

    @Test
    void delete_failedAuthentication_deletesNothing() {
        when(authenticationService.authenticateTrainee(LOGIN)).thenThrow(new AuthenticationFailedException("bad"));

        assertThrows(AuthenticationFailedException.class, () -> traineeService.delete(LOGIN));
        verify(traineeDao, never()).delete(any());
    }

    @Test
    void getUnassignedTrainers_returnsTrainersFromDao() {
        List<Trainer> trainers = List.of(trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga")));
        when(trainerDao.findNotAssignedToTrainee("John.Smith")).thenReturn(trainers);

        assertEquals(trainers, traineeService.getUnassignedTrainers(LOGIN));
        verify(authenticationService).authenticateTrainee(LOGIN);
    }

    @Test
    void updateTrainers_replacesTraineeTrainers() {
        Trainer oldTrainer = trainer("Old", "Trainer", "Old.Trainer", new TrainingType("Yoga"));
        Trainer nodira = trainer("Nodira", "Karimova", "Nodira.Karimova", new TrainingType("Yoga"));
        john.getTrainers().add(oldTrainer);
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(nodira));

        List<Trainer> result = traineeService.updateTrainers(LOGIN, List.of("Nodira.Karimova"));

        assertEquals(List.of(nodira), result);
        assertEquals(1, john.getTrainers().size());
        assertTrue(john.getTrainers().contains(nodira));
    }

    @Test
    void updateTrainers_unknownTrainer_throwsExceptionAndKeepsOldList() {
        Trainer oldTrainer = trainer("Old", "Trainer", "Old.Trainer", new TrainingType("Yoga"));
        john.getTrainers().add(oldTrainer);
        when(authenticationService.authenticateTrainee(LOGIN)).thenReturn(john);
        when(trainerDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> traineeService.updateTrainers(LOGIN, List.of("Nobody.Here")));
        assertTrue(john.getTrainers().contains(oldTrainer));
    }
}
