package com.epam.gym.model;

import java.time.LocalDate;

public class Trainee extends User{
    private LocalDate dateOfBirth;
    private String address;


    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Trainee{" +
                "dateOfBirth=" + dateOfBirth +
                ", address='" + address + '\'' +
                ", " + super.toString() +
                '}';
    }

    public Trainee(){

    }

    public Trainee(Long userId, String firstName, String lastName, String username, String password, boolean active, LocalDate dateOfBirth, String address) {
        super(userId, firstName, lastName, username, password, active);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }
}
