package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.dto.Credentials;
import com.epam.gym.dto.TrainingRequest;
import com.epam.gym.exception.AuthenticationFailedException;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@TestPropertySource("classpath:application-test.properties")
@Transactional
class GymCrmContextTest {

    @Autowired
    private GymFacade facade;

    private Credentials traineeLogin;
    private Credentials trainerLogin;
    private String traineeUsername;
    private String trainerUsername;

    @BeforeEach
    void setUp() {
        Trainee trainee = facade.createTrainee(new Trainee(new User("Ali", "Valiyev")));
        Trainer trainer = facade.createTrainer(new Trainer(new User("Dilnoza", "Rahimova"), new TrainingType("Cardio")));
        traineeUsername = trainee.getUser().getUsername();
        trainerUsername = trainer.getUser().getUsername();
        traineeLogin = new Credentials(traineeUsername, trainee.getUser().getPassword());
        trainerLogin = new Credentials(trainerUsername, trainer.getUser().getPassword());
    }

    @Test
    void trainingTypesAreLoadedFromDatabase() {
        assertEquals(6, facade.getTrainingTypes().size());
    }

    @Test
    void createTrainee_withSameNameTwice_getsSerialNumberInUsername() {
        Trainee second = facade.createTrainee(new Trainee(new User("Ali", "Valiyev")));

        assertEquals("Ali.Valiyev1", second.getUser().getUsername());
        assertEquals(10, second.getUser().getPassword().length());
        assertTrue(second.getUser().isActive());
    }

    @Test
    void credentialsMatch_checksPasswordAndRole() {
        assertTrue(facade.traineeCredentialsMatch(traineeUsername, traineeLogin.password()));
        assertFalse(facade.traineeCredentialsMatch(traineeUsername, "wrong"));
        assertFalse(facade.traineeCredentialsMatch(trainerUsername, trainerLogin.password()));
        assertTrue(facade.trainerCredentialsMatch(trainerUsername, trainerLogin.password()));
    }

    @Test
    void operationsWithWrongPassword_areRejected() {
        Credentials wrong = new Credentials(traineeUsername, "wrong");

        assertThrows(AuthenticationFailedException.class, () -> facade.selectTrainee(wrong, traineeUsername));
        assertThrows(AuthenticationFailedException.class, () -> facade.deleteTrainee(wrong));
    }

    @Test
    void changePassword_newPasswordWorksOldDoesNot() {
        facade.changeTraineePassword(traineeLogin, "newSecret1");

        assertTrue(facade.traineeCredentialsMatch(traineeUsername, "newSecret1"));
        assertFalse(facade.traineeCredentialsMatch(traineeUsername, traineeLogin.password()));
    }

    @Test
    void updateTrainee_changesProfile() {
        Trainee changes = new Trainee(new User("Ali", "Karimov"));
        changes.setAddress("Bukhara");

        facade.updateTrainee(traineeLogin, changes);

        Trainee selected = facade.selectTrainee(traineeLogin, traineeUsername);
        assertEquals("Karimov", selected.getUser().getLastName());
        assertEquals("Bukhara", selected.getAddress());
        assertEquals(traineeUsername, selected.getUser().getUsername());
    }

    @Test
    void deactivateTwice_secondCallIsRejected() {
        facade.deactivateTrainer(trainerLogin);

        assertFalse(facade.selectTrainer(trainerLogin, trainerUsername).getUser().isActive());
        assertThrows(IllegalStateException.class, () -> facade.deactivateTrainer(trainerLogin));
    }

    @Test
    void addTraining_thenSearchByCriteria() {
        facade.addTraining(traineeLogin, new TrainingRequest(
                traineeUsername, trainerUsername, "Evening Cardio", "Cardio", LocalDate.of(2026, 3, 10), 45));

        assertEquals(1, facade.getTraineeTrainings(traineeLogin, null, null, trainerUsername, "Cardio").size());
        assertEquals(0, facade.getTraineeTrainings(traineeLogin, null, null, null, "Yoga").size());
        assertEquals(1, facade.getTrainerTrainings(trainerLogin, LocalDate.of(2026, 3, 1), null, traineeUsername).size());
    }

    @Test
    void updateTraineeTrainers_changesUnassignedList() {
        assertEquals(1, facade.getUnassignedTrainers(traineeLogin).size());

        facade.updateTraineeTrainers(traineeLogin, List.of(trainerUsername));

        assertEquals(0, facade.getUnassignedTrainers(traineeLogin).size());
    }

    @Test
    void deleteTrainee_removesTraineeAndItsTrainings() {
        facade.addTraining(traineeLogin, new TrainingRequest(
                traineeUsername, trainerUsername, "Evening Cardio", "Cardio", LocalDate.of(2026, 3, 10), 45));

        facade.deleteTrainee(traineeLogin);

        assertFalse(facade.traineeCredentialsMatch(traineeUsername, traineeLogin.password()));
        assertEquals(0, facade.getTrainerTrainings(trainerLogin, null, null, null).size());
        assertThrows(NoSuchElementException.class, () -> facade.selectTrainee(trainerLogin, traineeUsername));
    }
}
