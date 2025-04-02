package com.planeapp.model;

public class Pilot {
    private int id;
    private String name;
    private String licenseNumber;

    // Constructor
    public Pilot(int id, String name, String licenseNumber) {
        this.id = id;
        this.name = name;
        this.licenseNumber = licenseNumber;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    // Setters (optional)
    public void setName(String name) {
        this.name = name;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    @Override
    public String toString() {
        return "Pilot{" +
               "id=" + id +
               ", name='" + name + '\'' +
               ", licenseNumber='" + licenseNumber + '\'' +
               '}';
    }
} 