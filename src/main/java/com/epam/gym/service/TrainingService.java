package com.epam.gym.service;

import com.epam.gym.model.Training;

import java.util.Optional;

public interface TrainingService {
    Training create(Training training);

    Optional<Training> select(Long id);
}
