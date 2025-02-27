package de.hhs;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import de.hhs.webserver.WebServer;
import org.json.JSONObject;

// Server, der mehrere Roboter verwaltet
public class GroundStation {
	private final int port;
	private Set<RobotSession> robots;
	private DatabaseManager dbManager;
	private String lastPreparedRobotName;
	private final Set<Socket> waitingSockets = new HashSet<>();

	public GroundStation(int port) {
		this.port = port;
		this.robots = new HashSet<>();
		this.dbManager = new DatabaseManager(); // Datenbankmanager initialisieren
	}

	public synchronized void addRobot(RobotSession session, String robotName, String status) {
		robots.add(session);

		// Only insert if the robot doesn't exist, else just update
		if (!dbManager.robotExists(robotName)) {
			dbManager.insertRobot(robotName, status);
		} else {
			dbManager.updateRobotStatus(robotName, status);
		}

		System.out.println("New robot added/updated: " + robotName);
	}

	public synchronized void sendToRobot(String name, String command) {
		for (RobotSession robot : robots) {
			if (robot.getName().equals(name)) {
				robot.send(command);
				break;
			}
		}
	}

	public void landRobot(String name, int x, int y) {
		JSONObject landingCommand = new JSONObject();
		landingCommand.put("CMD", "land");
		landingCommand.put("MESSAGE", "land|" + x + "|" + y + "|NORTH");

		sendToRobot(name, landingCommand.toString());
		System.out.println("Sent landing command to robot " + name + ": " + landingCommand);
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

	public void prepareSessionForAddingRobot(String robotName, String status) {
		this.lastPreparedRobotName = robotName;
		dbManager.insertRobot(robotName, status);
		System.out.println("Prepared session for Robot: " + robotName + " with status: " + status);

		// Prüfe, ob eine wartende Verbindung existiert:
		Socket waitingSocket = null;
		synchronized (waitingSockets) {
			if (!waitingSockets.isEmpty()) {
				// Hole eine beliebige wartende Verbindung
				waitingSocket = waitingSockets.iterator().next();
				waitingSockets.remove(waitingSocket);
			}
		}
		if (waitingSocket != null) {
			// Bearbeite diese Verbindung jetzt
			System.out.println("Assigning a waiting connection to robot: " + robotName);
			handleNewConnection(waitingSocket);
		}
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
			System.out.println("No prepared robot name. Storing connection in waiting pool.");
			synchronized (waitingSockets) {
				waitingSockets.add(robotSocket);
			}
			return;
		}

		// Es gibt einen vorbereiteten Namen – bearbeite diese Verbindung:
		String status = "waiting";
		RobotSession session = new RobotSession(this, robotSocket, lastPreparedRobotName, status);
		addRobot(session, session.getName(), status);

		try {
			PrintWriter writer = new PrintWriter(robotSocket.getOutputStream(), true);
			String json = "{\"name\":\"" + lastPreparedRobotName + "\"}";
			writer.println(json);
			writer.flush();
			System.out.println("Sent JSON to RemoteRobot: " + json);
		} catch (IOException e) {
			e.printStackTrace();
		}

		new Thread(session).start();
		lastPreparedRobotName = null;
	}

	public static String generateUUID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
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