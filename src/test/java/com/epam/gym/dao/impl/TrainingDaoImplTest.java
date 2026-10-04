package com.epam.gym.dao.impl;

import com.epam.gym.config.AppConfig;
import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static com.epam.gym.TestEntities.trainee;
import static com.epam.gym.TestEntities.trainer;
import static com.epam.gym.TestEntities.training;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringJUnitConfig(AppConfig.class)
@TestPropertySource("classpath:application-test.properties")
@Transactional
class TrainingDaoImplTest {

    private static final LocalDate JAN_10 = LocalDate.of(2026, 1, 10);
    private static final LocalDate FEB_10 = LocalDate.of(2026, 2, 10);
    private static final LocalDate MAR_10 = LocalDate.of(2026, 3, 10);

    @Autowired
    private TrainingDao trainingDao;

    @Autowired
    private TraineeDao traineeDao;

    @Autowired
    private TrainerDao trainerDao;

    @Autowired
    private TrainingTypeDao trainingTypeDao;

    private Trainee john;
    private Trainee jane;
    private Trainer nodira;
    private Trainer rustam;
    private TrainingType yoga;
    private TrainingType cardio;

    @BeforeEach
    void setUp() {
        yoga = trainingTypeDao.findByName("Yoga").orElseThrow();
        cardio = trainingTypeDao.findByName("Cardio").orElseThrow();
        john = traineeDao.create(trainee("John", "Smith", "John.Smith"));
        jane = traineeDao.create(trainee("Jane", "Doe", "Jane.Doe"));
        nodira = trainerDao.create(trainer("Nodira", "Karimova", "Nodira.Karimova", yoga));
        rustam = trainerDao.create(trainer("Rustam", "Aliyev", "Rustam.Aliyev", cardio));

        trainingDao.create(training(john, nodira, yoga, "Yoga January", JAN_10));
        trainingDao.create(training(john, rustam, cardio, "Cardio February", FEB_10));
        trainingDao.create(training(john, nodira, yoga, "Yoga March", MAR_10));
        trainingDao.create(training(jane, nodira, yoga, "Jane Yoga", FEB_10));
    }

    @Test
    void findById_existingTraining_returnsIt() {
        Training created = trainingDao.create(training(jane, rustam, cardio, "Extra", MAR_10));

        assertEquals("Extra", trainingDao.findById(created.getId()).orElseThrow().getTrainingName());
    }

    @Test
    void findById_unknownTraining_returnsEmpty() {
        assertTrue(trainingDao.findById(9999L).isEmpty());
    }

    @Test
    void findTraineeTrainings_noCriteria_returnsAllTraineeTrainingsSortedByDate() {
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", null, null, null, null);

        assertEquals(List.of("Yoga January", "Cardio February", "Yoga March"), names(result));
    }

    @Test
    void findTraineeTrainings_dateRange_returnsTrainingsInsideRange() {
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", FEB_10, MAR_10, null, null);

        assertEquals(List.of("Cardio February", "Yoga March"), names(result));
    }

    @Test
    void findTraineeTrainings_byTrainer_returnsOnlyThatTrainersTrainings() {
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", null, null, "Nodira.Karimova", null);

        assertEquals(List.of("Yoga January", "Yoga March"), names(result));
    }

    @Test
    void findTraineeTrainings_byType_returnsOnlyThatType() {
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", null, null, null, "Cardio");

        assertEquals(List.of("Cardio February"), names(result));
    }

    @Test
    void findTraineeTrainings_allCriteria_combinesThem() {
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", JAN_10, FEB_10, "Nodira.Karimova", "Yoga");

        assertEquals(List.of("Yoga January"), names(result));
    }

    @Test
    void findTrainerTrainings_noCriteria_returnsAllTrainerTrainings() {
        List<Training> result = trainingDao.findTrainerTrainings("Nodira.Karimova", null, null, null);

        assertEquals(List.of("Yoga January", "Jane Yoga", "Yoga March"), names(result));
    }

    @Test
    void findTrainerTrainings_byTraineeAndDate_combinesThem() {
        List<Training> result = trainingDao.findTrainerTrainings("Nodira.Karimova", FEB_10, null, "John.Smith");

        assertEquals(List.of("Yoga March"), names(result));
    }

    private List<String> names(List<Training> trainings) {
        return trainings.stream().map(Training::getTrainingName).toList();
    }
}
