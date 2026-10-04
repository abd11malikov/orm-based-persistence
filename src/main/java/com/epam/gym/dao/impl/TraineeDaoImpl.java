package com.epam.gym.dao.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.model.Trainee;
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
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private Map<Long, Trainee> trainees;

    @Autowired
    @Qualifier("traineeStorage")
    public void setTrainees(Map<Long, Trainee> trainees) {
        this.trainees = trainees;
    }

    @Override
    public Trainee create(Trainee trainee) {
        Long id = nextId();
        trainee.setUserId(id);
        trainees.put(id, trainee);
        log.debug("Created trainee with id {}", id);
        return trainee;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Long id = trainee.getUserId();
        if (id == null) {
            throw new IllegalArgumentException("Trainee id must not be null for update");
        }
        if (!trainees.containsKey(id)) {
            throw new NoSuchElementException("Trainee with id " + id + " not found");
        }
        trainees.put(id, trainee);
        log.debug("Updated trainee with id {}", id);
        return trainee;
    }

    @Override
    public boolean delete(Long id) {
        boolean removed = trainees.remove(id) != null;
        log.debug("Delete trainee with id {}: {}", id, removed ? "removed" : "not found");
        return removed;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Optional<Trainee> trainee = Optional.ofNullable(trainees.get(id));
        log.debug("Find trainee with id {}: {}", id, trainee.isPresent() ? "found" : "not found");
        return trainee;
    }

    @Override
    public List<Trainee> findAll() {
        List<Trainee> all = trainees.values().stream().toList();
        log.debug("Found {} trainees", all.size());
        return all;
    }

    private Long nextId() {
        return trainees.keySet().stream()
                .max(Long::compare)
                .orElse(0L) + 1;
    }
}
