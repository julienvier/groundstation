package de.hhs;

import java.io.*;
import java.net.Socket;

// Manages communication between the robot & the ground station
// Each robot session runs in a separate thread
public class RobotSession implements Runnable {

	private GroundStation groundStation;
	private Socket robotSocket;
	private PrintWriter commandWriter;
	private BufferedReader responseReader;
	private String name;

	public RobotSession(GroundStation groundStation, Socket robotSocket) {
		this.groundStation = groundStation;
		this.robotSocket = robotSocket;
		try {
			this.commandWriter = new PrintWriter(robotSocket.getOutputStream(), true);
			this.responseReader = new BufferedReader(new InputStreamReader(robotSocket.getInputStream()));
			this.name = responseReader.readLine(); // First message expected to be the robot's name
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
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
			DatabaseManager dbManager = new DatabaseManager();

			if (name == null || name.isEmpty()) {
				name = responseReader.readLine();
				System.out.println("Robot registered with name: " + name);
			}

			int robotID = dbManager.getOrCreateRobot(name, "ACTIVE");
			int planetID = 1;

			String response;
			while ((response = responseReader.readLine()) != null) {
				System.out.println("Robot " + name + " response: " + response);

				if (response.startsWith("scanned:MEASURE|")) {
					String[] parts = response.split("\\|");
					if (parts.length == 3) {
						String terrain = parts[1];

						send("getpos");
						String posResponse = responseReader.readLine();

						if (posResponse.startsWith("pos:POSITION|")) {
							String[] posParts = posResponse.split("\\|");
							if (posParts.length == 4) {
								int x = Integer.parseInt(posParts[1]);
								int y = Integer.parseInt(posParts[2]);

								dbManager.insertPosition(planetID, robotID, x, y, terrain);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			groundStation.removeRobot(this);
			close();
		}
	}
}
