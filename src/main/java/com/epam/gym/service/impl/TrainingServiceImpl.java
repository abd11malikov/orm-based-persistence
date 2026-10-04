package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class TrainingServiceImpl implements TrainingService {
    private static final Logger log = LoggerFactory.getLogger(TrainingServiceImpl.class);

    private TrainingDao trainingDao;
    private TraineeDao traineeDao;
    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;

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

    @Override
    public Training create(Training training) {
        if (training.getTrainee() == null || training.getTrainee().getId() == null) {
            throw new IllegalArgumentException("Training trainee is required");
        }
        if (training.getTrainer() == null || training.getTrainer().getId() == null) {
            throw new IllegalArgumentException("Training trainer is required");
        }
        if (training.getTrainingType() == null) {
            throw new IllegalArgumentException("Training type is required");
        }

        Long traineeId = training.getTrainee().getId();
        Long trainerId = training.getTrainer().getId();
        String typeName = training.getTrainingType().getTrainingTypeName();

        Trainee trainee = traineeDao.findById(traineeId)
                .orElseThrow(() -> new NoSuchElementException("Trainee with id " + traineeId + " not found"));
        Trainer trainer = trainerDao.findById(trainerId)
                .orElseThrow(() -> new NoSuchElementException("Trainer with id " + trainerId + " not found"));
        TrainingType type = trainingTypeDao.findByName(typeName)
                .orElseThrow(() -> new NoSuchElementException("Training type " + typeName + " not found"));

        training.setTrainee(trainee);
        training.setTrainer(trainer);
        training.setTrainingType(type);

        Training created = trainingDao.create(training);
        log.info("Created training with id {} for trainee {} and trainer {}", created.getId(), traineeId, trainerId);
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Training> select(Long id) {
        log.info("Selecting training with id {}", id);
        return trainingDao.findById(id);
    }
}
