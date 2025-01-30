package de.hhs.webserver;

import de.hhs.DatabaseManager;
import io.javalin.Javalin;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebServer {
    public static void start() {
        DatabaseManager dbManager = new DatabaseManager();
        Javalin app = Javalin.create().start(8080);

        app.get("/api/planets", ctx -> {
            JSONArray planets = dbManager.getAllPlanets();
            ctx.json(planets.toString());
        });

        app.get("/api/roboter", ctx -> {
            JSONArray roboter = dbManager.getAllRoboter();
            ctx.json(roboter.toString());
        });

        app.get("/api/positions", ctx -> {
            JSONArray positions = dbManager.getAllPositions();
            ctx.json(positions.toString());
        });

        app.post("/api/planets", ctx -> {
            JSONObject body = new JSONObject(ctx.body());
            String name = body.getString("name");
            int höhe = body.getInt("höhe");
            int breite = body.getInt("breite");
            dbManager.insertPlanet(name, höhe, breite);
            ctx.status(201).json("{\"message\": \"Planet hinzugefügt\"}");
        });

        app.post("/api/roboter", ctx -> {
            JSONObject body = new JSONObject(ctx.body());
            String status = body.getString("status");
            dbManager.insertRoboter(status);
            ctx.status(201).json("{\"message\": \"Roboter hinzugefügt\"}");
        });

        app.post("/api/positions", ctx -> {
            JSONObject body = new JSONObject(ctx.body());
            int planetID = body.getInt("planetID");
            int roboterID = body.getInt("roboterID");
            int x = body.getInt("x");
            int y = body.getInt("y");
            String beschaffenheit = body.getString("beschaffenheit");

            dbManager.insertPosition(planetID, roboterID, x, y, beschaffenheit);
            ctx.status(201).json("{\"message\": \"Position gespeichert\"}");
        });
    }
}