package de.hhs;

import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseManager {
    private static final String URL = "jdbc:postgresql://localhost:5432/exoplanet_db";
    private static final String USER = "admin";
    private static final String PASSWORD = "12341234";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void insertPlanet(String name, int höhe, int breite) {
        String sql = "INSERT INTO Planet (Name, Höhe, Breite) VALUES (?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, höhe);
            pstmt.setInt(3, breite);
            pstmt.executeUpdate();
            System.out.println("Planet hinzugefügt: " + name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertRoboter(String status) {
        String sql = "INSERT INTO Roboter (Status) VALUES (?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.executeUpdate();
            System.out.println("Roboter hinzugefügt mit Status: " + status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertPosition(int planetID, int roboterID, int x, int y, String beschaffenheit) {
        String sql = "INSERT INTO Position (PlanetID, RoboterID, X, Y, Beschaffenheit) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, planetID);
            pstmt.setInt(2, roboterID);
            pstmt.setInt(3, x);
            pstmt.setInt(4, y);
            pstmt.setString(5, beschaffenheit);
            pstmt.executeUpdate();
            System.out.println("Position gespeichert: Roboter " + roboterID + " auf Planet " + planetID + " bei (" + x + "," + y + ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int getOrCreateRoboter(String name, String status) {
        String selectSql = "SELECT RoboterID FROM Roboter WHERE Status = ?";
        String insertSql = "INSERT INTO Roboter (Status) VALUES (?) RETURNING RoboterID";

        try (Connection conn = connect();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setString(1, status);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("RoboterID");
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, status);
                ResultSet insertRs = insertStmt.executeQuery();
                if (insertRs.next()) {
                    return insertRs.getInt("RoboterID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void getPosition(int roboterID) {
        String sql = "SELECT * FROM Position WHERE RoboterID = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roboterID);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                System.out.println("Roboter " + roboterID + " befindet sich auf Planet " + rs.getInt("PlanetID") +
                        " bei X: " + rs.getInt("X") + ", Y: " + rs.getInt("Y") +
                        " (Boden: " + rs.getString("Beschaffenheit") + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JSONArray getAllPlanets() {
        JSONArray result = new JSONArray();
        String sql = "SELECT * FROM \"planet\"";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                JSONObject planet = new JSONObject();
                planet.put("PlanetID", rs.getInt("PlanetID"));
                planet.put("Name", rs.getString("Name"));
                planet.put("Höhe", rs.getInt("Höhe"));
                planet.put("Breite", rs.getInt("Breite"));
                result.put(planet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONArray getAllRoboter() {
        JSONArray result = new JSONArray();
        String sql = "SELECT * FROM roboter";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                JSONObject roboter = new JSONObject();
                roboter.put("RoboterID", rs.getInt("RoboterID"));
                roboter.put("Status", rs.getString("Status"));
                result.put(roboter);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONArray getAllPositions() {
        JSONArray result = new JSONArray();
        String sql = "SELECT * FROM position";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                JSONObject position = new JSONObject();
                position.put("PositionID", rs.getInt("PositionID"));
                position.put("PlanetID", rs.getInt("PlanetID"));
                position.put("RoboterID", rs.getInt("RoboterID"));
                position.put("X", rs.getInt("X"));
                position.put("Y", rs.getInt("Y"));
                position.put("Beschaffenheit", rs.getString("Beschaffenheit"));
                result.put(position);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

}