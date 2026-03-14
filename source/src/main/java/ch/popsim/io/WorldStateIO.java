package ch.popsim.io;

import ch.popsim.model.*;
import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class WorldStateIO {

    public static WorldState load(File file) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            WorldState world = new WorldState();

            // Parse locations
            List<Location> locations = new ArrayList<>();
            JsonArray locArray = root.getAsJsonArray("Locations");
            if (locArray != null) {
                for (JsonElement el : locArray) {
                    JsonObject obj = el.getAsJsonObject();
                    String icon = obj.get("Icon").getAsString();
                    String name = obj.get("Name").getAsString();
                    JsonObject coord = obj.getAsJsonObject("Coord");
                    double x = coord.get("X").getAsDouble();
                    double y = coord.get("Y").getAsDouble();
                    locations.add(new Location(icon, name, new Coordinate(x, y)));
                }
            }
            world.setLocations(locations);

            // Parse citizens
            List<Citizen> citizens = new ArrayList<>();
            JsonArray citArray = root.getAsJsonArray("Citizens");
            if (citArray != null) {
                for (JsonElement el : citArray) {
                    JsonObject obj = el.getAsJsonObject();
                    String icon = obj.get("Icon").getAsString();
                    String firstname = obj.get("Firstname").getAsString();
                    String lastname = obj.get("Lastname").getAsString();
                    String homeName = obj.get("Home").getAsString();
                    Location home = world.findLocationByName(homeName);

                    List<ScheduleEvent> schedule = new ArrayList<>();
                    JsonArray schedArray = obj.getAsJsonArray("Schedule");
                    if (schedArray != null) {
                        for (JsonElement se : schedArray) {
                            JsonObject sObj = se.getAsJsonObject();
                            String timeStr = sObj.get("Time").getAsString();
                            LocalTime time = LocalTime.parse(timeStr);
                            String locName = sObj.get("Location").getAsString();
                            Location targetLoc = world.findLocationByName(locName);
                            String activity = sObj.get("Activity").getAsString();
                            schedule.add(new ScheduleEvent(time, targetLoc, activity));
                        }
                    }

                    Citizen citizen = new Citizen(icon, firstname, lastname, home, schedule);
                    citizen.sortSchedule();
                    citizens.add(citizen);
                }
            }
            world.setCitizens(citizens);
            world.resetAllCitizens();

            return world;
        }
    }

    public static void save(WorldState world, File file) throws IOException {
        JsonObject root = new JsonObject();

        // Serialize locations
        JsonArray locArray = new JsonArray();
        for (Location loc : world.getLocations()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("Icon", loc.getIcon());
            obj.addProperty("Name", loc.getName());
            JsonObject coord = new JsonObject();
            coord.addProperty("X", loc.getCoordinate().getX());
            coord.addProperty("Y", loc.getCoordinate().getY());
            obj.add("Coord", coord);
            locArray.add(obj);
        }
        root.add("Locations", locArray);

        // Serialize citizens
        JsonArray citArray = new JsonArray();
        for (Citizen c : world.getCitizens()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("Icon", c.getIcon());
            obj.addProperty("Firstname", c.getFirstname());
            obj.addProperty("Lastname", c.getLastname());
            obj.addProperty("Home", c.getHome() != null ? c.getHome().getName() : "");

            JsonArray schedArray = new JsonArray();
            for (ScheduleEvent se : c.getSchedule()) {
                JsonObject sObj = new JsonObject();
                sObj.addProperty("Time", se.getTime().toString());
                sObj.addProperty("Location", se.getTargetLocation() != null ? se.getTargetLocation().getName() : "");
                sObj.addProperty("Activity", se.getActivity());
                schedArray.add(sObj);
            }
            obj.add("Schedule", schedArray);
            citArray.add(obj);
        }
        root.add("Citizens", citArray);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(root, writer);
        }
    }
}
