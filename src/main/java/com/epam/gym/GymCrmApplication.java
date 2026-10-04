package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class GymCrmApplication {
    private static final Logger log = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            Trainee trainee = new Trainee();
            trainee.setFirstName("John");
            trainee.setLastName("Doe");
            trainee.setDateOfBirth(LocalDate.of(1998, 3, 15));
            trainee.setAddress("7 Amir Temur Street, Tashkent");
            facade.createTrainee(trainee);
            log.info("Created: {}", trainee);

            TrainingType yoga = new TrainingType();
            yoga.setTrainingTypeName("Yoga");
            Trainer trainer = new Trainer();
            trainer.setFirstName("Nodira");
            trainer.setLastName("Karimova");
            trainer.setSpecialization(yoga);
            facade.createTrainer(trainer);
            log.info("Created: {}", trainer);

            Training training = new Training();
            training.setTraineeId(trainee.getUserId());
            training.setTrainerId(trainer.getUserId());
            training.setTrainingName("Morning Yoga");
            training.setTrainingType(yoga);
            training.setTrainingDate(LocalDate.now().plusDays(1));
            training.setTrainingDuration(60);
            facade.createTraining(training);
            log.info("Created: {}", training);

            trainee.setAddress("15 Navoi Street, Tashkent");
            facade.updateTrainee(trainee);
            log.info("Selected after update: {}", facade.selectTrainee(trainee.getUserId()).orElseThrow());

            facade.deleteTrainee(2L);
            log.info("Trainee 2 present after delete: {}", facade.selectTrainee(2L).isPresent());
        }
    }
}
