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
import java.util.List;
import java.util.Optional;

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
        // given
        Trainee trainee = trainee("John", "Smith", "John.Smith");

        // when
        Trainee created = traineeDao.create(trainee);
        entityManager.flush();
        entityManager.clear();

        // then
        Trainee loaded = traineeDao.findById(created.getId()).orElseThrow();
        assertNotNull(loaded.getUser().getId());
        assertEquals("John.Smith", loaded.getUser().getUsername());
        assertEquals("Tashkent", loaded.getAddress());
    }

    @Test
    void findByUsername_existingTrainee_returnsIt() {
        // given
        traineeDao.create(trainee("John", "Smith", "John.Smith"));

        // when
        Optional<Trainee> trainee = traineeDao.findByUsername("John.Smith");

        // then
        assertTrue(trainee.isPresent());
    }

    @Test
    void findByUsername_unknownTrainee_returnsEmpty() {
        // given
        String username = "Nobody.Here";

        // when
        Optional<Trainee> trainee = traineeDao.findByUsername(username);

        // then
        assertTrue(trainee.isEmpty());
    }

    @Test
    void findAll_returnsAllTrainees() {
        // given
        traineeDao.create(trainee("John", "Smith", "John.Smith"));
        traineeDao.create(trainee("Jane", "Smith", "Jane.Smith"));

        // when
        List<Trainee> trainees = traineeDao.findAll();

        // then
        assertEquals(2, trainees.size());
    }

    @Test
    void delete_removesTraineeUserTrainingsAndTrainerLinks() {
        // given
        TrainingType yoga = trainingTypeDao.findByName("Yoga").orElseThrow();
        Trainer trainer = trainerDao.create(trainer("Nodira", "Karimova", "Nodira.Karimova", yoga));
        Trainee trainee = trainee("John", "Smith", "John.Smith");
        trainee.getTrainers().add(trainer);
        traineeDao.create(trainee);
        Training training = trainingDao.create(training(trainee, trainer, yoga, "Morning Yoga", LocalDate.now()));
        entityManager.flush();
        entityManager.clear();

        // when
        traineeDao.delete(traineeDao.findById(trainee.getId()).orElseThrow());
        entityManager.flush();
        entityManager.clear();

        // then
        assertTrue(traineeDao.findById(trainee.getId()).isEmpty());
        assertFalse(userDao.existsByUsername("John.Smith"));
        assertNull(entityManager.find(Training.class, training.getId()));
        assertTrue(trainerDao.findById(trainer.getId()).isPresent());
    }
}
