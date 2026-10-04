package com.epam.gym.service;

import com.epam.gym.model.Trainee;

import java.util.Optional;

public interface TraineeService {
    Trainee create(Trainee trainee);

    Trainee update(Trainee trainee);

    boolean delete(Long id);

    Optional<Trainee> select(Long id);
}
