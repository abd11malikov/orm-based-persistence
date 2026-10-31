package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.dto.Credentials;
import com.epam.gym.dto.TrainingRequest;
import com.epam.gym.exception.AuthenticationFailedException;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
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

    private static final LocalDate MAR_10 = LocalDate.of(2026, 3, 10);

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
    void getTrainingTypes_returnsTypesFromDatabase() {
        // given
        int seededTypes = 6;

        // when
        List<TrainingType> types = facade.getTrainingTypes();

        // then
        assertEquals(seededTypes, types.size());
    }

    @Test
    void createTrainee_withSameNameTwice_getsSerialNumberInUsername() {
        // given
        Trainee sameName = new Trainee(new User("Ali", "Valiyev"));

        // when
        Trainee second = facade.createTrainee(sameName);

        // then
        assertEquals("Ali.Valiyev1", second.getUser().getUsername());
        assertEquals(10, second.getUser().getPassword().length());
        assertTrue(second.getUser().isActive());
    }

    @Test
    void credentialsMatch_checksPasswordAndRole() {
        // given
        String traineePassword = traineeLogin.password();
        String trainerPassword = trainerLogin.password();

        // when
        boolean traineeMatches = facade.traineeCredentialsMatch(traineeUsername, traineePassword);
        boolean wrongPasswordMatches = facade.traineeCredentialsMatch(traineeUsername, "wrong");
        boolean trainerAsTraineeMatches = facade.traineeCredentialsMatch(trainerUsername, trainerPassword);
        boolean trainerMatches = facade.trainerCredentialsMatch(trainerUsername, trainerPassword);

        // then
        assertTrue(traineeMatches);
        assertFalse(wrongPasswordMatches);
        assertFalse(trainerAsTraineeMatches);
        assertTrue(trainerMatches);
    }

    @Test
    void selectTrainee_wrongPassword_isRejected() {
        // given
        Credentials wrong = new Credentials(traineeUsername, "wrong");

        // when
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class,
                () -> facade.selectTrainee(wrong, traineeUsername));

        // then
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void deleteTrainee_wrongPassword_isRejectedAndKeepsTrainee() {
        // given
        Credentials wrong = new Credentials(traineeUsername, "wrong");

        // when
        assertThrows(AuthenticationFailedException.class, () -> facade.deleteTrainee(wrong));

        // then
        assertTrue(facade.traineeCredentialsMatch(traineeUsername, traineeLogin.password()));
    }

    @Test
    void changeTraineePassword_newPasswordWorksOldDoesNot() {
        // given
        String newPassword = "newSecret1";

        // when
        facade.changeTraineePassword(traineeLogin, newPassword);

        // then
        assertTrue(facade.traineeCredentialsMatch(traineeUsername, newPassword));
        assertFalse(facade.traineeCredentialsMatch(traineeUsername, traineeLogin.password()));
    }

    @Test
    void updateTrainee_changesProfile() {
        // given
        Trainee changes = new Trainee(new User("Ali", "Karimov"));
        changes.setAddress("Bukhara");

        // when
        facade.updateTrainee(traineeLogin, changes);

        // then
        Trainee selected = facade.selectTrainee(traineeLogin, traineeUsername);
        assertEquals("Karimov", selected.getUser().getLastName());
        assertEquals("Bukhara", selected.getAddress());
        assertEquals(traineeUsername, selected.getUser().getUsername());
    }

    @Test
    void deactivateTrainer_twice_secondCallIsRejected() {
        // given
        facade.deactivateTrainer(trainerLogin);

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> facade.deactivateTrainer(trainerLogin));

        // then
        assertEquals("Trainer " + trainerUsername + " is already inactive", exception.getMessage());
        assertFalse(facade.selectTrainer(trainerLogin, trainerUsername).getUser().isActive());
    }

    @Test
    void addTraining_isFoundBySearchCriteria() {
        // given
        TrainingRequest request = new TrainingRequest(
                traineeUsername, trainerUsername, "Evening Cardio", "Cardio", MAR_10, 45);

        // when
        facade.addTraining(traineeLogin, request);

        // then
        List<Training> byTrainerAndType = facade.getTraineeTrainings(traineeLogin, null, null, trainerUsername, "Cardio");
        List<Training> byOtherType = facade.getTraineeTrainings(traineeLogin, null, null, null, "Yoga");
        List<Training> forTrainer = facade.getTrainerTrainings(trainerLogin, LocalDate.of(2026, 3, 1), null, traineeUsername);
        assertEquals(1, byTrainerAndType.size());
        assertEquals(0, byOtherType.size());
        assertEquals(1, forTrainer.size());
    }

    @Test
    void updateTraineeTrainers_removesTrainerFromUnassignedList() {
        // given
        assertEquals(1, facade.getUnassignedTrainers(traineeLogin).size());

        // when
        facade.updateTraineeTrainers(traineeLogin, List.of(trainerUsername));

        // then
        assertEquals(0, facade.getUnassignedTrainers(traineeLogin).size());
    }

    @Test
    void deleteTrainee_removesTraineeAndItsTrainings() {
        // given
        facade.addTraining(traineeLogin, new TrainingRequest(
                traineeUsername, trainerUsername, "Evening Cardio", "Cardio", MAR_10, 45));

        // when
        facade.deleteTrainee(traineeLogin);

        // then
        assertFalse(facade.traineeCredentialsMatch(traineeUsername, traineeLogin.password()));
        assertEquals(0, facade.getTrainerTrainings(trainerLogin, null, null, null).size());
        assertThrows(NoSuchElementException.class, () -> facade.selectTrainee(trainerLogin, traineeUsername));
    }
}
