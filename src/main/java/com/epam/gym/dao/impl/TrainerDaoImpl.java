package com.epam.gym.dao.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.model.Trainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);
    private Map<Long, Trainer> trainers;

    @Autowired
    @Qualifier("trainerStorage")
    public void setTrainers(Map<Long, Trainer> trainers) {
        this.trainers = trainers;
    }

    @Override
    public Trainer create(Trainer trainer) {
        Long id = nextId();
        trainer.setUserId(id);
        trainers.put(id, trainer);
        log.debug("Created trainer with id {}", id);
        return trainer;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Long id = trainer.getUserId();
        if (id == null) {
            throw new IllegalArgumentException("Trainer id must not be null for update");
        }
        if (!trainers.containsKey(id)) {
            throw new NoSuchElementException("Trainer with id " + id + " not found");
        }
        trainers.put(id, trainer);
        log.debug("Updated trainer with id {}", id);
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Optional<Trainer> trainer = Optional.ofNullable(trainers.get(id));
        log.debug("Find trainer with id {}: {}", id, trainer.isPresent() ? "found" : "not found");
        return trainer;
    }

    @Override
    public List<Trainer> findAll() {
        List<Trainer> all = trainers.values().stream().toList();
        log.debug("Found {} trainers", all.size());
        return all;
    }

    private Long nextId() {
        return trainers.keySet().stream()
                .max(Long::compare)
                .orElse(0L) + 1;
    }
}
