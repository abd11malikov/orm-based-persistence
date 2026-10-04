package com.epam.gym.dao.impl;

import com.epam.gym.config.AppConfig;
import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.epam.gym.TestEntities.trainee;
import static com.epam.gym.TestEntities.trainer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@TestPropertySource("classpath:application-test.properties")
@Transactional
class TrainerDaoImplTest {

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    @PersistenceContext
    private EntityManager entityManager;

    private TrainingType yoga;

    @BeforeEach
    void setUp() {
        yoga = trainingTypeDao.findByName("Yoga").orElseThrow();
    }

    @Test
    void create_savesTrainerWithSpecialization() {
        Trainer created = trainerDao.create(trainer("Nodira", "Karimova", "Nodira.Karimova", yoga));
        entityManager.flush();
        entityManager.clear();

        Trainer loaded = trainerDao.findById(created.getId()).orElseThrow();
        assertEquals("Yoga", loaded.getSpecialization().getTrainingTypeName());
        assertEquals("Nodira.Karimova", loaded.getUser().getUsername());
    }

    @Test
    void findByUsername_existingTrainer_returnsIt() {
        trainerDao.create(trainer("Nodira", "Karimova", "Nodira.Karimova", yoga));

        assertTrue(trainerDao.findByUsername("Nodira.Karimova").isPresent());
    }

    @Test
    void findByUsername_unknownTrainer_returnsEmpty() {
        assertTrue(trainerDao.findByUsername("Nobody.Here").isEmpty());
    }

    @Test
    void findNotAssignedToTrainee_returnsOnlyTrainersNotLinkedToTrainee() {
        Trainer assigned = trainerDao.create(trainer("Nodira", "Karimova", "Nodira.Karimova", yoga));
        Trainer notAssigned = trainerDao.create(trainer("Rustam", "Aliyev", "Rustam.Aliyev", yoga));
        Trainee trainee = trainee("John", "Smith", "John.Smith");
        trainee.getTrainers().add(assigned);
        traineeDao.create(trainee);
        entityManager.flush();

        List<Trainer> result = trainerDao.findNotAssignedToTrainee("John.Smith");

        assertEquals(1, result.size());
        assertEquals(notAssigned.getId(), result.get(0).getId());
    }

    @Test
    void findNotAssignedToTrainee_traineeWithoutTrainers_returnsAllTrainers() {
        trainerDao.create(trainer("Nodira", "Karimova", "Nodira.Karimova", yoga));
        trainerDao.create(trainer("Rustam", "Aliyev", "Rustam.Aliyev", yoga));
        traineeDao.create(trainee("John", "Smith", "John.Smith"));

        assertEquals(2, trainerDao.findNotAssignedToTrainee("John.Smith").size());
    }
}
