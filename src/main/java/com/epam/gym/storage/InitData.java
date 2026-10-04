package com.epam.gym.storage;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;

import java.util.List;

public class InitData {
    private List<Trainee> trainees;
    private List<Trainer> trainers;
    private List<Training> trainings;

    public List<Trainee> getTrainees() {
        return trainees;
    }

    public void setTrainees(List<Trainee> trainees) {
        this.trainees = trainees;
    }

    public List<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<Trainer> trainers) {
        this.trainers = trainers;
    }

    public List<Training> getTrainings() {
        return trainings;
    }

    public void setTrainings(List<Training> trainings) {
        this.trainings = trainings;
    }

    @Override
    public String toString() {
        return "InitData{" +
                "trainees=" + trainees +
                ", trainers=" + trainers +
                ", trainings=" + trainings +
                '}';
    }
}
