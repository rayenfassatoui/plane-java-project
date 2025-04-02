package com.planeapp.model;

public class Passenger {
    private int id;
    private String name;
    private String passportNumber;
    private String seatNumber;

    // Constructor
    public Passenger(int id, String name, String passportNumber, String seatNumber) {
        this.id = id;
        this.name = name;
        this.passportNumber = passportNumber;
        this.seatNumber = seatNumber;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    // Setters (optional, if needed)
    public void setName(String name) {
        this.name = name;
    }

    public void setPassportNumber(String passportNumber) {
        this.passportNumber = passportNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    @Override
    public String toString() {
        return "Passenger{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", passportNumber='" + passportNumber + '\'' +
               ", seatNumber='" + seatNumber + '\'' +
               '}';
    }
} 