package com.epam.gym.service;

import com.epam.gym.dto.Credentials;
import com.epam.gym.model.Trainer;

public interface TrainerService {
    Trainer create(Trainer trainer);

    boolean credentialsMatch(String username, String password);

    Trainer selectByUsername(Credentials credentials, String username);

    void changePassword(Credentials credentials, String newPassword);

    Trainer updateProfile(Credentials credentials, Trainer changes);

    void activate(Credentials credentials);

    void deactivate(Credentials credentials);
}
