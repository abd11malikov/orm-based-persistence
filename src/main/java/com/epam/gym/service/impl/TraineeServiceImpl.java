package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.User;
import com.epam.gym.service.TraineeService;
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
        User user = trainee.getUser();
        if (user == null) {
            throw new IllegalArgumentException("Trainee must have user data");
        }
        user.setUsername(usernameGenerator.generate(user.getFirstName(), user.getLastName()));
        user.setPassword(passwordGenerator.generate());
        user.setActive(true);

        Trainee created = traineeDao.create(trainee);
        log.info("Created trainee with id {} and username {}", created.getId(), user.getUsername());
        return created;
    }

    @Override
    public Trainee update(Trainee trainee) {
        Long id = trainee.getId();
        if (id == null) {
            throw new IllegalArgumentException("Trainee id must not be null for update");
        }
        Trainee existing = traineeDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trainee with id " + id + " not found"));

        existing.getUser().setFirstName(trainee.getUser().getFirstName());
        existing.getUser().setLastName(trainee.getUser().getLastName());
        existing.setDateOfBirth(trainee.getDateOfBirth());
        existing.setAddress(trainee.getAddress());

        log.info("Updated trainee with id {}", id);
        return existing;
    }

    @Override
    public boolean delete(Long id) {
        Optional<Trainee> trainee = traineeDao.findById(id);
        if (trainee.isEmpty()) {
            log.warn("Trainee with id {} not found for delete", id);
            return false;
        }
        traineeDao.delete(trainee.get());
        log.info("Deleted trainee with id {}", id);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> select(Long id) {
        log.info("Selecting trainee with id {}", id);
        return traineeDao.findById(id);
    }
}
