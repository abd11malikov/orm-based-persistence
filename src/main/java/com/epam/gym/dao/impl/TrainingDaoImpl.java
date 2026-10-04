package com.epam.gym.dao.impl;

import com.epam.gym.dao.TrainingDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger log = LoggerFactory.getLogger(TrainingDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Training create(Training training) {
        entityManager.persist(training);
        log.debug("Created training with id {}", training.getId());
        return training;
    }

    @Override
    public Optional<Training> findById(Long id) {
        Optional<Training> training = Optional.ofNullable(entityManager.find(Training.class, id));
        log.debug("Find training with id {}: {}", id, training.isPresent() ? "found" : "not found");
        return training;
    }

    @Override
    public List<Training> findTraineeTrainings(String traineeUsername, LocalDate fromDate, LocalDate toDate,
                                               String trainerUsername, String trainingTypeName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);
        Join<Training, Trainee> trainee = training.join("trainee");
        Join<Trainee, User> traineeUser = trainee.join("user");
        Join<Training, Trainer> trainer = training.join("trainer");
        Join<Trainer, User> trainerUser = trainer.join("user");
        Join<Training, TrainingType> type = training.join("trainingType");

        List<Predicate> conditions = new ArrayList<>();
        conditions.add(cb.equal(traineeUser.get("username"), traineeUsername));
        addDateConditions(cb, training, fromDate, toDate, conditions);
        if (trainerUsername != null) {
            conditions.add(cb.equal(trainerUser.get("username"), trainerUsername));
        }
        if (trainingTypeName != null) {
            conditions.add(cb.equal(type.get("trainingTypeName"), trainingTypeName));
        }

        query.select(training)
                .where(conditions.toArray(new Predicate[0]))
                .orderBy(cb.asc(training.get("trainingDate")));
        List<Training> result = entityManager.createQuery(query).getResultList();
        log.debug("Found {} trainings for trainee {}", result.size(), traineeUsername);
        return result;
    }

    @Override
    public List<Training> findTrainerTrainings(String trainerUsername, LocalDate fromDate, LocalDate toDate,
                                               String traineeUsername) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Training> query = cb.createQuery(Training.class);
        Root<Training> training = query.from(Training.class);
        Join<Training, Trainer> trainer = training.join("trainer");
        Join<Trainer, User> trainerUser = trainer.join("user");
        Join<Training, Trainee> trainee = training.join("trainee");
        Join<Trainee, User> traineeUser = trainee.join("user");

        List<Predicate> conditions = new ArrayList<>();
        conditions.add(cb.equal(trainerUser.get("username"), trainerUsername));
        addDateConditions(cb, training, fromDate, toDate, conditions);
        if (traineeUsername != null) {
            conditions.add(cb.equal(traineeUser.get("username"), traineeUsername));
        }

        query.select(training)
                .where(conditions.toArray(new Predicate[0]))
                .orderBy(cb.asc(training.get("trainingDate")));
        List<Training> result = entityManager.createQuery(query).getResultList();
        log.debug("Found {} trainings for trainer {}", result.size(), trainerUsername);
        return result;
    }

    private void addDateConditions(CriteriaBuilder cb, Root<Training> training,
                                   LocalDate fromDate, LocalDate toDate, List<Predicate> conditions) {
        if (fromDate != null) {
            conditions.add(cb.greaterThanOrEqualTo(training.get("trainingDate"), fromDate));
        }
        if (toDate != null) {
            conditions.add(cb.lessThanOrEqualTo(training.get("trainingDate"), toDate));
        }
    }
}
