package de.hhs.webserver;

import de.hhs.DatabaseManager;
import de.hhs.GroundStation;
import org.json.JSONArray;
import org.json.JSONObject;
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

		app.get("/api/robots", ctx -> {
			JSONArray robots = dbManager.getAllRobots();
			ctx.json(robots.toString());
		});

		app.get("/api/positions", ctx -> {
			JSONArray positions = dbManager.getAllPositions();
			ctx.json(positions.toString());
		});

		app.get("/api/testplanet", ctx -> {
			String uuid = GroundStation.generateUUID();
			JSONArray planets = dbManager.insertPlanet(uuid, 0, 0);
			if (planets == null) {
				ctx.status(500).result("Failed to insert planet");
			} else {
				ctx.json(planets.toString());
			}
		});

		app.post("/api/robots", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String name = body.getString("name");
			String status = body.getString("status");

			// Add the robot to the ground station
			groundStation.prepareSession(name, status);

			ctx.status(201).json("{\"message\": \"Robot added, waiting for connection\"}");
		});

		app.post("/api/positions", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			int planetID = body.getInt("planetID");
			int robotID = body.getInt("robotID");
			int x = body.getInt("x");
			int y = body.getInt("y");
			String terrain = body.getString("terrain");
			dbManager.insertPosition(planetID, robotID, x, y, terrain);
			ctx.status(201).json("{\"message\": \"Position saved\"}");
		});
	}
}