package de.hhs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import de.hhs.webserver.WebServer;

public class GroundStation {
	private final int port;
	private Set<RobotSession> robots;
	private DatabaseManager dbManager;
	private String lastPreparedRobotName;
	private final Set<Socket> waitingSockets = new HashSet<>();
	private RobotSession currentRobotSession = null;

	public GroundStation(int port) {
		this.port = port;
		this.robots = new HashSet<>();
		this.dbManager = new DatabaseManager(); 
	}

	public synchronized void addRobot(RobotSession session, String robotName, String status) {
		robots.add(session);

		if (!dbManager.robotExists(robotName)) {
			dbManager.insertRobot(robotName, status);
		} else {
			dbManager.updateRobotStatus(robotName, status);
		}

		System.out.println("New robot added/updated: " + robotName);
	}

	public synchronized void setCurrentRobot(String robotName) {
		for (RobotSession session : robots) {
			if (session.getName().equals(robotName)) {
				currentRobotSession = session;
				System.out.println("Current robot set to: " + robotName);
				return;
			}
		}
		System.out.println("Robot " + robotName + " not found.");
	}

	public synchronized void sendToRobot(String name, String command) {
		for (RobotSession robot : robots) {
			if (robot.getName().equals(name)) {
				robot.send(command);
				break;
			}
		}
	}

	public void landRobot(String name, int x, int y, String direction) {
		JSONObject landingCommand = new JSONObject();
		landingCommand.put("CMD", "land");
		landingCommand.put("MESSAGE", "land|" + x + "|" + y + "|" + direction);

		sendToRobot(name, landingCommand.toString());
		updateOtherRobotPosition(name, x, y);
		System.out.println("Sent landing command to robot " + name + ": " + landingCommand);
	}
	
	public boolean moveRobot(String name) {
	    JSONObject moveCommand = new JSONObject();
	    moveCommand.put("CMD", "move");

	    for (RobotSession robot : robots) {
	        if (robot.getName().equals(name)) {
	            robot.send(moveCommand.toString());
	            System.out.println("Sent move command to robot " + name);
	            return true;
	        }
	    }
	    return false;
	}
	
	public void updateOtherRobotPosition(String name, int x, int y) {
		JSONObject updateCommand = new JSONObject();
		updateCommand.put("CMD", "update");
		updateCommand.put("MESSAGE", "update|" + name + "|" + x + "|" + y);
		
		for (RobotSession robot : robots) {
	        if (!robot.getName().equals(name)) {
	            robot.send(updateCommand.toString());
	            System.out.println("Sent update command to robot " + name);
	        }
	    }
	}
	
	public void rotateRobotRight(String name) {
		JSONObject rotateRightCommand = new JSONObject();
		rotateRightCommand.put("CMD", "rotateright");
		sendToRobot(name, rotateRightCommand.toString());
		System.out.println("Sent rotateRight command to robot " + name + ": " + rotateRightCommand);
	}

	public void rotateRobotLeft(String name) {
		JSONObject rotateLeftCommand = new JSONObject();
		rotateLeftCommand.put("CMD", "rotateleft");

		sendToRobot(name, rotateLeftCommand.toString());
		System.out.println("Sent rotateLeft command to robot " + name + ": " + rotateLeftCommand);
	}

	public void scanRobot(String name) {
		JSONObject scanCommand = new JSONObject();
		scanCommand.put("CMD", "scan");

		sendToRobot(name, scanCommand.toString());
		System.out.println("Sent scan command to robot " + name + ": " + scanCommand);
	}

	public void exploreRobot(String name) {
		JSONObject exploreCommand = new JSONObject();
		exploreCommand.put("CMD", "explore");

		sendToRobot(name, exploreCommand.toString());
		System.out.println("Sent explore command to robot " + name + ": " + exploreCommand);
	}

	public synchronized void disconnectRobot(String name) {
		RobotSession robotToRemove = null;

		for (RobotSession robot : robots) {
			if (robot.getName().equals(name)) {
				robotToRemove = robot;
				break;
			}
		}

		if (robotToRemove != null) {
			try {

				JSONObject disconnectCommand = new JSONObject();
				disconnectCommand.put("CMD", "disconnect");
				robotToRemove.send(disconnectCommand.toString());

				Thread.sleep(1000);

				robotToRemove.close();

				robots.remove(robotToRemove);
				System.out.println("Robot " + name + " successfully disconnected from GroundStation.");

				dbManager.deleteRobotFromDatabase(name);
				System.out.println("Robot " + name + " removed from database.");
			} catch (Exception e) {
				System.err.println("Error while disconnecting robot " + name + ": " + e.getMessage());
			}
		} else {
			System.out.println("Robot " + name + " not found in GroundStation.");
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


	public void prepareSessionForAddingRobot(String robotName, String status) {
		this.lastPreparedRobotName = robotName;
		dbManager.insertRobot(robotName, status);
		System.out.println("Prepared session for Robot: " + robotName + " with status: " + status);

		Socket waitingSocket = null;
		synchronized (waitingSockets) {
			if (!waitingSockets.isEmpty()) {
				waitingSocket = waitingSockets.iterator().next();
				waitingSockets.remove(waitingSocket);
			}
		}
		if (waitingSocket != null) {
			System.out.println("Assigning a waiting connection to robot: " + robotName);
			handleNewConnection(waitingSocket);
		}
	}

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

		try {
			PrintWriter writer = new PrintWriter(robotSocket.getOutputStream(), true);
			JSONObject response = new JSONObject();

			BufferedReader reader = new BufferedReader(new InputStreamReader(robotSocket.getInputStream()));
			String jsonLine = reader.readLine();
			if (jsonLine == null) {
				System.out.println("No registration data received.");
				return;
			}
			JSONObject request = new JSONObject(jsonLine);
			String command = request.optString("CMD", "").toLowerCase();

			if (command.equals("register")) {

				String robotName = lastPreparedRobotName;
				System.out.println("Registering new robot: " + robotName);

				response.put("name", robotName);
				writer.println(response.toString());
				writer.flush();

				RobotSession session = new RobotSession(this, robotSocket, lastPreparedRobotName, "waiting");
				robots.add(session);
				new Thread(session).start();
				lastPreparedRobotName = null;
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		int port = 9000;
		GroundStation station = new GroundStation(port);

		new Thread(() -> new WebServer(station).start()).start();

		station.startListening();
	}
}