package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GymCrmContextTest {

    @Autowired
    private GymFacade facade;

    @Test
    void contextLoads_andFacadeIsInjected() {
        assertNotNull(facade);
    }

    @Test
    void storageIsInitializedFromFile() {
        assertEquals("John.Doe", facade.selectTrainee(1L).orElseThrow().getUsername());
        assertEquals("Robert.Taylor", facade.selectTrainer(3L).orElseThrow().getUsername());
        assertTrue(facade.selectTraining(1L).isPresent());
    }

    @Test
    void createTrainee_withExistingName_getsSerialNumberInUsername() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("John");
        trainee.setLastName("Doe");

        Trainee created = facade.createTrainee(trainee);

        assertEquals("John.Doe1", created.getUsername());
        assertEquals(10, created.getPassword().length());
        assertTrue(created.isActive());
        assertTrue(facade.selectTrainee(created.getUserId()).isPresent());
    }

    @Test
    void createTraining_forNewTraineeAndTrainer_isStored() {
        Trainee trainee = new Trainee();
        trainee.setFirstName("Ali");
        trainee.setLastName("Valiyev");
        facade.createTrainee(trainee);

        Trainer trainer = new Trainer();
        trainer.setFirstName("Dilnoza");
        trainer.setLastName("Rahimova");
        facade.createTrainer(trainer);

        Training training = new Training();
        training.setTraineeId(trainee.getUserId());
        training.setTrainerId(trainer.getUserId());
        training.setTrainingName("Evening Cardio");
        training.setTrainingDate(LocalDate.now());
        training.setTrainingDuration(45);

        Training created = facade.createTraining(training);

        assertTrue(facade.selectTraining(created.getTrainingId()).isPresent());
    }

    @Test
    void deleteTrainee_removesItFromStorage() {
        assertTrue(facade.deleteTrainee(2L));
        assertTrue(facade.selectTrainee(2L).isEmpty());
    }
}
