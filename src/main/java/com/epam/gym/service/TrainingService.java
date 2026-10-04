package com.epam.gym.service;

import com.epam.gym.dto.Credentials;
import com.epam.gym.dto.TrainingRequest;
import com.epam.gym.model.Training;

import java.time.LocalDate;
import java.util.List;

public interface TrainingService {
    Training addTraining(Credentials credentials, TrainingRequest request);

    List<Training> getTraineeTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                       String trainerUsername, String trainingTypeName);

    List<Training> getTrainerTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                       String traineeUsername);
}
