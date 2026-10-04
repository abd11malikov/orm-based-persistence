package com.epam.gym.dao.impl;

import com.epam.gym.dao.TrainingDao;
import com.epam.gym.model.Training;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);
    private Map<Long, Training> trainings;

    @Autowired
    @Qualifier("trainingStorage")
    public void setTrainings(Map<Long, Training> trainings) {
        this.trainings = trainings;
    }

    @Override
    public Training create(Training training) {
        Long id = nextId();
        training.setTrainingId(id);
        trainings.put(id, training);
        log.debug("Created training with id {}", id);
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Optional<Training> training = Optional.ofNullable(trainings.get(id));
        log.debug("Find training with id {}: {}", id, training.isPresent() ? "found" : "not found");
        return training;
    }

    @Override
    public List<Training> findAll() {
        List<Training> all = trainings.values().stream().toList();
        log.debug("Found {} trainings", all.size());
        return all;
    }

    private Long nextId() {
        return trainings.keySet().stream()
                .max(Long::compare)
                .orElse(0L) + 1;
    }
}
