package com.planeapp.dao;

import com.planeapp.model.Pilot;
import java.util.List;
import java.util.Optional;

public interface PilotDAO {
    Optional<Pilot> addPilot(Pilot pilot);
    List<Pilot> getAllPilots();
    Optional<Pilot> findPilotById(int id);
    List<Pilot> searchPilots(String query);
    boolean deletePilot(int id);
    // Method to get assigned pilot might belong here or in PlaneDAO depending on perspective
    // Optional<Pilot> getAssignedPilotForPlane(int planeId); 
} 