package com.epam.gym.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordGeneratorTest {

    private PasswordGenerator passwordGenerator;

    @BeforeEach
    void setUp() {
        passwordGenerator = new PasswordGenerator();
    }

    @Test
    void generate_returnsPasswordWithTenCharacters() {
        String password = passwordGenerator.generate();

        assertEquals(10, password.length());
    }

    @Test
    void generate_returnsOnlyLettersAndDigits() {
        String password = passwordGenerator.generate();

        for (char c : password.toCharArray()) {
            assertTrue(Character.isLetterOrDigit(c), "Unexpected character: " + c);
        }
    }

    @Test
    void generate_returnsDifferentPasswordsOnEachCall() {
        String first = passwordGenerator.generate();
        String second = passwordGenerator.generate();

        assertNotEquals(first, second);
    }
}
