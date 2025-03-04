package de.hhs.webserver;

import org.json.JSONArray;
import org.json.JSONObject;

import de.hhs.DatabaseManager;
import de.hhs.GroundStation;
import io.javalin.Javalin;

public class WebServer {
	private final GroundStation groundStation;
	private final DatabaseManager dbManager = new DatabaseManager();

	public WebServer(GroundStation groundStation) {
		this.groundStation = groundStation;
	}

	public void start() {
		Javalin app = Javalin.create(config -> {
			config.plugins.enableCors(cors -> cors.add(it -> it.anyHost()));
		}).start(8088);

		app.get("/api/planets", ctx -> {
			JSONArray planets = dbManager.getAllPlanets();
			ctx.json(planets.toString());
		});

		app.get("/api/planet-size", ctx -> {
			JSONObject planetSize = dbManager.getLatestPlanetSize();
			ctx.json(planetSize.toString());
		});

		app.get("/api/robots", ctx -> {
			JSONArray robots = dbManager.getAllRobots();
			ctx.json(robots.toString());
		});

		app.get("/api/positions", ctx -> {
			JSONArray positions = dbManager.getAllPositions();
			ctx.json(positions.toString());
		});

		app.post("/api/land", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			int x = body.getInt("x");
			int y = body.getInt("y");
			String name = body.getString("robotID");
			String direction = body.getString("robotDirection");

			groundStation.landRobot(name, x, y, direction);
		});
		
		app.post("/api/move", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String name = body.getString("robotID");
			groundStation.moveRobot(name);
		});
		
		app.post("/api/right", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String name = body.getString("robotID");
			groundStation.rotateRobotRight(name);
		});
		
		app.post("/api/left", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String name = body.getString("robotID");
			groundStation.rotateRobotLeft(name);
		});
		
		app.post("/api/scan", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String name = body.getString("robotID");
			groundStation.scanRobot(name);
		});

		app.post("/api/explore", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String name = body.getString("robotID");
			groundStation.exploreRobot(name);
		});
		
		app.post("/api/robots", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String name = body.getString("name");
			String status = body.getString("status");

			// Add the robot to the ground station
			groundStation.prepareSessionForAddingRobot(name, status);

			ctx.status(201).json("{\"message\": \"Robot added, waiting for connection\"}");
		});

		app.post("/api/currentRobot", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String robotName = body.getString("name");
			groundStation.setCurrentRobot(robotName);
			ctx.status(200).json("{\"message\":\"Robot " + robotName + " is now active.\"}");
		});

	}
}