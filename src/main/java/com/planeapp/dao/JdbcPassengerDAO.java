package com.planeapp.dao;

import com.planeapp.model.Passenger;
import com.planeapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPassengerDAO implements PassengerDAO {

    @Override
    public Optional<Passenger> addPassenger(Passenger passenger, int planeId) {
        // Note: Plane existence validation could be done here or in a service layer
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
                Passenger addedPassenger = new Passenger(newId, passenger.getName(), passenger.getPassportNumber(), passenger.getSeatNumber());
                return Optional.of(addedPassenger);
            } else { System.err.println("Error adding passenger: Failed ID retrieval."); return Optional.empty(); }
        } catch (SQLException e) {
            System.err.println("SQL Error addPassenger: " + e.getMessage());
             if (e.getSQLState().equals("23505")) { System.err.println("Seat exists: " + passenger.getSeatNumber() + " on plane " + planeId); }
             if (e.getSQLState().equals("23503")) { System.err.println("Plane ID not found: " + planeId); }
            return Optional.empty();
        }
    }

    @Override
    public List<Passenger> getPassengersForPlane(int planeId) {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT id, name, passport_number, seat_number FROM passengers WHERE plane_id = ? ORDER BY seat_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, planeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                passengers.add(mapResultSetToPassenger(rs));
            }
        } catch (SQLException e) { System.err.println("SQL Error getPassengersForPlane: " + e.getMessage()); }
        return passengers;
    }

     @Override
     public boolean deletePassenger(int passengerId) {
         String sql = "DELETE FROM passengers WHERE id = ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, passengerId);
             int affectedRows = pstmt.executeUpdate();
             return affectedRows > 0;
         } catch (SQLException e) { System.err.println("SQL Error deletePassenger: " + e.getMessage()); return false; }
    }

    // Helper method to map ResultSet to Passenger object
    private Passenger mapResultSetToPassenger(ResultSet rs) throws SQLException {
         return new Passenger(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("passport_number"),
            rs.getString("seat_number")
        );
    }

    /**
     * Finds passengers who are on planes assigned to pilots whose name contains the query string.
     * NOTE: This implementation uses nested IN clauses for demonstration purposes.
     *       In practice, JOINs are usually more efficient for this type of query.
     * @param pilotNameQuery The substring to search for in pilot names (case-insensitive).
     * @return A list of matching passengers.
     */
    @Override
    public List<Passenger> findPassengersOnPlanesOfPilotsWithNameLike(String pilotNameQuery) {
        List<Passenger> passengers = new ArrayList<>();
        String sql = "SELECT id, name, passport_number, seat_number " +
                     "FROM passengers " +
                     "WHERE plane_id IN ( " +
                     "  SELECT id " +
                     "  FROM planes " +
                     "  WHERE pilot_id IN ( " +
                     "    SELECT id " +
                     "    FROM pilots " +
                     "    WHERE LOWER(name) LIKE ? " +
                     "  ) " +
                     ") " +
                     "ORDER BY name";

        String searchTerm = "%" + pilotNameQuery.toLowerCase() + "%";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, searchTerm);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                passengers.add(mapResultSetToPassenger(rs));
            }
        } catch (SQLException e) {
            System.err.println("SQL Error findPassengersOnPlanesOfPilotsWithNameLike: " + e.getMessage());
        }
        return passengers;
    }
} 