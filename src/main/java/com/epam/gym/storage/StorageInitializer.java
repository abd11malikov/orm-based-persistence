package com.epam.gym.storage;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class StorageInitializer implements BeanPostProcessor {
    private InitData initData;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private static final Logger log = LoggerFactory.getLogger(StorageInitializer.class);
    private String initFilePath;

    @Value("${storage.init.file}")
    public void setInitFilePath(String initFilePath){
        this.initFilePath = initFilePath;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @NonNull String beanName) {
        log.debug("bean name : {} bean class name is {}", beanName, bean.getClass());
        if (beanName.equals("traineeStorage")){
            List<Trainee> trainees = getInitData().getTrainees();
            for (Trainee trainee : trainees) {
                ((Map<Long, Trainee>) bean).put(trainee.getUserId(),trainee);
            }
            log.info("{} trainees are added to the storage",trainees.size());
        }else if(beanName.equals("trainerStorage")){
            List<Trainer> trainers = getInitData().getTrainers();
            for (Trainer trainer : trainers) {
                ((Map<Long,Trainer>) bean).put(trainer.getUserId(), trainer);
            }
            log.info("{} trainers are added to storage",trainers.size());
        }else if(beanName.equals("trainingStorage")){
            List<Training> trainings = getInitData().getTrainings();
            for (Training training : trainings) {
                ((Map<Long,Training>) bean).put(training.getTrainingId(), training);
            }
            log.info("{} trainings are added to storage",trainings.size());
        }
        return bean;
    }

    private InitData getInitData(){
        if (initData == null ){
            try(InputStream inputStream = new ClassPathResource(initFilePath).getInputStream()) {
                initData = objectMapper.readValue(inputStream, InitData.class);
                log.info("Loaded init data from {}",initFilePath);
                return initData;
            } catch (IOException e) {
                throw new BeanInitializationException("Failed to load storage init data from " + initFilePath, e);
            }
        }
        return initData;
    }
}
