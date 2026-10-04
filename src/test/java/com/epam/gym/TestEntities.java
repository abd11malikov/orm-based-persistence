package com.epam.gym;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;

import java.time.LocalDate;

public final class TestEntities {

    private TestEntities() {
    }

    public static User user(String firstName, String lastName, String username) {
        User user = new User(firstName, lastName);
        user.setUsername(username);
        user.setPassword("password12");
        user.setActive(true);
        return user;
    }

    public static Trainee trainee(String firstName, String lastName, String username) {
        Trainee trainee = new Trainee(user(firstName, lastName, username));
        trainee.setDateOfBirth(LocalDate.of(2000, 1, 1));
        trainee.setAddress("Tashkent");
        return trainee;
    }

    public static Trainer trainer(String firstName, String lastName, String username, TrainingType specialization) {
        return new Trainer(user(firstName, lastName, username), specialization);
    }

    public static Training training(Trainee trainee, Trainer trainer, TrainingType type, String name, LocalDate date) {
        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(type);
        training.setTrainingName(name);
        training.setTrainingDate(date);
        training.setTrainingDuration(60);
        return training;
    }
}
