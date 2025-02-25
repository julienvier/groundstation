package de.hhs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseManager {
	private static final String URL = "jdbc:postgresql://localhost:5432/exoplanet_db";
	private static final String USER = "admin";
	private static final String PASSWORD = "12341234";

	private Connection connect() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}

	public JSONArray insertPlanet(String name, int height, int width) {
		String sql = "INSERT INTO Planet (planetid, Height, Width) VALUES (?, ?, ?)";

		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.setInt(2, height);
			pstmt.setInt(3, width);
			pstmt.executeUpdate();
			System.out.println("Planet added: " + name);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	// Fügt einen neuen Roboter mit Status "waiting" hinzu
	public void insertRobot(String name, String status) {
		String sql = "INSERT INTO robot (robotId, status) VALUES (?, ?)";

		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.setString(2, status);
			pstmt.executeUpdate();
			System.out.println("Robot '" + name + "' added with status: " + status);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insertPosition(int planetID, int robotID, int x, int y, String terrain) {
		String sql = "INSERT INTO Position (PlanetID, RobotID, X, Y, Terrain) VALUES (?, ?, ?, ?, ?)";

		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, planetID);
			pstmt.setInt(2, robotID);
			pstmt.setInt(3, x);
			pstmt.setInt(4, y);
			pstmt.setString(5, terrain);
			pstmt.executeUpdate();
			System.out.println(
					"Position saved: Robot " + robotID + " on Planet " + planetID + " at (" + x + "," + y + ")");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public int getOrCreateRobot(String name, String status) {
		String selectSql = "SELECT RobotID FROM Robot WHERE Status = ?";
		String insertSql = "INSERT INTO Robot (Status) VALUES (?) RETURNING RobotID";

		try (Connection conn = connect(); PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

			selectStmt.setString(1, status);
			ResultSet rs = selectStmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("RobotID");
			}

			try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
				insertStmt.setString(1, status);
				ResultSet insertRs = insertStmt.executeQuery();
				if (insertRs.next()) {
					return insertRs.getInt("RobotID");
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public JSONArray getAllPlanets() {
		JSONArray result = new JSONArray();
		String sql = "SELECT * FROM planet";

		try (Connection conn = connect();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				JSONObject planet = new JSONObject();
				String uuid = GroundStation.generateUUID();
				planet.put("planetid", rs.getString(uuid));
				planet.put("height", rs.getInt("height"));
				planet.put("width", rs.getInt("width"));
				result.put(planet);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public JSONArray getAllRobots() {
		JSONArray result = new JSONArray();
		String sql = "SELECT * FROM robot";

		try (Connection conn = connect();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				JSONObject robot = new JSONObject();
				robot.put("RobotID", rs.getString("RobotID"));
				robot.put("Status", rs.getString("Status"));
				result.put(robot);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public JSONArray getAllPositions() {
		JSONArray result = new JSONArray();
		String sql = "SELECT * FROM position";

		try (Connection conn = connect();
				PreparedStatement pstmt = conn.prepareStatement(sql);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				JSONObject position = new JSONObject();
				position.put("PositionID", rs.getInt("PositionID"));
				position.put("PlanetID", rs.getInt("PlanetID"));
				position.put("RobotID", rs.getInt("RobotID"));
				position.put("X", rs.getInt("X"));
				position.put("Y", rs.getInt("Y"));
				position.put("Terrain", rs.getString("Terrain"));
				result.put(position);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void getPosition(int robotID) {
		String sql = "SELECT * FROM Position WHERE RobotID = ?";

		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setInt(1, robotID);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				System.out.println("Robot " + robotID + " is located on Planet " + rs.getInt("PlanetID") + " at X: "
						+ rs.getInt("X") + ", Y: " + rs.getInt("Y") + " (Terrain: " + rs.getString("Terrain") + ")");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// Aktualisiert den Status eines Roboters
	public void updateRobotStatus(String name, String status) {
		String sql = "UPDATE Robot SET status = ? WHERE robotId = ?";

		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, status);
			pstmt.setString(2, name);
			pstmt.executeUpdate();
			System.out.println("Robot '" + name + "' status updated to: " + status);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean robotExists(String robotId) {
		String sql = "SELECT 1 FROM robot WHERE robotId = ?"; // or whatever your column is
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, robotId);
			ResultSet rs = pstmt.executeQuery();
			return rs.next(); // true if at least one row found
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean planetExists(String name) {
		String sql = "SELECT 1 FROM planet WHERE name = ?";
		try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			return rs.next(); // true if at least one row found
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

}
