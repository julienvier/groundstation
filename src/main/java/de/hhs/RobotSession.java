package de.hhs;

import java.io.*;
import java.net.Socket;
//manages communication between robot & ground station
//each robot session runs in a separate thread
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
            // Initializing robot session name
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

            // Der Roboter sollte beim ersten Verbindungsaufbau seinen Namen senden
            if (name == null || name.isEmpty()) {
                name = responseReader.readLine(); // Erwartet den Namen vom Roboter
                System.out.println("Robot registered with name: " + name);
            }

            // Roboter-ID aus der Datenbank holen oder neu anlegen
            int roboterID = dbManager.getOrCreateRoboter(name, "AKTIV");
            int planetID = 1; // Falls du mehrere Planeten hast, passe dies an.

            String response;
            while ((response = responseReader.readLine()) != null) {
                System.out.println("Robot " + name + " response: " + response);

                // Falls Roboter eine Messung sendet ("scaned:MEASURE|SAND|25.3")
                if (response.startsWith("scaned:MEASURE|")) {
                    String[] parts = response.split("\\|");
                    if (parts.length == 3) {
                        String ground = parts[1];
                        float temperature = Float.parseFloat(parts[2]);

                        // Position des Roboters abrufen
                        send("getpos");
                        String posResponse = responseReader.readLine();

                        if (posResponse.startsWith("pos:POSITION|")) {
                            String[] posParts = posResponse.split("\\|");
                            if (posParts.length == 4) {
                                int x = Integer.parseInt(posParts[1]);
                                int y = Integer.parseInt(posParts[2]);

                                // Daten in die Datenbank speichern
                                dbManager.insertPosition(planetID, roboterID, x, y, ground);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            groundStation.abandonRobot(this);
            close();
        }
    }
}
//class summary:
//manages commands & responses
//Thread-based
//Retrieves robot name - reads 1st message as robot's name
//sends commands, receives responses, handles disconnection