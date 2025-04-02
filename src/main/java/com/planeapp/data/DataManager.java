package com.planeapp.data;

import com.planeapp.model.Pilot;
import com.planeapp.model.Plane;
import com.planeapp.model.Passenger;
import com.planeapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataManager {

    // Remove ArrayLists and IDs, as they are now handled by the database
    // private final List<Plane> planes;
    // private final List<Pilot> pilots;
    // private int nextPlaneId = 1;
    // private int nextPilotId = 1;

    public DataManager() {
        // Constructor can be empty or used for initial setup if needed
        // Initial data should be added directly to the database or via the app
    }

    // --- Plane Management ---

    public Optional<Plane> addPlane(Plane plane) {
        String sql = "INSERT INTO planes (model, registration_number) VALUES (?, ?)";
        // Use RETURNING id to get the newly generated ID
        String sqlReturningId = "INSERT INTO planes (model, registration_number) VALUES (?, ?) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlReturningId)) {

            pstmt.setString(1, plane.getModel());
            pstmt.setString(2, plane.getRegistrationNumber());

            ResultSet rs = pstmt.executeQuery(); // Use executeQuery for RETURNING

            if (rs.next()) {
                int newId = rs.getInt(1);
                // Create a new Plane object with the correct ID
                Plane addedPlane = new Plane(newId, plane.getModel(), plane.getRegistrationNumber());
                return Optional.of(addedPlane);
            } else {
                 System.err.println("Error adding plane: Failed to retrieve generated ID.");
                 return Optional.empty();
            }

        } catch (SQLException e) {
            System.err.println("SQL Error adding plane: " + e.getMessage());
            // Check for unique constraint violation (e.g., duplicate registration number)
            if (e.getSQLState().equals("23505")) { // Standard SQLState for unique violation
                System.err.println("Plane with registration number '" + plane.getRegistrationNumber() + "' already exists.");
            }
            return Optional.empty();
        }
    }

    public List<Plane> getAllPlanes() {
        List<Plane> planes = new ArrayList<>();
        String sql = "SELECT id, model, registration_number, pilot_id FROM planes ORDER BY id";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String model = rs.getString("model");
                String regNumber = rs.getString("registration_number");
                Integer pilotId = (Integer) rs.getObject("pilot_id"); // getObject handles SQL NULL

                Plane plane = new Plane(id, model, regNumber);
                plane.setPilotId(pilotId);
                planes.add(plane);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting all planes: " + e.getMessage());
        }
        return planes;
    }

    public Optional<Plane> findPlaneById(int id) {
        String sql = "SELECT id, model, registration_number, pilot_id FROM planes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                 String model = rs.getString("model");
                 String regNumber = rs.getString("registration_number");
                 Integer pilotId = (Integer) rs.getObject("pilot_id");
                 Plane plane = new Plane(id, model, regNumber);
                 plane.setPilotId(pilotId);
                 return Optional.of(plane);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error finding plane by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

     public List<Plane> searchPlanes(String query) {
        List<Plane> planes = new ArrayList<>();
        // Use LOWER() for case-insensitive search and prepared statement for safety
        String sql = "SELECT id, model, registration_number, pilot_id FROM planes " +
                     "WHERE LOWER(model) LIKE ? OR LOWER(registration_number) LIKE ? ORDER BY id";
        String searchTerm = "%" + query.toLowerCase() + "%"; // Add wildcards

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String model = rs.getString("model");
                String regNumber = rs.getString("registration_number");
                Integer pilotId = (Integer) rs.getObject("pilot_id");
                Plane plane = new Plane(id, model, regNumber);
                plane.setPilotId(pilotId);
                planes.add(plane);
            }
        } catch (SQLException e) {
            System.err.println("SQL Error searching planes: " + e.getMessage());
        }
        return planes;
    }

    public boolean deletePlane(int id) {
         String sql = "DELETE FROM planes WHERE id = ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setInt(1, id);
             int affectedRows = pstmt.executeUpdate();
             return affectedRows > 0;

         } catch (SQLException e) {
             System.err.println("SQL Error deleting plane: " + e.getMessage());
             return false;
         }
    }

    // --- Pilot Management ---

    public Optional<Pilot> addPilot(Pilot pilot) {
        String sqlReturningId = "INSERT INTO pilots (name, license_number) VALUES (?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlReturningId)) {

            pstmt.setString(1, pilot.getName());
            pstmt.setString(2, pilot.getLicenseNumber());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt(1);
                Pilot addedPilot = new Pilot(newId, pilot.getName(), pilot.getLicenseNumber());
                return Optional.of(addedPilot);
            } else {
                System.err.println("Error adding pilot: Failed to retrieve generated ID.");
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("SQL Error adding pilot: " + e.getMessage());
             if (e.getSQLState().equals("23505")) { // Unique constraint violation
                System.err.println("Pilot with license number '" + pilot.getLicenseNumber() + "' already exists.");
            }
            return Optional.empty();
        }
    }

    public List<Pilot> getAllPilots() {
        List<Pilot> pilots = new ArrayList<>();
        String sql = "SELECT id, name, license_number FROM pilots ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                pilots.add(new Pilot(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("license_number")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting all pilots: " + e.getMessage());
        }
        return pilots;
    }

    public Optional<Pilot> findPilotById(int id) {
        String sql = "SELECT id, name, license_number FROM pilots WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Optional.of(new Pilot(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("license_number")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error finding pilot by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<Pilot> searchPilots(String query) {
         List<Pilot> pilots = new ArrayList<>();
        // Search by name (case-insensitive) or exact ID match
        String sql = "SELECT id, name, license_number FROM pilots " +
                     "WHERE LOWER(name) LIKE ? OR CAST(id AS TEXT) = ? ORDER BY id";
        String searchName = "%" + query.toLowerCase() + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchName);
            pstmt.setString(2, query); // ID search needs exact match
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                 pilots.add(new Pilot(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("license_number")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error searching pilots: " + e.getMessage());
        }
        return pilots;
    }

    public boolean deletePilot(int id) {
        // Database handles unassigning via ON DELETE SET NULL constraint if set up
        String sql = "DELETE FROM pilots WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
             System.err.println("SQL Error deleting pilot: " + e.getMessage());
             // Optional: Check for foreign key constraint violation if ON DELETE was not SET NULL
             // if (e.getSQLState().equals("23503")) { ... }
             return false;
        }
    }

    // --- Assignment ---

    public boolean assignPilotToPlane(int planeId, Integer pilotId) {
         // First check if plane exists
         if (findPlaneById(planeId).isEmpty()) {
             System.err.println("Error assigning pilot: Plane not found.");
             return false;
         }
         // If assigning (not unassigning), check if pilot exists
         if (pilotId != null && findPilotById(pilotId).isEmpty()) {
              System.err.println("Error assigning pilot: Pilot not found.");
              return false;
         }

        String sql = "UPDATE planes SET pilot_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Use setObject to handle null correctly for Integer type
            if (pilotId == null) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, pilotId);
            }
            pstmt.setInt(2, planeId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("SQL Error assigning/unassigning pilot for plane: " + e.getMessage());
            return false;
        }
    }

     public Optional<Pilot> getAssignedPilotForPlane(int planeId) {
         // Join query might be more efficient, but separate lookups are simpler here
        return findPlaneById(planeId)
                .flatMap(plane -> Optional.ofNullable(plane.getPilotId())) // Get pilotId if present
                .flatMap(this::findPilotById); // Find pilot by ID using existing method
    }

    // --- Passenger Management ---

    public Optional<Passenger> addPassenger(Passenger passenger, int planeId) {
        // Validate plane exists first
        if (findPlaneById(planeId).isEmpty()) {
            System.err.println("Cannot add passenger: Plane with ID " + planeId + " not found.");
            return Optional.empty();
        }

        String sqlReturningId = "INSERT INTO passengers (name, passport_number, seat_number, plane_id) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlReturningId)) {

            pstmt.setString(1, passenger.getName());
            pstmt.setString(2, passenger.getPassportNumber());
            pstmt.setString(3, passenger.getSeatNumber());
            pstmt.setInt(4, planeId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt(1);
                // Create a new Passenger object with the correct ID
                Passenger addedPassenger = new Passenger(newId, passenger.getName(), passenger.getPassportNumber(), passenger.getSeatNumber());
                // Note: The new object doesn't store planeId, it's managed by the relationship
                return Optional.of(addedPassenger);
            } else {
                System.err.println("Error adding passenger: Failed to retrieve generated ID.");
                return Optional.empty();
            }
        } catch (SQLException e) {
            System.err.println("SQL Error adding passenger: " + e.getMessage());
             if (e.getSQLState().equals("23505")) { // Unique constraint violation (e.g., duplicate seat on plane)
                System.err.println("Passenger with seat number '" + passenger.getSeatNumber() + "' might already exist on this plane (ID: " + planeId + ").");
            }
             if (e.getSQLState().equals("23503")) { // Foreign key violation
                 System.err.println("Cannot add passenger: Plane with ID " + planeId + " likely doesn't exist (foreign key constraint).");
             }
            return Optional.empty();
        }
    }

    public List<Passenger> getPassengersForPlane(int planeId) {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT id, name, passport_number, seat_number FROM passengers WHERE plane_id = ? ORDER BY seat_number";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, planeId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                passengers.add(new Passenger(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("passport_number"),
                    rs.getString("seat_number")
                ));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error getting passengers for plane ID " + planeId + ": " + e.getMessage());
        }
        return passengers;
    }

     public boolean deletePassenger(int passengerId) {
         String sql = "DELETE FROM passengers WHERE id = ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setInt(1, passengerId);
             int affectedRows = pstmt.executeUpdate();
             return affectedRows > 0;

         } catch (SQLException e) {
             System.err.println("SQL Error deleting passenger: " + e.getMessage());
             return false;
         }
    }
} 