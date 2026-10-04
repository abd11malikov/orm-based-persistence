package com.epam.gym.dto;

public record Credentials(String username, String password) {

    @Override
    public String toString() {
        return "Credentials{username='" + username + "'}";
    }
}
