package com.epam.gym.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class PasswordGenerator {
    private final int length;
    private final String characters;
    private final Random random;

    public PasswordGenerator(@Value("${password.length}") int length,
                             @Value("${password.characters}") String characters,
                             Random random) {
        if (length <= 0) {
            throw new IllegalArgumentException("Password length must be positive");
        }
        if (characters == null || characters.isEmpty()) {
            throw new IllegalArgumentException("Password characters must not be empty");
        }
        this.length = length;
        this.characters = characters;
        this.random = random;
    }

    public String generate() {
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            password.append(characters.charAt(index));
        }
        return password.toString();
    }
}
