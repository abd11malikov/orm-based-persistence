package com.epam.gym.model;

public class Trainer extends User {
    private TrainingType specialization;

    public TrainingType getSpecialization() {
        return specialization;
    }

    public void setSpecialization(TrainingType specialization) {
        this.specialization = specialization;
    }

    @Override
    public String toString() {
        return "Trainer{" +
                "specialization=" + specialization +
                ", " + super.toString() +
                '}';
    }
}
