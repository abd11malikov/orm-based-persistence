package com.epam.gym.dao;

import com.epam.gym.model.Trainee;

import java.util.List;
import java.util.Optional;

public interface TraineeDao {
    Trainee create(Trainee trainee);

    /**
     * Throws IllegalArgumentException if userId is null,
     * NoSuchElementException if no trainee has this userId.
     */
    Trainee update(Trainee trainee);

    boolean delete(Long id);

    Optional<Trainee> findById(Long id);

    List<Trainee> findAll();
}
