package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dto.Credentials;
import com.epam.gym.dto.TrainingRequest;
import com.epam.gym.exception.AuthenticationFailedException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static com.epam.gym.util.ValidationUtils.requireNotBlank;
import static com.epam.gym.util.ValidationUtils.requireNotNull;
import static com.epam.gym.util.ValidationUtils.requirePositive;

@Service
@Transactional
public class TrainingServiceImpl implements TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private AuthenticationService authenticationService;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public Training addTraining(Credentials credentials, TrainingRequest request) {
        validate(request);
        User user = authenticationService.authenticateUser(credentials);
        String username = user.getUsername();
        if (!username.equals(request.traineeUsername()) && !username.equals(request.trainerUsername())) {
            throw new AuthenticationFailedException("Only the trainee or the trainer can add this training");
        }

        Trainee trainee = traineeDao.findByUsername(request.traineeUsername())
                .orElseThrow(() -> new NoSuchElementException("Trainee " + request.traineeUsername() + " not found"));
        Trainer trainer = trainerDao.findByUsername(request.trainerUsername())
                .orElseThrow(() -> new NoSuchElementException("Trainer " + request.trainerUsername() + " not found"));
        TrainingType type = trainingTypeDao.findByName(request.trainingTypeName())
                .orElseThrow(() -> new NoSuchElementException("Training type " + request.trainingTypeName() + " not found"));

        Training training = new Training();
        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(type);
        training.setTrainingName(request.trainingName());
        training.setTrainingDate(request.trainingDate());
        training.setTrainingDuration(request.trainingDuration());
        trainingDao.create(training);

        trainee.getTrainings().add(training);
        trainee.getTrainers().add(trainer);

        log.info("Added training '{}' for trainee {} with trainer {}",
                request.trainingName(), request.traineeUsername(), request.trainerUsername());
        return training;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTraineeTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                              String trainerUsername, String trainingTypeName) {
        authenticationService.authenticateTrainee(credentials);
        List<Training> trainings = trainingDao.findTraineeTrainings(
                credentials.username(), fromDate, toDate, trainerUsername, trainingTypeName);
        log.info("Found {} trainings for trainee {}", trainings.size(), credentials.username());
        return trainings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> getTrainerTrainings(Credentials credentials, LocalDate fromDate, LocalDate toDate,
                                              String traineeUsername) {
        authenticationService.authenticateTrainer(credentials);
        List<Training> trainings = trainingDao.findTrainerTrainings(
                credentials.username(), fromDate, toDate, traineeUsername);
        log.info("Found {} trainings for trainer {}", trainings.size(), credentials.username());
        return trainings;
    }

    private void validate(TrainingRequest request) {
        requireNotNull(request, "Training request");
        requireNotBlank(request.traineeUsername(), "Trainee username");
        requireNotBlank(request.trainerUsername(), "Trainer username");
        requireNotBlank(request.trainingName(), "Training name");
        requireNotBlank(request.trainingTypeName(), "Training type");
        requireNotNull(request.trainingDate(), "Training date");
        requirePositive(request.trainingDuration(), "Training duration");
    }
}
