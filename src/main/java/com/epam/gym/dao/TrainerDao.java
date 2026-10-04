package com.epam.gym.dao;

import com.epam.gym.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    Trainer create(Trainer trainer);

    Trainer update(Trainer trainer);

    Optional<Trainer> findById(Long id);

    List<Trainer> findAll();
}
