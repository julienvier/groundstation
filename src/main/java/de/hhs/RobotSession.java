package de.hhs;

import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

// Manages communication between the robot & the ground station
// Each robot session runs in a separate thread
public class RobotSession implements Runnable {
	private GroundStation groundStation;
	private Socket robotSocket;
	private PrintWriter commandWriter;
	private BufferedReader responseReader;
	private String name;
	private String status;

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
			// Always send the init command, regardless of name
			sendInitialCommands();

			// Roboterantworten lesen
			String response;
			while ((response = responseReader.readLine()) != null) {
				processRobotResponse(response);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			groundStation.removeRobot(this);
			close();
		}
	}

	private void sendInitialCommands() {
		// Initialisiere den Roboter oder führe programmatische Aktionen aus
		JSONObject initCommand = new JSONObject();
		initCommand.put("NAME", name);
		send(initCommand.toString());
	}

	private void processRobotResponse(String response) {
		System.out.println("Robot " + name + " response: " + response);
		JSONObject jsonResponse = new JSONObject(response);

		// Beispiel: Wenn der Roboter landet, aktualisiere den Status
		if (jsonResponse.getString("CMD").equalsIgnoreCase("landed")) {
			groundStation.updateRobotStatusToOnline(name);
			System.out.println("Robot '" + name + "' is now ONLINE.");
		}
		// Weitere Befehle und Bedingungen hier implementieren...
	}
}