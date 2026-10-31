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
import java.util.Optional;

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

    private Trainee jane;
    private Trainer rustam;
    private TrainingType cardio;

    @BeforeEach
    void setUp() {
        TrainingType yoga = trainingTypeDao.findByName("Yoga").orElseThrow();
        cardio = trainingTypeDao.findByName("Cardio").orElseThrow();
        Trainee john = traineeDao.create(trainee("John", "Smith", "John.Smith"));
        jane = traineeDao.create(trainee("Jane", "Doe", "Jane.Doe"));
        Trainer nodira = trainerDao.create(trainer("Nodira", "Karimova", "Nodira.Karimova", yoga));
        rustam = trainerDao.create(trainer("Rustam", "Aliyev", "Rustam.Aliyev", cardio));

        trainingDao.create(training(john, nodira, yoga, "Yoga January", JAN_10));
        trainingDao.create(training(john, rustam, cardio, "Cardio February", FEB_10));
        trainingDao.create(training(john, nodira, yoga, "Yoga March", MAR_10));
        trainingDao.create(training(jane, nodira, yoga, "Jane Yoga", FEB_10));
    }

    @Test
    void findById_existingTraining_returnsIt() {
        // given
        Training created = trainingDao.create(training(jane, rustam, cardio, "Extra", MAR_10));

        // when
        Optional<Training> training = trainingDao.findById(created.getId());

        // then
        assertTrue(training.isPresent());
        assertEquals("Extra", training.get().getTrainingName());
    }

    @Test
    void findById_unknownTraining_returnsEmpty() {
        // given
        Long unknownId = 9999L;

        // when
        Optional<Training> training = trainingDao.findById(unknownId);

        // then
        assertTrue(training.isEmpty());
    }

    @Test
    void findTraineeTrainings_noCriteria_returnsAllTraineeTrainingsSortedByDate() {
        // given
        String traineeUsername = "John.Smith";

        // when
        List<Training> result = trainingDao.findTraineeTrainings(traineeUsername, null, null, null, null);

        // then
        assertEquals(List.of("Yoga January", "Cardio February", "Yoga March"), names(result));
    }

    @Test
    void findTraineeTrainings_dateRange_returnsTrainingsInsideRange() {
        // given
        LocalDate from = FEB_10;
        LocalDate to = MAR_10;

        // when
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", from, to, null, null);

        // then
        assertEquals(List.of("Cardio February", "Yoga March"), names(result));
    }

    @Test
    void findTraineeTrainings_byTrainer_returnsOnlyThatTrainersTrainings() {
        // given
        String trainerUsername = "Nodira.Karimova";

        // when
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", null, null, trainerUsername, null);

        // then
        assertEquals(List.of("Yoga January", "Yoga March"), names(result));
    }

    @Test
    void findTraineeTrainings_byType_returnsOnlyThatType() {
        // given
        String trainingType = "Cardio";

        // when
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", null, null, null, trainingType);

        // then
        assertEquals(List.of("Cardio February"), names(result));
    }

    @Test
    void findTraineeTrainings_allCriteria_combinesThem() {
        // given
        LocalDate from = JAN_10;
        LocalDate to = FEB_10;

        // when
        List<Training> result = trainingDao.findTraineeTrainings("John.Smith", from, to, "Nodira.Karimova", "Yoga");

        // then
        assertEquals(List.of("Yoga January"), names(result));
    }

    @Test
    void findTrainerTrainings_noCriteria_returnsAllTrainerTrainings() {
        // given
        String trainerUsername = "Nodira.Karimova";

        // when
        List<Training> result = trainingDao.findTrainerTrainings(trainerUsername, null, null, null);

        // then
        assertEquals(List.of("Yoga January", "Jane Yoga", "Yoga March"), names(result));
    }

    @Test
    void findTrainerTrainings_byTraineeAndDate_combinesThem() {
        // given
        LocalDate from = FEB_10;

        // when
        List<Training> result = trainingDao.findTrainerTrainings("Nodira.Karimova", from, null, "John.Smith");

        // then
        assertEquals(List.of("Yoga March"), names(result));
    }

    private List<String> names(List<Training> trainings) {
        return trainings.stream().map(Training::getTrainingName).toList();
    }
}
