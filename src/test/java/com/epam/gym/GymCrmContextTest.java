package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@TestPropertySource("classpath:application-test.properties")
@Transactional
class GymCrmContextTest {

    @Autowired
    private GymFacade facade;

    @Test
    void contextLoads_andFacadeIsInjected() {
        assertNotNull(facade);
    }

    @Test
    void trainingTypesAreLoadedFromDatabase() {
        assertEquals(6, facade.getTrainingTypes().size());
    }

    @Test
    void createTrainee_withSameNameTwice_getsSerialNumberInUsername() {
        facade.createTrainee(new Trainee(new User("John", "Doe")));
        Trainee second = facade.createTrainee(new Trainee(new User("John", "Doe")));

        assertEquals("John.Doe1", second.getUser().getUsername());
        assertEquals(10, second.getUser().getPassword().length());
        assertTrue(second.getUser().isActive());
    }

    @Test
    void createTraining_forNewTraineeAndTrainer_isStored() {
        Trainee trainee = facade.createTrainee(new Trainee(new User("Ali", "Valiyev")));
        Trainer trainer = facade.createTrainer(new Trainer(new User("Dilnoza", "Rahimova"), new TrainingType("Cardio")));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(new TrainingType("Cardio"));
        training.setTrainingName("Evening Cardio");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(45);
        Training created = facade.createTraining(training);

        assertTrue(facade.selectTraining(created.getId()).isPresent());
    }

    @Test
    void deleteTrainee_removesItFromDatabase() {
        Trainee trainee = facade.createTrainee(new Trainee(new User("Ali", "Valiyev")));

        assertTrue(facade.deleteTrainee(trainee.getId()));
        assertTrue(facade.selectTrainee(trainee.getId()).isEmpty());
    }
}
