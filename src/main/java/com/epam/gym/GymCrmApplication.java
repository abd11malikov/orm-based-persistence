package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.dto.Credentials;
import com.epam.gym.dto.TrainingRequest;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;
import java.util.List;

public class GymCrmApplication {
    private static final Logger log = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            Trainer yogaTrainer = facade.createTrainer(
                    new Trainer(new User("Nodira", "Karimova"), new TrainingType("Yoga")));
            Trainer cardioTrainer = facade.createTrainer(
                    new Trainer(new User("Rustam", "Aliyev"), new TrainingType("Cardio")));
            Credentials trainerLogin = new Credentials(
                    yogaTrainer.getUser().getUsername(), yogaTrainer.getUser().getPassword());

            Trainee trainee = new Trainee(new User("John", "Doe"));
            trainee.setDateOfBirth(LocalDate.of(1998, 3, 15));
            trainee.setAddress("7 Amir Temur Street, Tashkent");
            facade.createTrainee(trainee);
            String traineeUsername = trainee.getUser().getUsername();
            Credentials traineeLogin = new Credentials(traineeUsername, trainee.getUser().getPassword());

            log.info("Trainee credentials match: {}",
                    facade.traineeCredentialsMatch(traineeUsername, traineeLogin.password()));
            log.info("Wrong password matches: {}", facade.traineeCredentialsMatch(traineeUsername, "wrong"));

            facade.changeTraineePassword(traineeLogin, "newSecret1");
            traineeLogin = new Credentials(traineeUsername, "newSecret1");

            Trainee changes = new Trainee(new User("John", "Doe"));
            changes.setAddress("15 Navoi Street, Tashkent");
            facade.updateTrainee(traineeLogin, changes);
            log.info("Selected: {}", facade.selectTrainee(traineeLogin, traineeUsername));

            facade.updateTraineeTrainers(traineeLogin, List.of(yogaTrainer.getUser().getUsername()));
            log.info("Unassigned trainers: {}", facade.getUnassignedTrainers(traineeLogin));

            facade.addTraining(traineeLogin, new TrainingRequest(traineeUsername,
                    cardioTrainer.getUser().getUsername(), "Morning Cardio", "Cardio", LocalDate.now(), 45));
            facade.addTraining(trainerLogin, new TrainingRequest(traineeUsername,
                    yogaTrainer.getUser().getUsername(), "Evening Yoga", "Yoga", LocalDate.now().plusDays(1), 60));
            log.info("Trainee trainings: {}", facade.getTraineeTrainings(traineeLogin, null, null, null, null));
            log.info("Trainer trainings: {}", facade.getTrainerTrainings(trainerLogin, null, null, null));

            facade.deactivateTrainee(traineeLogin);
            try {
                facade.deactivateTrainee(traineeLogin);
            } catch (IllegalStateException e) {
                log.info("Second deactivate rejected: {}", e.getMessage());
            }
            facade.activateTrainee(traineeLogin);

            facade.deleteTrainee(traineeLogin);
            log.info("Trainee credentials match after delete: {}",
                    facade.traineeCredentialsMatch(traineeUsername, "newSecret1"));
        }
    }
}
