package com.epam.gym.service.impl;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.dao.UserDao;
import com.epam.gym.dto.Credentials;
import com.epam.gym.exception.AuthenticationFailedException;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.User;
import com.epam.gym.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    private static final String INVALID_CREDENTIALS = "Invalid username or password";

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final UserDao userDao;

    public AuthenticationServiceImpl(TraineeDao traineeDao,
                                     TrainerDao trainerDao,
                                     UserDao userDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.userDao = userDao;
    }

    @Override
    public boolean traineeCredentialsMatch(String username, String password) {
        Optional<Trainee> trainee = traineeDao.findByUsername(username);
        boolean matches = trainee.isPresent() && passwordMatches(trainee.get().getUser(), password);
        log.info("Trainee credentials check for {}: {}", username, matches ? "match" : "no match");
        return matches;
    }

    @Override
    public boolean trainerCredentialsMatch(String username, String password) {
        Optional<Trainer> trainer = trainerDao.findByUsername(username);
        boolean matches = trainer.isPresent() && passwordMatches(trainer.get().getUser(), password);
        log.info("Trainer credentials check for {}: {}", username, matches ? "match" : "no match");
        return matches;
    }

    @Override
    public Trainee authenticateTrainee(Credentials credentials) {
        requireCredentials(credentials);
        Optional<Trainee> trainee = traineeDao.findByUsername(credentials.username());
        if (trainee.isEmpty() || !passwordMatches(trainee.get().getUser(), credentials.password())) {
            throw failed(credentials.username());
        }
        log.debug("Trainee {} authenticated", credentials.username());
        return trainee.get();
    }

    @Override
    public Trainer authenticateTrainer(Credentials credentials) {
        requireCredentials(credentials);
        Optional<Trainer> trainer = trainerDao.findByUsername(credentials.username());
        if (trainer.isEmpty() || !passwordMatches(trainer.get().getUser(), credentials.password())) {
            throw failed(credentials.username());
        }
        log.debug("Trainer {} authenticated", credentials.username());
        return trainer.get();
    }

    @Override
    public User authenticateUser(Credentials credentials) {
        requireCredentials(credentials);
        Optional<User> user = userDao.findByUsername(credentials.username());
        if (user.isEmpty() || !passwordMatches(user.get(), credentials.password())) {
            throw failed(credentials.username());
        }
        log.debug("User {} authenticated", credentials.username());
        return user.get();
    }

    private boolean passwordMatches(User user, String password) {
        return user.getPassword().equals(password);
    }

    private void requireCredentials(Credentials credentials) {
        if (credentials == null || credentials.username() == null || credentials.password() == null) {
            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }
    }

    private AuthenticationFailedException failed(String username) {
        log.warn("Authentication failed for username {}", username);
        return new AuthenticationFailedException(INVALID_CREDENTIALS);
    }
}
