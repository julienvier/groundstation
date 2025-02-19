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

	private int port;
	private Set<RobotSession> robots;
	private DatabaseManager dbManager;

	public GroundStation(int port) {
		this.port = port;
		this.robots = new HashSet<>();
		this.dbManager = new DatabaseManager(); // Datenbankmanager initialisieren
	}

	// Registriert neuen Roboter und setzt Status auf "waiting"
	public synchronized void addRobot(RobotSession session, String robotName) {
		robots.add(session);
		dbManager.insertRobot(robotName, "waiting"); // Roboter in DB setzen
		System.out.println("New robot added: " + robotName);
	}

	// Sendet Befehl an einen bestimmten Roboter
	public synchronized void sendToRobot(String name, String command) {
		for (RobotSession robot : robots) {
			if (robot.getName().equals(name)) {
				robot.send(command);
				break;
			}
		}
	}

	// Entfernt Roboter und setzt Status auf "offline"
	public synchronized void removeRobot(RobotSession session) {
		robots.remove(session);
		dbManager.updateRobotStatus(session.getName(), "offline");
		System.out.println("Robot removed: " + session.getName());
	}

	// Setzt Status auf "online", wenn Roboter landet
	public synchronized void updateRobotStatusToOnline(String name) {
		dbManager.updateRobotStatus(name, "online");
	}

	// Setzt Status auf "offline" beim Shutdown
	public synchronized void shutdown() {
		for (RobotSession robot : robots) {
			dbManager.updateRobotStatus(robot.getName(), "offline");
			robot.close();
		}
		robots.clear();
		System.out.println("Ground station shut down.");
	}

	public Set<RobotSession> getRobots() {
		return robots;
	}

	public static void main(String[] args) {
		new Thread(WebServer::start).start();
		int port = 9000;
		GroundStation station = new GroundStation(port);

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Ground Station listening on port " + port);

			System.out.println("Waiting for the first robot to connect...");
			Socket robotSocket = serverSocket.accept();
			System.out.println("Robot connected!");

			Scanner scanner = new Scanner(System.in);
			System.out.print("Enter the robot's name: ");
			String robotName = scanner.nextLine();

			// `init`-Befehl senden
			PrintWriter out = new PrintWriter(robotSocket.getOutputStream(), true);
			JSONObject initCommand = new JSONObject();
			initCommand.put("CMD", "init");
			initCommand.put("NAME", robotName);
			out.println(initCommand.toString());

			// Roboter in der Datenbank als "waiting" hinzufügen
			RobotSession session = new RobotSession(station, robotSocket);
			station.addRobot(session, robotName);
			new Thread(session).start();

			// Weitere Roboter akzeptieren
			while (true) {
				Socket clientSocket = serverSocket.accept();
				RobotSession newSession = new RobotSession(station, clientSocket);
				station.addRobot(newSession, newSession.getName());
				new Thread(newSession).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
