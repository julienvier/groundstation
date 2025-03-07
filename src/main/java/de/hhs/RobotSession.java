package de.hhs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class RobotSession implements Runnable {
	private GroundStation groundStation;
	private Socket robotSocket;
	private PrintWriter commandWriter;
	private BufferedReader responseReader;
	private String planetId;
	private String name;
	private String status;
	private final DatabaseManager dbManager = new DatabaseManager();

	public RobotSession(GroundStation groundStation, Socket robotSocket, String name, String status) {
		this.groundStation = groundStation;
		this.robotSocket = robotSocket;
		this.name = name;
		this.status = status;
		try {
			this.commandWriter = new PrintWriter(robotSocket.getOutputStream(), true);
			this.responseReader = new BufferedReader(new InputStreamReader(robotSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}

	public String getStatus() {
		return status;
	}

	public void send(String command) {
		commandWriter.println(command);
		commandWriter.flush();
	}

	public void close() {
		try {
			robotSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			String line;
			while ((line = responseReader.readLine()) != null) {
				System.out.println("Robot " + name + " says: " + line);

				final String prefix = "[PLANET-RESPONSE] ";
				if (line.startsWith(prefix)) {
					line = line.substring(prefix.length());
				}

				try {
					JSONObject json = new JSONObject(line);

					String cmd = json.optString("CMD", "").toLowerCase();
					if (cmd.equals("init")) {
						JSONObject sizeObj = json.getJSONObject("SIZE");
						int width = sizeObj.getInt("WIDTH");
						int height = sizeObj.getInt("HEIGHT");
						System.out.println("Received init from Robot: Planet size is " + width + " x " + height);
						if (width == 10 && height == 6) {
							planetId = "DefaultPlanet";
						}
						if (!dbManager.planetExists(planetId)) {
							dbManager.insertPlanet(planetId, width, height);
						}

					} else {
						processRobotResponse(json.toString());
					}
				} catch (Exception e) {
					System.err.println("Could not parse JSON: " + line);
				}
			}

			System.out.println("Robot " + name + " disconnected or stream ended.");

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			groundStation.removeRobot(this);
			close();
		}
	}

	private void processRobotResponse(String response) {
		System.out.println("Robot " + name + " response: " + response);
		JSONObject jsonResponse = new JSONObject(response);

		// Beispiel: Wenn der Roboter "CMD":"landed" schickt => Status auf online
		if (jsonResponse.optString("CMD").equalsIgnoreCase("landed")) {
			groundStation.updateRobotStatusToOnline(name);
			System.out.println("Robot '" + name + "' is now ONLINE.");
		}
		if (jsonResponse.optString("CMD").equalsIgnoreCase("data")
				&& !dbManager.positionExists(planetId, jsonResponse.optInt("X"), jsonResponse.optInt("Y"))) {
			dbManager.insertPosition(planetId, name, jsonResponse.optInt("X"), jsonResponse.optInt("Y"),
					jsonResponse.optString("GROUND"), jsonResponse.optDouble("TEMP"));
		}

		
		if (jsonResponse.optString("CMD").equalsIgnoreCase("moved")) {
	        JSONObject position = jsonResponse.getJSONObject("POSITION");
	        int x = position.getInt("X");
	        int y = position.getInt("Y");
	        String direction = position.getString("DIRECTION");

	        // Save the new position in the database (without terrain and temperature)
	        dbManager.updateRobotPosition(name, x, y, direction);
	        System.out.println("Robot '" + name + "' moved to (" + x + "," + y + ") facing " + direction);
	    }
		
		// ... andere Fälle hier ...
	}

	private void processRobotRequest(String request) {
		System.out.println("Robot " + name + " request: " + request);

		// TODO move all robot requests to here
	}
}