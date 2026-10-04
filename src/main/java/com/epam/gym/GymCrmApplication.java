package com.epam.gym;

import com.epam.gym.config.AppConfig;
import com.epam.gym.facade.GymFacade;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.epam.gym.model.TrainingType;
import com.epam.gym.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate;

public class GymCrmApplication {
    private static final Logger log = LoggerFactory.getLogger(GymCrmApplication.class);

    public static void main(String[] args) {
        try (var context = new AnnotationConfigApplicationContext(AppConfig.class)) {
            GymFacade facade = context.getBean(GymFacade.class);

            log.info("Training types: {}", facade.getTrainingTypes());

            Trainee trainee = new Trainee(new User("John", "Doe"));
            trainee.setDateOfBirth(LocalDate.of(1998, 3, 15));
            trainee.setAddress("7 Amir Temur Street, Tashkent");
            facade.createTrainee(trainee);
            log.info("Created: {}", trainee);

            Trainer trainer = new Trainer(new User("Nodira", "Karimova"), new TrainingType("Yoga"));
            facade.createTrainer(trainer);
            log.info("Created: {}", trainer);

            Training training = new Training();
            training.setTrainee(trainee);
            training.setTrainer(trainer);
            training.setTrainingName("Morning Yoga");
            training.setTrainingType(new TrainingType("Yoga"));
            training.setTrainingDate(LocalDate.now().plusDays(1));
            training.setTrainingDuration(60);
            facade.createTraining(training);
            log.info("Created: {}", training);

            trainee.setAddress("15 Navoi Street, Tashkent");
            facade.updateTrainee(trainee);
            log.info("Selected after update: {}", facade.selectTrainee(trainee.getId()).orElseThrow());

            facade.deleteTrainee(trainee.getId());
            log.info("Trainee {} present after delete: {}",
                    trainee.getId(), facade.selectTrainee(trainee.getId()).isPresent());
        }
    }
}
