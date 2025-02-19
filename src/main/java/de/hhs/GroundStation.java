package de.hhs;

import de.hhs.webserver.WebServer;
import org.json.JSONObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

// Server that manages multiple robots
public class GroundStation {

	private int port; // Port where the ground station listens for robot connections
	private Set<RobotSession> robots; // Active robot sessions

	public GroundStation(int port) {
		this.port = port;
		this.robots = new HashSet<>();
	}

	// Registers a new robot by adding a RobotSession to the set
	public synchronized void addRobot(RobotSession session) {
		robots.add(session);
		System.out.println("New robot added: " + session.getName());
	}

	// Sends a command to the specified robot
	public synchronized void sendToRobot(String name, String command) {
		for (RobotSession robot : robots) {
			if (robot.getName().equals(name)) {
				robot.send(command);
				break;
			}
		}
	}

	// Removes a robot session from the set
	public synchronized void removeRobot(RobotSession session) {
		robots.remove(session);
		System.out.println("Robot removed: " + session.getName());
	}

	// Closes all robot connections and clears the set
	public synchronized void shutdown() {
		for (RobotSession robot : robots) {
			robot.close();
		}
		robots.clear();
		System.out.println("Ground station shut down.");
	}

	public Set<RobotSession> getRobots() {
		return robots;
	}

	// Entry point
	public static void main(String[] args) {
		new Thread(WebServer::start).start();

		int port = 9000;
		GroundStation station = new GroundStation(port);

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Ground Station listening on port " + port);

			// **Schritt 1: Warte auf den ersten Roboter**
			System.out.println("Waiting for the first robot to connect...");
			Socket robotSocket = serverSocket.accept();
			System.out.println("Robot connected!");

			// **Schritt 2: Benutzer fragt nach einem Namen**
			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter the robot's name: ");
			String robotName = scanner.nextLine();

			// **Schritt 3: Sende `init`-Befehl**
			PrintWriter out = new PrintWriter(robotSocket.getOutputStream(), true);
			JSONObject initCommand = new JSONObject();
			initCommand.put("CMD", "init");
			initCommand.put("NAME", robotName);
			out.println(initCommand.toString());

			System.out.println("Robot initialized as: " + robotName);

			// **Schritt 4: Starte RobotSession**
			RobotSession session = new RobotSession(station, robotSocket);
			station.addRobot(session);
			new Thread(session).start();

			// **Schritt 5: Weitere Roboter akzeptieren**
			while (true) {
				Socket clientSocket = serverSocket.accept();
				RobotSession newSession = new RobotSession(station, clientSocket);
				station.addRobot(newSession);
				new Thread(newSession).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
