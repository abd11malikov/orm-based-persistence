package com.epam.gym.dao;

import com.epam.gym.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    Training create(Training training);

    Optional<Training> findById(Long id);

    List<Training> findTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                        String trainerUsername, String trainingTypeName);

    List<Training> findTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                        String traineeUsername);
}
