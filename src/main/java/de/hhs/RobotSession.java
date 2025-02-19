package de.hhs;

import java.io.*;
import java.net.Socket;

import org.json.JSONObject;

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
			this.name = responseReader.readLine();
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
		DatabaseManager dbManager = new DatabaseManager(); // **Hier außerhalb des try-Blocks deklarieren**

		try {
			// Falls Name nicht bekannt ist, lese ihn aus der Verbindung
			if (name == null || name.isEmpty()) {
				name = responseReader.readLine();
				System.out.println("Robot registered with name: " + name);

				// Setze initialen Status auf "waiting" in der DB
				dbManager.insertRobot(name, "waiting");
			}

			// Roboter-ID aus der Datenbank holen oder neu anlegen
			int robotID = dbManager.getOrCreateRobot(name, "waiting");
			int planetID = 1; // Falls mehrere Planeten existieren, hier anpassen.

			String response;
			while ((response = responseReader.readLine()) != null) {
				System.out.println("Robot " + name + " response: " + response);
				JSONObject jsonResponse = new JSONObject(response);

				// **Roboter landet => Status auf "online" setzen**
				if (jsonResponse.getString("CMD").equalsIgnoreCase("landed")) {
					dbManager.updateRobotStatus(name, "online");
					System.out.println("Robot '" + name + "' is now ONLINE.");
				}

				// **Scanned-Position speichern**
				if (jsonResponse.getString("CMD").equalsIgnoreCase("scanned")) {
					// Fordere aktuelle Position an
					send("getpos");
					String posResponse = responseReader.readLine();
					JSONObject posJson = new JSONObject(posResponse);

					if (posJson.getString("CMD").equalsIgnoreCase("pos")) {
						int x = posJson.getInt("X");
						int y = posJson.getInt("Y");

						// Speichere Position in der Datenbank
						dbManager.insertPosition(planetID, robotID, x, y, "unknown");
						System.out.println("Position saved: (" + x + ", " + y + ")");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// **Roboter entfernt => Status auf "offline" setzen**
			dbManager.updateRobotStatus(name, "offline"); // Funktioniert jetzt, weil `dbManager` immer verfügbar ist
			groundStation.removeRobot(this);
			System.out.println("Robot '" + name + "' is now OFFLINE.");
			close();
		}
	}
}
