package com.planeapp.dao;

import com.planeapp.model.Plane;
import com.planeapp.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPlaneDAO implements PlaneDAO {

    @Override
    public Optional<Plane> addPlane(Plane plane) {
        String sqlReturningId = "INSERT INTO planes (model, registration_number) VALUES (?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlReturningId)) {
            pstmt.setString(1, plane.getModel());
            pstmt.setString(2, plane.getRegistrationNumber());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int newId = rs.getInt(1);
                Plane addedPlane = new Plane(newId, plane.getModel(), plane.getRegistrationNumber());
                return Optional.of(addedPlane);
            } else { System.err.println("Error adding plane: Failed ID retrieval."); return Optional.empty(); }
        } catch (SQLException e) {
            System.err.println("SQL Error addPlane: " + e.getMessage());
            if (e.getSQLState().equals("23505")) { System.err.println("Reg number exists: " + plane.getRegistrationNumber()); }
            return Optional.empty();
        }
    }

    @Override
    public List<Plane> getAllPlanes() {
        List<Plane> planes = new ArrayList<>();
        String sql = "SELECT id, model, registration_number, pilot_id FROM planes ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Plane plane = mapResultSetToPlane(rs);
                planes.add(plane);
            }
        } catch (SQLException e) { System.err.println("SQL Error getAllPlanes: " + e.getMessage()); }
        return planes;
    }

    @Override
    public Optional<Plane> findPlaneById(int id) {
        String sql = "SELECT id, model, registration_number, pilot_id FROM planes WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                 return Optional.of(mapResultSetToPlane(rs));
            }
        } catch (SQLException e) { System.err.println("SQL Error findPlaneById: " + e.getMessage()); }
        return Optional.empty();
    }

     @Override
     public List<Plane> searchPlanes(String query) {
        List<Plane> planes = new ArrayList<>();
        String sql = "SELECT id, model, registration_number, pilot_id FROM planes " +
                     "WHERE LOWER(model) LIKE ? OR LOWER(registration_number) LIKE ? ORDER BY id";
        String searchTerm = "%" + query.toLowerCase() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, searchTerm);
            pstmt.setString(2, searchTerm);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                planes.add(mapResultSetToPlane(rs));
            }
        } catch (SQLException e) { System.err.println("SQL Error searchPlanes: " + e.getMessage()); }
        return planes;
    }

    @Override
    public boolean deletePlane(int id) {
         String sql = "DELETE FROM planes WHERE id = ?";
         try (Connection conn = DatabaseConnection.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {
             pstmt.setInt(1, id);
             int affectedRows = pstmt.executeUpdate();
             return affectedRows > 0;
         } catch (SQLException e) { System.err.println("SQL Error deletePlane: " + e.getMessage()); return false; }
    }

    @Override
    public boolean assignPilotToPlane(int planeId, Integer pilotId) {
        String sql = "UPDATE planes SET pilot_id = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (pilotId == null) {
                pstmt.setNull(1, Types.INTEGER);
            } else {
                pstmt.setInt(1, pilotId);
            }
            pstmt.setInt(2, planeId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("SQL Error assignPilotToPlane: " + e.getMessage());
            return false;
        }
    }

    // Helper method to map ResultSet to Plane object
    private Plane mapResultSetToPlane(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String model = rs.getString("model");
        String regNumber = rs.getString("registration_number");
        Integer pilotId = (Integer) rs.getObject("pilot_id");
        Plane plane = new Plane(id, model, regNumber);
        plane.setPilotId(pilotId);
        return plane;
    }
} 