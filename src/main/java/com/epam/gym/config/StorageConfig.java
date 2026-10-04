package com.epam.gym.config;

import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import com.epam.gym.model.Training;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class StorageConfig {

    @Bean
    public Map<Long, Trainee> traineeStorage(){
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<Long, Trainer> trainerStorage(){
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<Long, Training> trainingStorage(){
        return new ConcurrentHashMap<>();
    }
}
