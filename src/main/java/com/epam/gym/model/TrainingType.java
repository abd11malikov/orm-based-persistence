package com.epam.gym.model;

import java.util.Objects;

public class TrainingType {
    private String trainingTypeName;

    public String getTrainingTypeName() {
        return trainingTypeName;
    }

    public void setTrainingTypeName(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    @Override
    public String toString() {
        return "TrainingType{" +
                "trainingTypeName='" + trainingTypeName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TrainingType that = (TrainingType) o;
        return Objects.equals(trainingTypeName, that.getTrainingTypeName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(trainingTypeName);
    }
}
