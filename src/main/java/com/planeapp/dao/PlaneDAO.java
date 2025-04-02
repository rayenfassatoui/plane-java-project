package com.planeapp.dao;

import com.planeapp.model.Plane;
import java.util.List;
import java.util.Optional;

public interface PlaneDAO {
    Optional<Plane> addPlane(Plane plane);
    List<Plane> getAllPlanes();
    Optional<Plane> findPlaneById(int id);
    List<Plane> searchPlanes(String query);
    boolean deletePlane(int id);
    boolean assignPilotToPlane(int planeId, Integer pilotId); // Keep assignment here for now
} 