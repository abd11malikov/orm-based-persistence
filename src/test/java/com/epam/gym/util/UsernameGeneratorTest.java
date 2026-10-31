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
        usernameGenerator = new UsernameGenerator(userDao);
    }

    @Test
    void generate_usernameIsFree_returnsBaseUsername() {
        // given
        when(userDao.existsByUsername("John.Smith")).thenReturn(false);

        // when
        String username = usernameGenerator.generate("John", "Smith");

        // then
        assertEquals("John.Smith", username);
    }

    @Test
    void generate_baseUsernameTaken_addsSerialNumber() {
        // given
        when(userDao.existsByUsername("John.Smith")).thenReturn(true);
        when(userDao.existsByUsername("John.Smith1")).thenReturn(false);

        // when
        String username = usernameGenerator.generate("John", "Smith");

        // then
        assertEquals("John.Smith1", username);
    }

    @Test
    void generate_baseAndFirstSerialTaken_returnsNextSerialNumber() {
        // given
        when(userDao.existsByUsername("John.Smith")).thenReturn(true);
        when(userDao.existsByUsername("John.Smith1")).thenReturn(true);
        when(userDao.existsByUsername("John.Smith2")).thenReturn(false);

        // when
        String username = usernameGenerator.generate("John", "Smith");

        // then
        assertEquals("John.Smith2", username);
    }

    @Test
    void generate_namesWithSpaces_trimsThem() {
        // given
        when(userDao.existsByUsername(anyString())).thenReturn(false);

        // when
        String username = usernameGenerator.generate("  John ", " Smith  ");

        // then
        assertEquals("John.Smith", username);
    }

    @Test
    void generate_nullFirstName_throwsException() {
        // given
        String firstName = null;

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usernameGenerator.generate(firstName, "Smith"));

        // then
        assertEquals("First name must not be empty", exception.getMessage());
    }

    @Test
    void generate_blankLastName_throwsException() {
        // given
        String lastName = "  ";

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> usernameGenerator.generate("John", lastName));

        // then
        assertEquals("Last name must not be empty", exception.getMessage());
    }
}
