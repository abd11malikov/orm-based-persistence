package com.epam.gym.dao.impl;

import com.epam.gym.config.AppConfig;
import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.epam.gym.TestEntities.trainee;
import static com.epam.gym.TestEntities.trainer;
import static com.epam.gym.TestEntities.training;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@TestPropertySource("classpath:application-test.properties")
@Transactional
class TraineeDaoImplTest {

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @Autowired
    private UserDao userDao;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void create_savesTraineeAndItsUser() {
        Trainee created = traineeDao.create(trainee("John", "Smith", "John.Smith"));
        entityManager.flush();
        entityManager.clear();

        Trainee loaded = traineeDao.findById(created.getId()).orElseThrow();
        assertNotNull(loaded.getUser().getId());
        assertEquals("John.Smith", loaded.getUser().getUsername());
        assertEquals("Tashkent", loaded.getAddress());
    }

    @Test
    void findByUsername_existingTrainee_returnsIt() {
        traineeDao.create(trainee("John", "Smith", "John.Smith"));

        assertTrue(traineeDao.findByUsername("John.Smith").isPresent());
    }

    @Test
    void findByUsername_unknownTrainee_returnsEmpty() {
        assertTrue(traineeDao.findByUsername("Nobody.Here").isEmpty());
    }

    @Test
    void findAll_returnsAllTrainees() {
        traineeDao.create(trainee("John", "Smith", "John.Smith"));
        traineeDao.create(trainee("Jane", "Smith", "Jane.Smith"));

        assertEquals(2, traineeDao.findAll().size());
    }

    @Test
    void delete_removesTraineeUserTrainingsAndTrainerLinks() {
        TrainingType yoga = trainingTypeDao.findByName("Yoga").orElseThrow();
        Trainer trainer = trainerDao.create(trainer("Nodira", "Karimova", "Nodira.Karimova", yoga));
        Trainee trainee = trainee("John", "Smith", "John.Smith");
        trainee.getTrainers().add(trainer);
        traineeDao.create(trainee);
        Training training = trainingDao.create(training(trainee, trainer, yoga, "Morning Yoga", LocalDate.now()));
        entityManager.flush();
        entityManager.clear();

        traineeDao.delete(traineeDao.findById(trainee.getId()).orElseThrow());
        entityManager.flush();
        entityManager.clear();

        assertTrue(traineeDao.findById(trainee.getId()).isEmpty());
        assertFalse(userDao.existsByUsername("John.Smith"));
        assertNull(entityManager.find(Training.class, training.getId()));
        assertTrue(trainerDao.findById(trainer.getId()).isPresent());
    }
}
