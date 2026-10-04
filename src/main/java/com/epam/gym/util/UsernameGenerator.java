package com.epam.gym.util;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class UsernameGenerator {
    private static final Logger log = LoggerFactory.getLogger(UsernameGenerator.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    public String generate(String firstName, String lastName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name must not be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name must not be empty");
        }

        String baseUsername = firstName.trim() + "." + lastName.trim();
        Set<String> existingUsernames = getExistingUsernames();

        String username = baseUsername;
        int serialNumber = 1;
        while (existingUsernames.contains(username)) {
            username = baseUsername + serialNumber;
            serialNumber++;
        }

        log.debug("Generated username {} for {} {}", username, firstName, lastName);
        return username;
    }

    private Set<String> getExistingUsernames() {
        Set<String> usernames = new HashSet<>();
        for (Trainee trainee : traineeDao.findAll()) {
            usernames.add(trainee.getUsername());
        }
        for (Trainer trainer : trainerDao.findAll()) {
            usernames.add(trainer.getUsername());
        }
        return usernames;
    }
}
