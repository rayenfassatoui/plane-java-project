package com.planeapp.dao;

import com.planeapp.model.Pilot;
import com.planeapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPilotDAO implements PilotDAO {

    @Override
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
            } else { System.err.println("Error adding pilot: Failed ID retrieval."); return Optional.empty(); }
        } catch (SQLException e) {
            System.err.println("SQL Error addPilot: " + e.getMessage());
             if (e.getSQLState().equals("23505")) { System.err.println("License exists: " + pilot.getLicenseNumber()); }
            return Optional.empty();
        }
    }

    @Override
    public List<Pilot> getAllPilots() {
        List<Pilot> pilots = new ArrayList<>();
        String sql = "SELECT id, name, license_number FROM pilots ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                pilots.add(mapResultSetToPilot(rs));
            }
        } catch (SQLException e) { System.err.println("SQL Error getAllPilots: " + e.getMessage()); }
        return pilots;
    }

    @Override
    public Optional<Pilot> findPilotById(int id) {
        String sql = "SELECT id, name, license_number FROM pilots WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToPilot(rs));
            }
        } catch (SQLException e) { System.err.println("SQL Error findPilotById: " + e.getMessage()); }
        return Optional.empty();
    }

    @Override
    public List<Pilot> searchPilots(String query) {
         List<Pilot> pilots = new ArrayList<>();
        String sql = "SELECT id, name, license_number FROM pilots " +
                     "WHERE LOWER(name) LIKE ? OR CAST(id AS TEXT) = ? ORDER BY id";
        String searchName = "%" + query.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchName);
            pstmt.setString(2, query);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                 pilots.add(mapResultSetToPilot(rs));
            }
        } catch (SQLException e) { System.err.println("SQL Error searchPilots: " + e.getMessage()); }
        return pilots;
    }

    @Override
    public boolean deletePilot(int id) {
        String sql = "DELETE FROM pilots WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) { System.err.println("SQL Error deletePilot: " + e.getMessage()); return false; }
    }

    // Helper method to map ResultSet to Pilot object
    private Pilot mapResultSetToPilot(ResultSet rs) throws SQLException {
        return new Pilot(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("license_number")
        );
    }

    // Note: getAssignedPilotForPlane could potentially live here if needed
    // It requires a join or a separate call to PlaneDAO
} 