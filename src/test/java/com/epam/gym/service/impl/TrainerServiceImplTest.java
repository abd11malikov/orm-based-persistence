package com.epam.gym.service.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dto.Credentials;
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

import java.util.NoSuchElementException;
import java.util.Optional;

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
class TrainerServiceImplTest {

    private static final Credentials LOGIN = new Credentials("Nodira.Karimova", "password12");

    @Mock
    private TrainerDao trainerDao;

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private UsernameGenerator usernameGenerator;

    @Mock
    private PasswordGenerator passwordGenerator;

    private TrainerServiceImpl trainerService;

    private final TrainingType yoga = new TrainingType("Yoga");
    private final TrainingType cardio = new TrainingType("Cardio");
    private Trainer nodira;

    @BeforeEach
    void setUp() {
        trainerService = new TrainerServiceImpl();
        trainerService.setTrainerDao(trainerDao);
        trainerService.setTrainingTypeDao(trainingTypeDao);
        trainerService.setAuthenticationService(authenticationService);
        trainerService.setUsernameGenerator(usernameGenerator);
        trainerService.setPasswordGenerator(passwordGenerator);

        nodira = trainer("Nodira", "Karimova", "Nodira.Karimova", yoga);
    }

    @Test
    void create_setsCredentialsAndLoadsSpecializationFromDatabase() {
        TrainingType yogaFromDb = new TrainingType("Yoga");
        Trainer trainer = new Trainer(new User("Nodira", "Karimova"), new TrainingType("Yoga"));
        when(trainingTypeDao.findByName("Yoga")).thenReturn(Optional.of(yogaFromDb));
        when(usernameGenerator.generate("Nodira", "Karimova")).thenReturn("Nodira.Karimova");
        when(passwordGenerator.generate()).thenReturn("sT7uV1wX3y");
        when(trainerDao.create(trainer)).thenReturn(trainer);

        Trainer created = trainerService.create(trainer);

        assertSame(yogaFromDb, created.getSpecialization());
        assertEquals("Nodira.Karimova", created.getUser().getUsername());
        assertEquals("sT7uV1wX3y", created.getUser().getPassword());
        assertTrue(created.getUser().isActive());
    }

    @Test
    void create_unknownSpecialization_throwsException() {
        Trainer trainer = new Trainer(new User("Nodira", "Karimova"), new TrainingType("Swimming"));
        when(trainingTypeDao.findByName("Swimming")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainerService.create(trainer));
        verify(trainerDao, never()).create(any());
    }

    @Test
    void create_withoutSpecialization_throwsException() {
        Trainer trainer = new Trainer(new User("Nodira", "Karimova"), null);

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(trainer));
        verify(trainerDao, never()).create(any());
    }

    @Test
    void create_missingLastName_throwsException() {
        Trainer trainer = new Trainer(new User("Nodira", ""), yoga);

        assertThrows(IllegalArgumentException.class, () -> trainerService.create(trainer));
    }

    @Test
    void credentialsMatch_delegatesToAuthenticationService() {
        when(authenticationService.trainerCredentialsMatch("Nodira.Karimova", "wrong")).thenReturn(false);

        assertFalse(trainerService.credentialsMatch("Nodira.Karimova", "wrong"));
    }

    @Test
    void selectByUsername_authenticatedUser_returnsTrainer() {
        when(trainerDao.findByUsername("Nodira.Karimova")).thenReturn(Optional.of(nodira));

        assertSame(nodira, trainerService.selectByUsername(LOGIN, "Nodira.Karimova"));
        verify(authenticationService).authenticateUser(LOGIN);
    }

    @Test
    void selectByUsername_unknownTrainer_throwsException() {
        when(trainerDao.findByUsername("Nobody.Here")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> trainerService.selectByUsername(LOGIN, "Nobody.Here"));
    }

    @Test
    void changePassword_setsNewPassword() {
        when(authenticationService.authenticateTrainer(LOGIN)).thenReturn(nodira);

        trainerService.changePassword(LOGIN, "newSecret1");

        assertEquals("newSecret1", nodira.getUser().getPassword());
    }

    @Test
    void updateProfile_copiesFieldsAndKeepsCredentials() {
        Trainer changes = new Trainer(new User("Nodira", "Aliyeva"), new TrainingType("Cardio"));
        when(authenticationService.authenticateTrainer(LOGIN)).thenReturn(nodira);
        when(trainingTypeDao.findByName("Cardio")).thenReturn(Optional.of(cardio));

        Trainer updated = trainerService.updateProfile(LOGIN, changes);

        assertSame(nodira, updated);
        assertEquals("Aliyeva", updated.getUser().getLastName());
        assertSame(cardio, updated.getSpecialization());
        assertEquals("Nodira.Karimova", updated.getUser().getUsername());
        assertEquals("password12", updated.getUser().getPassword());
    }

    @Test
    void deactivate_activeTrainer_makesItInactive() {
        when(authenticationService.authenticateTrainer(LOGIN)).thenReturn(nodira);

        trainerService.deactivate(LOGIN);

        assertFalse(nodira.getUser().isActive());
    }

    @Test
    void deactivate_alreadyInactiveTrainer_throwsException() {
        nodira.getUser().setActive(false);
        when(authenticationService.authenticateTrainer(LOGIN)).thenReturn(nodira);

        assertThrows(IllegalStateException.class, () -> trainerService.deactivate(LOGIN));
    }

    @Test
    void activate_inactiveTrainer_makesItActive() {
        nodira.getUser().setActive(false);
        when(authenticationService.authenticateTrainer(LOGIN)).thenReturn(nodira);

        trainerService.activate(LOGIN);

        assertTrue(nodira.getUser().isActive());
    }

    @Test
    void activate_alreadyActiveTrainer_throwsException() {
        when(authenticationService.authenticateTrainer(LOGIN)).thenReturn(nodira);

        assertThrows(IllegalStateException.class, () -> trainerService.activate(LOGIN));
    }
}
