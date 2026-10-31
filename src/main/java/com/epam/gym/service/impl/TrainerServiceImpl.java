package com.epam.gym.service.impl;

import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.TrainingTypeDao;
import com.epam.gym.dto.Credentials;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.TrainerService;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

import static com.epam.gym.util.ValidationUtils.requireNotBlank;
import static com.epam.gym.util.ValidationUtils.requireNotNull;

@Service
@Transactional
public class TrainerServiceImpl implements TrainerService {
    private static final Logger log = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TrainerDao trainerDao;
    private final TrainingTypeDao trainingTypeDao;
    private final AuthenticationService authenticationService;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public TrainerServiceImpl(TrainerDao trainerDao,
                              TrainingTypeDao trainingTypeDao,
                              AuthenticationService authenticationService,
                              UsernameGenerator usernameGenerator,
                              PasswordGenerator passwordGenerator) {
        this.trainerDao = trainerDao;
        this.trainingTypeDao = trainingTypeDao;
        this.authenticationService = authenticationService;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Trainer create(Trainer trainer) {
        validate(trainer);
        trainer.setSpecialization(findTrainingType(trainer.getSpecialization().getTrainingTypeName()));
        User user = trainer.getUser();
        user.setUsername(usernameGenerator.generate(user.getFirstName(), user.getLastName()));
        user.setPassword(passwordGenerator.generate());
        user.setActive(true);

        Trainer created = trainerDao.create(trainer);
        log.info("Created trainer with username {}", user.getUsername());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean credentialsMatch(String username, String password) {
        return authenticationService.trainerCredentialsMatch(username, password);
    }

    @Override
    @Transactional(readOnly = true)
    public Trainer selectByUsername(Credentials credentials, String username) {
        authenticationService.authenticateUser(credentials);
        Trainer trainer = trainerDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainer " + username + " not found"));
        log.info("Selected trainer {}", username);
        return trainer;
    }

    @Override
    public void changePassword(Credentials credentials, String newPassword) {
        requireNotBlank(newPassword, "New password");
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        trainer.getUser().setPassword(newPassword);
        log.info("Changed password for trainer {}", credentials.username());
    }

    @Override
    public Trainer updateProfile(Credentials credentials, Trainer changes) {
        validate(changes);
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        trainer.getUser().setFirstName(changes.getUser().getFirstName());
        trainer.getUser().setLastName(changes.getUser().getLastName());
        trainer.setSpecialization(findTrainingType(changes.getSpecialization().getTrainingTypeName()));
        log.info("Updated profile of trainer {}", credentials.username());
        return trainer;
    }

    @Override
    public void activate(Credentials credentials) {
        changeActiveStatus(credentials, true);
    }

    @Override
    public void deactivate(Credentials credentials) {
        changeActiveStatus(credentials, false);
    }

    private void changeActiveStatus(Credentials credentials, boolean active) {
        Trainer trainer = authenticationService.authenticateTrainer(credentials);
        if (trainer.getUser().isActive() == active) {
            throw new IllegalStateException("Trainer " + credentials.username() + " is already "
                    + (active ? "active" : "inactive"));
        }
        trainer.getUser().setActive(active);
        log.info("Trainer {} is now {}", credentials.username(), active ? "active" : "inactive");
    }

    private void validate(Trainer trainer) {
        requireNotNull(trainer, "Trainer");
        requireNotNull(trainer.getUser(), "Trainer user data");
        requireNotBlank(trainer.getUser().getFirstName(), "First name");
        requireNotBlank(trainer.getUser().getLastName(), "Last name");
        requireNotNull(trainer.getSpecialization(), "Specialization");
        requireNotBlank(trainer.getSpecialization().getTrainingTypeName(), "Specialization");
    }

    private TrainingType findTrainingType(String name) {
        return trainingTypeDao.findByName(name)
                .orElseThrow(() -> new NoSuchElementException("Training type " + name + " not found"));
    }
}
