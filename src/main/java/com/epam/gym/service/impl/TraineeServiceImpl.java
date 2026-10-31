package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dto.Credentials;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.User;
import com.epam.gym.service.AuthenticationService;
import com.epam.gym.service.TraineeService;
import com.epam.gym.util.PasswordGenerator;
import com.epam.gym.util.UsernameGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.epam.gym.util.ValidationUtils.requireNotBlank;
import static com.epam.gym.util.ValidationUtils.requireNotNull;

@Service
@Transactional
public class TraineeServiceImpl implements TraineeService {
    private static final Logger log = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final AuthenticationService authenticationService;
    private final UsernameGenerator usernameGenerator;
    private final PasswordGenerator passwordGenerator;

    public TraineeServiceImpl(TraineeDao traineeDao,
                              TrainerDao trainerDao,
                              AuthenticationService authenticationService,
                              UsernameGenerator usernameGenerator,
                              PasswordGenerator passwordGenerator) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.authenticationService = authenticationService;
        this.usernameGenerator = usernameGenerator;
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Trainee create(Trainee trainee) {
        validate(trainee);
        User user = trainee.getUser();
        user.setUsername(usernameGenerator.generate(user.getFirstName(), user.getLastName()));
        user.setPassword(passwordGenerator.generate());
        user.setActive(true);

        Trainee created = traineeDao.create(trainee);
        log.info("Created trainee with username {}", user.getUsername());
        return created;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean credentialsMatch(String username, String password) {
        return authenticationService.traineeCredentialsMatch(username, password);
    }

    @Override
    @Transactional(readOnly = true)
    public Trainee selectByUsername(Credentials credentials, String username) {
        authenticationService.authenticateUser(credentials);
        Trainee trainee = traineeDao.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Trainee " + username + " not found"));
        log.info("Selected trainee {}", username);
        return trainee;
    }

    @Override
    public void changePassword(Credentials credentials, String newPassword) {
        requireNotBlank(newPassword, "New password");
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        trainee.getUser().setPassword(newPassword);
        log.info("Changed password for trainee {}", credentials.username());
    }

    @Override
    public Trainee updateProfile(Credentials credentials, Trainee changes) {
        validate(changes);
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        trainee.getUser().setFirstName(changes.getUser().getFirstName());
        trainee.getUser().setLastName(changes.getUser().getLastName());
        trainee.setDateOfBirth(changes.getDateOfBirth());
        trainee.setAddress(changes.getAddress());
        log.info("Updated profile of trainee {}", credentials.username());
        return trainee;
    }

    @Override
    public void activate(Credentials credentials) {
        changeActiveStatus(credentials, true);
    }

    @Override
    public void deactivate(Credentials credentials) {
        changeActiveStatus(credentials, false);
    }

    @Override
    public void delete(Credentials credentials) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        traineeDao.delete(trainee);
        log.info("Deleted trainee {}", credentials.username());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> getUnassignedTrainers(Credentials credentials) {
        authenticationService.authenticateTrainee(credentials);
        List<Trainer> trainers = trainerDao.findNotAssignedToTrainee(credentials.username());
        log.info("Found {} trainers not assigned to trainee {}", trainers.size(), credentials.username());
        return trainers;
    }

    @Override
    public List<Trainer> updateTrainers(Credentials credentials, List<String> trainerUsernames) {
        requireNotNull(trainerUsernames, "Trainer usernames");
        Trainee trainee = authenticationService.authenticateTrainee(credentials);

        Set<Trainer> trainers = new HashSet<>();
        for (String trainerUsername : trainerUsernames) {
            Trainer trainer = trainerDao.findByUsername(trainerUsername)
                    .orElseThrow(() -> new NoSuchElementException("Trainer " + trainerUsername + " not found"));
            trainers.add(trainer);
        }
        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(trainers);

        log.info("Trainee {} now has {} trainers", credentials.username(), trainers.size());
        return new ArrayList<>(trainee.getTrainers());
    }

    private void changeActiveStatus(Credentials credentials, boolean active) {
        Trainee trainee = authenticationService.authenticateTrainee(credentials);
        if (trainee.getUser().isActive() == active) {
            throw new IllegalStateException("Trainee " + credentials.username() + " is already "
                    + (active ? "active" : "inactive"));
        }
        trainee.getUser().setActive(active);
        log.info("Trainee {} is now {}", credentials.username(), active ? "active" : "inactive");
    }

    private void validate(Trainee trainee) {
        requireNotNull(trainee, "Trainee");
        requireNotNull(trainee.getUser(), "Trainee user data");
        requireNotBlank(trainee.getUser().getFirstName(), "First name");
        requireNotBlank(trainee.getUser().getLastName(), "Last name");
    }
}
