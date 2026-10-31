package com.epam.gym.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordGeneratorTest {

    private static final String CHARACTERS = "ABCDEFGHIJ";

    @Mock
    private Random random;

    @Test
    void generate_usesCharactersPickedByRandom() {
        // given
        PasswordGenerator generator = new PasswordGenerator(5, CHARACTERS, random);
        when(random.nextInt(CHARACTERS.length())).thenReturn(0, 2, 4, 6, 9);

        // when
        String password = generator.generate();

        // then
        assertEquals("ACEGJ", password);
    }

    @Test
    void generate_returnsPasswordOfConfiguredLength() {
        // given
        PasswordGenerator generator = new PasswordGenerator(12, CHARACTERS, new Random(42));

        // when
        String password = generator.generate();

        // then
        assertEquals(12, password.length());
    }

    @Test
    void generate_returnsOnlyConfiguredCharacters() {
        // given
        PasswordGenerator generator = new PasswordGenerator(50, "xy", new Random(42));

        // when
        String password = generator.generate();

        // then
        assertTrue(password.matches("[xy]{50}"), "Unexpected password: " + password);
    }

    @Test
    void generate_sameSeed_returnsSamePassword() {
        // given
        PasswordGenerator first = new PasswordGenerator(10, CHARACTERS, new Random(7));
        PasswordGenerator second = new PasswordGenerator(10, CHARACTERS, new Random(7));

        // when
        String firstPassword = first.generate();
        String secondPassword = second.generate();

        // then
        assertEquals(firstPassword, secondPassword);
    }

    @Test
    void constructor_nonPositiveLength_throwsException() {
        // given
        Random seeded = new Random(42);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new PasswordGenerator(0, CHARACTERS, seeded));

        // then
        assertEquals("Password length must be positive", exception.getMessage());
    }

    @Test
    void constructor_emptyCharacters_throwsException() {
        // given
        Random seeded = new Random(42);

        // when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new PasswordGenerator(10, "", seeded));

        // then
        assertEquals("Password characters must not be empty", exception.getMessage());
    }
}
