package com.epam.gym.service;

import com.epam.gym.model.TrainingType;

import java.util.List;
import java.util.Optional;

public interface TrainingTypeService {
    List<TrainingType> findAll();

    Optional<TrainingType> findByName(String trainingTypeName);
}
