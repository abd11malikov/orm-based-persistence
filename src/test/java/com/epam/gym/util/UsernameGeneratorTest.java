package com.epam.gym.util;

import com.epam.gym.dao.TraineeDao;
import com.epam.gym.dao.TrainerDao;
import com.epam.gym.model.Trainee;
import com.epam.gym.model.Trainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private TraineeDao traineeDao;

    @Mock
    private TrainerDao trainerDao;

    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        usernameGenerator = new UsernameGenerator();
        usernameGenerator.setTraineeDao(traineeDao);
        usernameGenerator.setTrainerDao(trainerDao);
    }

    @Test
    void generate_noExistingUsers_returnsBaseUsername() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals("John.Smith", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_traineeWithSameNameExists_addsSerialNumber() {
        when(traineeDao.findAll()).thenReturn(List.of(trainee("John.Smith")));
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals("John.Smith1", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_trainerWithSameNameExists_addsSerialNumber() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of(trainer("John.Smith")));

        assertEquals("John.Smith1", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_baseAndFirstSerialTaken_returnsNextSerialNumber() {
        when(traineeDao.findAll()).thenReturn(List.of(trainee("John.Smith")));
        when(trainerDao.findAll()).thenReturn(List.of(trainer("John.Smith1")));

        assertEquals("John.Smith2", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_gapInSerialNumbers_returnsFirstFreeOne() {
        when(traineeDao.findAll()).thenReturn(List.of(trainee("John.Smith"), trainee("John.Smith2")));
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals("John.Smith1", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_differentNameExists_returnsBaseUsername() {
        when(traineeDao.findAll()).thenReturn(List.of(trainee("Jane.Smith")));
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals("John.Smith", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_namesWithSpaces_trimsThem() {
        when(traineeDao.findAll()).thenReturn(List.of());
        when(trainerDao.findAll()).thenReturn(List.of());

        assertEquals("John.Smith", usernameGenerator.generate("  John ", " Smith  "));
    }

    @Test
    void generate_nullFirstName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> usernameGenerator.generate(null, "Smith"));
    }

    @Test
    void generate_blankLastName_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> usernameGenerator.generate("John", "  "));
    }

    private Trainee trainee(String username) {
        Trainee trainee = new Trainee();
        trainee.setUsername(username);
        return trainee;
    }

    private Trainer trainer(String username) {
        Trainer trainer = new Trainer();
        trainer.setUsername(username);
        return trainer;
    }
}
