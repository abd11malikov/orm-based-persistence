package com.epam.gym.service;

import com.epam.gym.dto.Credentials;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;

import java.util.List;

public interface TraineeService {
    Trainee create(Trainee trainee);

    boolean credentialsMatch(String username, String password);

    Trainee selectByUsername(Credentials credentials, String username);

    void changePassword(Credentials credentials, String newPassword);

    Trainee updateProfile(Credentials credentials, Trainee changes);

    void activate(Credentials credentials);

    void deactivate(Credentials credentials);

    void delete(Credentials credentials);

    List<Trainer> getUnassignedTrainers(Credentials credentials);

    List<Trainer> updateTrainers(Credentials credentials, List<String> trainerUsernames);
}
