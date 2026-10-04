package com.epam.gym.dao.impl;

import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDaoImpl implements TrainingTypeDao {
    private static final Logger log = LoggerFactory.getLogger(TrainingTypeDaoImpl.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<TrainingType> findByName(String trainingTypeName) {
        Optional<TrainingType> type = entityManager
                .createQuery("SELECT t FROM TrainingType t WHERE t.trainingTypeName = :name", TrainingType.class)
                .setParameter("name", trainingTypeName)
                .getResultStream()
                .findFirst();
        log.debug("Find training type {}: {}", trainingTypeName, type.isPresent() ? "found" : "not found");
        return type;
    }

    @Override
    public List<TrainingType> findAll() {
        List<TrainingType> types = entityManager
                .createQuery("SELECT t FROM TrainingType t ORDER BY t.trainingTypeName", TrainingType.class)
                .getResultList();
        log.debug("Found {} training types", types.size());
        return types;
    }
}
