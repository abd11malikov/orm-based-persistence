package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.model.Training;
import com.epam.gym.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

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

    @Override
    public Training create(Training training) {
        Long traineeId = training.getTraineeId();
        Long trainerId = training.getTrainerId();
        if (traineeId == null || traineeDao.findById(traineeId).isEmpty()) {
            throw new NoSuchElementException("Trainee with id " + traineeId + " not found");
        }
        if (trainerId == null || trainerDao.findById(trainerId).isEmpty()) {
            throw new NoSuchElementException("Trainer with id " + trainerId + " not found");
        }

        Training created = trainingDao.create(training);
        log.info("Created training with id {} for trainee {} and trainer {}",
                created.getTrainingId(), traineeId, trainerId);
        return created;
    }

    @Override
    public Optional<Training> select(Long id) {
        log.info("Selecting training with id {}", id);
        return trainingDao.findById(id);
    }
}
