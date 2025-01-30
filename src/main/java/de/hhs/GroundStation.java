package de.hhs;

import de.hhs.webserver.WebServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
//server that manages multiple robots
public class GroundStation {

    private int port;//port number where ground station listens for robot connections
    private Set<RobotSession> robots;//set that stores active RobotSession instances

    public GroundStation(int port) {
        this.port = port;
        this.robots = new HashSet<>();
    }
    //registers a new robot by adding new RobotSession to robots set
    public synchronized void newRobot(RobotSession session) {
        //synchronized ensures thread safety
        //(i.e. only one thread modifies robots at a time)
        robots.add(session);
        System.out.println("New robot added: " + session.getName());
    }
    //sends required command to the required robot
    public synchronized void sendToRobot(String name, String command) {
        for (RobotSession robot : robots) {
            if (robot.getName().equals(name)) {
                robot.send(command);
                break;
            }
        }
    }
    //removes robotSession from robots set
    public synchronized void abandonRobot(RobotSession session) {
        robots.remove(session);
        System.out.println("Robot removed: " + session.getName());
    }
    //closes all robot connections and clears the list of robots
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

    //entry point for the server
    public static void main(String[] args) {

        new Thread(() -> WebServer.start()).start();

        int port = 9000; // Example port
        GroundStation station = new GroundStation(port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Ground Station listening on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                //creates a robotSession
                RobotSession session = new RobotSession(station, clientSocket);
                //registers the robot in the ground station
                station.newRobot(session);
                //each robot runs in its own thread (for simultaneous communication)
                new Thread(session).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
//class summary:
//Acts as server - listens for robot connections
//manages robots
//sends commands
//Thread-safe - uses synchronized to prevent race conditions
//closes all connections before shutdown
