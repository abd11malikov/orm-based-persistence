package com.epam.gym.service.impl;

import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.TrainingType;
import com.epam.gym.service.TrainingTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeDao trainingTypeDao;

    public TrainingTypeServiceImpl(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Override
    public List<TrainingType> findAll() {
        return trainingTypeDao.findAll();
    }

    @Override
    public Optional<TrainingType> findByName(String trainingTypeName) {
        return trainingTypeDao.findByName(trainingTypeName);
    }
}
