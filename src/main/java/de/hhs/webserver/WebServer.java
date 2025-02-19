package de.hhs.webserver;

import de.hhs.DatabaseManager;
import io.javalin.Javalin;
import org.json.JSONArray;
import org.json.JSONObject;

public class WebServer {
	public static void start() {
		DatabaseManager dbManager = new DatabaseManager();
		Javalin app = Javalin.create().start(8088);

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

		app.post("/api/planets", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String name = body.getString("name");
			int height = body.getInt("height");
			int width = body.getInt("width");
			dbManager.insertPlanet(name, height, width);
			ctx.status(201).json("{\"message\": \"Planet added\"}");
		});

		app.post("/api/robots", ctx -> {
			JSONObject body = new JSONObject(ctx.body());
			String status = body.getString("status");
			dbManager.insertRobot(status);
			ctx.status(201).json("{\"message\": \"Robot added\"}");
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
