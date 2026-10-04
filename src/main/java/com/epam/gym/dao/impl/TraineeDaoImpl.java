package com.epam.gym.dao.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.model.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger log = LoggerFactory.getLogger(TraineeDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Trainee create(Trainee trainee) {
        entityManager.persist(trainee);
        log.debug("Created trainee with id {}", trainee.getId());
        return trainee;
    }

    @Override
    public void delete(Trainee trainee) {
        entityManager.remove(trainee);
        log.debug("Deleted trainee with id {}", trainee.getId());
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Optional<Trainee> trainee = Optional.ofNullable(entityManager.find(Trainee.class, id));
        log.debug("Find trainee with id {}: {}", id, trainee.isPresent() ? "found" : "not found");
        return trainee;
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        Optional<Trainee> trainee = entityManager
                .createQuery("SELECT t FROM Trainee t WHERE t.user.username = :username", Trainee.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
        log.debug("Find trainee with username {}: {}", username, trainee.isPresent() ? "found" : "not found");
        return trainee;
    }

    @Override
    public List<Trainee> findAll() {
        List<Trainee> trainees = entityManager
                .createQuery("SELECT t FROM Trainee t", Trainee.class)
                .getResultList();
        log.debug("Found {} trainees", trainees.size());
        return trainees;
    }
}
