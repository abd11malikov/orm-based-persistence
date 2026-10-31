package com.epam.gym.facade;

import com.epam.gym.dto.Credentials;
import com.epam.gym.dto.TrainingRequest;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TraineeService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.service.TrainingService;
import com.epam.gym.service.TrainingTypeService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class GymFacade {
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;
    private final TrainingTypeService trainingTypeService;

    public GymFacade(TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService,
                     TrainingTypeService trainingTypeService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
        this.trainingTypeService = trainingTypeService;
    }

    public Trainer createTrainer(Trainer trainer) {
        return trainerService.create(trainer);
    }

    public Trainee createTrainee(Trainee trainee) {
        return traineeService.create(trainee);
    }

    public boolean traineeCredentialsMatch(String username, String password) {
        return traineeService.credentialsMatch(username, password);
    }

    public boolean trainerCredentialsMatch(String username, String password) {
        return trainerService.credentialsMatch(username, password);
    }

    public Trainer selectTrainer(Credentials credentials, String username) {
        return trainerService.selectByUsername(credentials, username);
    }

    public Trainee selectTrainee(Credentials credentials, String username) {
        return traineeService.selectByUsername(credentials, username);
    }

    public void changeTraineePassword(Credentials credentials, String newPassword) {
        traineeService.changePassword(credentials, newPassword);
    }

    public void changeTrainerPassword(Credentials credentials, String newPassword) {
        trainerService.changePassword(credentials, newPassword);
    }

    public Trainer updateTrainer(Credentials credentials, Trainer changes) {
        return trainerService.updateProfile(credentials, changes);
    }

    public Trainee updateTrainee(Credentials credentials, Trainee changes) {
        return traineeService.updateProfile(credentials, changes);
    }

    public void activateTrainee(Credentials credentials) {
        traineeService.activate(credentials);
    }

    public void deactivateTrainee(Credentials credentials) {
        traineeService.deactivate(credentials);
    }

    public void activateTrainer(Credentials credentials) {
        trainerService.activate(credentials);
    }

    public void deactivateTrainer(Credentials credentials) {
        trainerService.deactivate(credentials);
    }

    public void deleteTrainee(Credentials credentials) {
        traineeService.delete(credentials);
    }

    public List<Training> getTraineeTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                              String trainerUsername, String trainingTypeName) {
        return trainingService.getTraineeTrainings(credentials, fromDate, toDate, trainerUsername, trainingTypeName);
    }

    public List<Training> getTrainerTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                              String traineeUsername) {
        return trainingService.getTrainerTrainings(credentials, fromDate, toDate, traineeUsername);
    }

    public Training addTraining(Credentials credentials, TrainingRequest request) {
        return trainingService.addTraining(credentials, request);
    }

    public List<Trainer> getUnassignedTrainers(Credentials credentials) {
        return traineeService.getUnassignedTrainers(credentials);
    }

    public List<Trainer> updateTraineeTrainers(Credentials credentials, List<String> trainerUsernames) {
        return traineeService.updateTrainers(credentials, trainerUsernames);
    }

    public List<TrainingType> getTrainingTypes() {
        return trainingTypeService.findAll();
    }
}
