package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.service.TraineeService;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private TraineeDao traineeDao;
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
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
    public Trainee create(Trainee trainee) {
        trainee.setUsername(usernameGenerator.generate(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(passwordGenerator.generate());
        trainee.setActive(true);

        Trainee created = traineeDao.create(trainee);
        log.info("Created trainee with id {} and username {}", created.getUserId(), created.getUsername());
        return created;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Long id = trainee.getUserId();
        if (id == null) {
            throw new IllegalArgumentException("Trainee id must not be null for update");
        }
        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trainee with id " + id + " not found"));

        trainee.setUsername(existing.getUsername());
        trainee.setPassword(existing.getPassword());

        Trainee updated = traineeDao.update(trainee);
        log.info("Updated trainee with id {}", id);
        return updated;
    }

    @Override
    public boolean delete(Long id) {
        boolean deleted = traineeDao.delete(id);
        if (deleted) {
            log.info("Deleted trainee with id {}", id);
        } else {
            log.warn("Trainee with id {} not found for delete", id);
        }
        return deleted;
    }

    @Override
    public Optional<Trainee> select(Long id) {
        log.info("Selecting trainee with id {}", id);
        return traineeDao.findById(id);
    }
}
