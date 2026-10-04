package com.epam.gym.dao.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.model.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger log = LoggerFactory.getLogger(TrainerDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainer create(Trainer trainer) {
        entityManager.persist(trainer);
        log.debug("Created trainer with id {}", trainer.getId());
        return trainer;
    }

    @Override
    public Optional<Trainer> findById(Long id) {
        Optional<Trainer> trainer = Optional.ofNullable(entityManager.find(Trainer.class, id));
        log.debug("Find trainer with id {}: {}", id, trainer.isPresent() ? "found" : "not found");
        return trainer;
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        Optional<Trainer> trainer = entityManager
                .createQuery("SELECT t FROM Trainer t WHERE t.user.username = :username", Trainer.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
        log.debug("Find trainer with username {}: {}", username, trainer.isPresent() ? "found" : "not found");
        return trainer;
    }

    @Override
    public List<Trainer> findAll() {
        List<Trainer> trainers = entityManager
                .createQuery("SELECT t FROM Trainer t", Trainer.class)
                .getResultList();
        log.debug("Found {} trainers", trainers.size());
        return trainers;
    }

    @Override
    public List<Trainer> findNotAssignedToTrainee(String traineeUsername) {
        List<Trainer> trainers = entityManager
                .createQuery("SELECT tr FROM Trainer tr WHERE tr NOT IN ("
                        + "SELECT assigned FROM Trainee te JOIN te.trainers assigned "
                        + "WHERE te.user.username = :username)", Trainer.class)
                .setParameter("username", traineeUsername)
                .getResultList();
        log.debug("Found {} trainers not assigned to trainee {}", trainers.size(), traineeUsername);
        return trainers;
    }
}
