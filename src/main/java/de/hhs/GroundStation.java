package de.hhs;

import de.hhs.webserver.WebServer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

// Server, der mehrere Roboter verwaltet
public class GroundStation {
	private final int port;
	private Set<RobotSession> robots;
	private DatabaseManager dbManager;
	private String lastPreparedRobotName;

	public GroundStation(int port) {
		this.port = port;
		this.robots = new HashSet<>();
		this.dbManager = new DatabaseManager(); // Datenbankmanager initialisieren
	}

	public synchronized void addRobot(RobotSession session, String robotName, String status) {
		robots.add(session);
		dbManager.insertRobot(robotName, status);
		System.out.println("New robot added: " + robotName);
	}

	public synchronized void sendToRobot(String name, String command) {
		for (RobotSession robot : robots) {
			if (robot.getName().equals(name)) {
				robot.send(command);
				break;
			}
		}
	}

	public synchronized void removeRobot(RobotSession session) {
		robots.remove(session);
		dbManager.updateRobotStatus(session.getName(), "offline");
		System.out.println("Robot removed: " + session.getName());
	}

	public synchronized void updateRobotStatusToOnline(String name) {
		dbManager.updateRobotStatus(name, "online");
	}

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

	// Ergänzung der Methode zum Hinzufügen von Roboter in GroundStation

	public void prepareSession(String robotName, String status) {
		// Nimmt nur die Registrierung vor. Die echte Socket-Session wird durch den Listener akzeptiert.
		this.lastPreparedRobotName = robotName;
		dbManager.insertRobot(robotName, "waiting");
		System.out.println("Prepared session for Robot: " + robotName + " with status: " + status);
	}

	// Startet den ServerSocket zum dauerhaften Hören auf Verbindungen
	public void startListening() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Ground Station listening on port " + port);
			while (true) {
				try {
					Socket robotSocket = serverSocket.accept();
					System.out.println("Robot connected!");
					handleNewConnection(robotSocket);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void handleNewConnection(Socket robotSocket) {
		if (lastPreparedRobotName == null) {
			System.out.println("No prepared robot name. Closing socket.");
			try { robotSocket.close(); } catch (IOException e) { e.printStackTrace(); }
			return;
		}

		String status = "waiting";
		RobotSession session = new RobotSession(this, robotSocket, lastPreparedRobotName, status);

		addRobot(session, session.getName(), status);

		new Thread(session).start();

		lastPreparedRobotName = null;
	}

	public static void main(String[] args) {
		int port = 9000;
		GroundStation station = new GroundStation(port);

		// Startet den HTTP WebServer getrennt in einem anderen Thread
		new Thread(() -> new WebServer(station).start()).start();

		// Startet den ServerSocket-Listener
		station.startListening();
	}
}