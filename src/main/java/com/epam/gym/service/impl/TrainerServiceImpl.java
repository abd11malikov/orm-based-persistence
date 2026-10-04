package com.epam.gym.service.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import com.epam.gym.service.TrainerService;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Transactional
public class TrainerServiceImpl implements TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDao trainerDao;
    private TrainingTypeDao trainingTypeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setTrainingTypeDao(TrainingTypeDao trainingTypeDao) {
        this.trainingTypeDao = trainingTypeDao;
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Trainer create(Trainer trainer) {
        User user = trainer.getUser();
        if (user == null) {
            throw new IllegalArgumentException("Trainer must have user data");
        }
        trainer.setSpecialization(findTrainingType(trainer.getSpecialization()));
        user.setUsername(usernameGenerator.generate(user.getFirstName(), user.getLastName()));
        user.setPassword(passwordGenerator.generate());
        user.setActive(true);

        Trainer created = trainerDao.create(trainer);
        log.info("Created trainer with id {} and username {}", created.getId(), user.getUsername());
        return created;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Long id = trainer.getId();
        if (id == null) {
            throw new IllegalArgumentException("Trainer id must not be null for update");
        }
        Trainer existing = trainerDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trainer with id " + id + " not found"));

        existing.getUser().setFirstName(trainer.getUser().getFirstName());
        existing.getUser().setLastName(trainer.getUser().getLastName());
        existing.setSpecialization(findTrainingType(trainer.getSpecialization()));

        log.info("Updated trainer with id {}", id);
        return existing;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> select(Long id) {
        log.info("Selecting trainer with id {}", id);
        return trainerDao.findById(id);
    }

    private TrainingType findTrainingType(TrainingType specialization) {
        if (specialization == null || specialization.getTrainingTypeName() == null) {
            throw new IllegalArgumentException("Trainer specialization is required");
        }
        String name = specialization.getTrainingTypeName();
        return trainingTypeDao.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Training type " + name + " not found"));
    }
}
