package com.planeapp.model;

public class Plane {
    private int id;
    private String model;
    private String registrationNumber;
    private Integer pilotId; // Use Integer to allow null if no pilot assigned

    // Constructor
    public Plane(int id, String model, String registrationNumber) {
        this.id = id;
        this.model = model;
        this.registrationNumber = registrationNumber;
        this.pilotId = null; // Initially no pilot assigned
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getModel() {
        return model;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public Integer getPilotId() {
        return pilotId;
    }

    // Setters
    public void setModel(String model) {
        this.model = model;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public void setPilotId(Integer pilotId) {
        this.pilotId = pilotId;
    }

    @Override
    public String toString() {
        return "Plane{" +
               "id=" + id +
               ", model='" + model + '\'' +
               ", registrationNumber='" + registrationNumber + '\'' +
               ", pilotId=" + (pilotId == null ? "None" : pilotId) +
               '}';
    }
} 