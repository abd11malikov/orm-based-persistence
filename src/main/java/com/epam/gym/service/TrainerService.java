package com.epam.gym.service;

import com.epam.gym.model.Trainer;

import java.util.Optional;

public interface TrainerService {
    Trainer create(Trainer trainer);

    Trainer update(Trainer trainer);

    Optional<Trainer> select(Long id);
}
