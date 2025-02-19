package de.hhs;

import de.hhs.webserver.WebServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

// Server that manages multiple robots
public class GroundStation {

	private int port; // Port number where the ground station listens for robot connections
	private Set<RobotSession> robots; // Set that stores active RobotSession instances

	public GroundStation(int port) {
		this.port = port;
		this.robots = new HashSet<>();
	}

	// Registers a new robot by adding a new RobotSession to the robot set
	public synchronized void addRobot(RobotSession session) {
		robots.add(session);
		System.out.println("New robot added: " + session.getName());
	}

	// Sends a required command to the specified robot
	public synchronized void sendToRobot(String name, String command) {
		for (RobotSession robot : robots) {
			if (robot.getName().equals(name)) {
				robot.send(command);
				break;
			}
		}
	}

	// Removes a robot session from the robot set
	public synchronized void removeRobot(RobotSession session) {
		robots.remove(session);
		System.out.println("Robot removed: " + session.getName());
	}

	// Closes all robot connections and clears the list of robots
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

	// Entry point for the server
	public static void main(String[] args) {

		new Thread(WebServer::start).start();

		int port = 9000;
		GroundStation station = new GroundStation(port);

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Ground Station listening on port " + port);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				RobotSession session = new RobotSession(station, clientSocket);
				station.addRobot(session);
				new Thread(session).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
