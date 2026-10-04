package com.epam.gym.util;

import com.epam.gym.dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsernameGeneratorTest {

    @Mock
    private UserDao userDao;

    private UsernameGenerator usernameGenerator;

    @BeforeEach
    void setUp() {
        usernameGenerator = new UsernameGenerator();
        usernameGenerator.setUserDao(userDao);
    }

    @Test
    void generate_usernameIsFree_returnsBaseUsername() {
        when(userDao.existsByUsername("John.Smith")).thenReturn(false);

        assertEquals("John.Smith", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_baseUsernameTaken_addsSerialNumber() {
        when(userDao.existsByUsername("John.Smith")).thenReturn(true);
        when(userDao.existsByUsername("John.Smith1")).thenReturn(false);

        assertEquals("John.Smith1", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_baseAndFirstSerialTaken_returnsNextSerialNumber() {
        when(userDao.existsByUsername("John.Smith")).thenReturn(true);
        when(userDao.existsByUsername("John.Smith1")).thenReturn(true);
        when(userDao.existsByUsername("John.Smith2")).thenReturn(false);

        assertEquals("John.Smith2", usernameGenerator.generate("John", "Smith"));
    }

    @Test
    void generate_namesWithSpaces_trimsThem() {
        when(userDao.existsByUsername(anyString())).thenReturn(false);

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
}
