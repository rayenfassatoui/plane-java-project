package com.planeapp.dao;

import com.planeapp.model.Passenger;
import java.util.List;
import java.util.Optional;

public interface PassengerDAO {
    Optional<Passenger> addPassenger(Passenger passenger, int planeId);
    List<Passenger> getPassengersForPlane(int planeId);
    boolean deletePassenger(int passengerId);

    // New method for demonstration of nested query concept
    List<Passenger> findPassengersOnPlanesOfPilotsWithNameLike(String pilotNameQuery);
} 