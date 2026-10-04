package com.epam.gym.service.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.model.Trainer;
import com.epam.gym.service.TrainerService;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TrainerServiceImpl implements TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private TrainerDao trainerDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
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
        trainer.setUsername(usernameGenerator.generate(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(passwordGenerator.generate());
        trainer.setActive(true);

        Trainer created = trainerDao.create(trainer);
        log.info("Created trainer with id {} and username {}", created.getUserId(), created.getUsername());
        return created;
    }

    @Override
    public Trainer update(Trainer trainer) {
        Long id = trainer.getUserId();
        if (id == null) {
            throw new IllegalArgumentException("Trainer id must not be null for update");
        }
        Trainer existing = trainerDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trainer with id " + id + " not found"));

        trainer.setUsername(existing.getUsername());
        trainer.setPassword(existing.getPassword());

        Trainer updated = trainerDao.update(trainer);
        log.info("Updated trainer with id {}", id);
        return updated;
    }

    @Override
    public Optional<Trainer> select(Long id) {
        log.info("Selecting trainer with id {}", id);
        return trainerDao.findById(id);
    }
}
