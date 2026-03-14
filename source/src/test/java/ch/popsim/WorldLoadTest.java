package ch.popsim;

import ch.popsim.io.WorldStateIO;
import ch.popsim.model.*;
import ch.popsim.sim.SimulationEngine;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class WorldLoadTest {

    private static final String SMALL_JSON = "../instructions/competition/competition/documents/world.small.json";
    private static final String BIG_JSON = "../instructions/competition/competition/documents/world.big.json";

    @Test
    void testLoadSmallWorld() throws Exception {
        File f = new File(SMALL_JSON);
        assertTrue(f.exists(), "world.small.json must exist at " + f.getAbsolutePath());

        WorldState world = WorldStateIO.load(f);
        assertNotNull(world);
        assertEquals(3, world.getLocations().size());
        assertEquals(4, world.getCitizens().size());

        // Check first location
        Location home1 = world.getLocations().get(0);
        assertEquals("Home 1", home1.getName());
        assertEquals("🏠", home1.getIcon());
        assertEquals(6681.0, home1.getCoordinate().getX(), 0.01);
        assertEquals(1409.0, home1.getCoordinate().getY(), 0.01);

        // Check first citizen
        Citizen noah = world.getCitizens().get(0);
        assertEquals("Noah", noah.getFirstname());
        assertEquals("Anderson", noah.getLastname());
        assertEquals("👧", noah.getIcon());
        assertNotNull(noah.getHome());
        assertEquals("Home 2", noah.getHome().getName());
        assertEquals(3, noah.getSchedule().size());

        // Citizens should start at home
        assertNotNull(noah.getCurrentPosition());
        assertEquals(noah.getHome().getCoordinate().getX(), noah.getCurrentPosition().getX(), 0.01);
        assertEquals(noah.getHome().getCoordinate().getY(), noah.getCurrentPosition().getY(), 0.01);

        System.out.println("[DEBUG_LOG] Small world loaded successfully: " + world.getLocations().size() + " locations, " + world.getCitizens().size() + " citizens");
    }

    @Test
    void testLoadBigWorld() throws Exception {
        File f = new File(BIG_JSON);
        assertTrue(f.exists(), "world.big.json must exist at " + f.getAbsolutePath());

        WorldState world = WorldStateIO.load(f);
        assertNotNull(world);
        assertTrue(world.getLocations().size() > 3, "Big world should have many locations");
        assertTrue(world.getCitizens().size() > 4, "Big world should have many citizens");

        System.out.println("[DEBUG_LOG] Big world loaded: " + world.getLocations().size() + " locations, " + world.getCitizens().size() + " citizens");
    }

    @Test
    void testSimulationBasic() throws Exception {
        File f = new File(SMALL_JSON);
        WorldState world = WorldStateIO.load(f);

        SimulationEngine engine = new SimulationEngine();
        engine.setWorld(world);

        // All citizens should start at home
        for (Citizen c : world.getCitizens()) {
            assertFalse(c.isTravelling());
            assertNotNull(c.getCurrentPosition());
        }

        // Run 600 steps (10 hours)
        for (int i = 0; i < 600; i++) {
            engine.step();
        }

        // Time should be 10:00
        assertEquals(10, engine.getCurrentTime().getHour());
        assertEquals(0, engine.getCurrentTime().getMinute());

        System.out.println("[DEBUG_LOG] After 600 steps, time = " + engine.getCurrentTime());
        for (Citizen c : world.getCitizens()) {
            System.out.println("[DEBUG_LOG] " + c.getDisplayName() + " - " + c.getStatusText() +
                    " pos=" + c.getCurrentPosition());
        }
    }

    @Test
    void testDistanceCalculation() {
        Coordinate a = new Coordinate(0, 0);
        Coordinate b = new Coordinate(3000, 4000);
        assertEquals(5000.0, a.distanceTo(b), 0.01);
    }

    @Test
    void testTravelTime() {
        // 5000m at 5km/h = 60 min
        Location a = new Location("A", "A", new Coordinate(0, 0));
        Location b = new Location("B", "B", new Coordinate(3000, 4000));
        ScheduleEvent ev = new ScheduleEvent(java.time.LocalTime.of(8, 0), b, "Go");
        assertEquals(60, ev.calculateTravelTime(a));
    }

    @Test
    void testSaveAndReload() throws Exception {
        File f = new File(SMALL_JSON);
        WorldState world = WorldStateIO.load(f);

        File temp = File.createTempFile("popsim_test", ".json");
        temp.deleteOnExit();
        WorldStateIO.save(world, temp);

        WorldState reloaded = WorldStateIO.load(temp);
        assertEquals(world.getLocations().size(), reloaded.getLocations().size());
        assertEquals(world.getCitizens().size(), reloaded.getCitizens().size());

        // Verify data integrity
        assertEquals(world.getLocations().get(0).getName(), reloaded.getLocations().get(0).getName());
        assertEquals(world.getCitizens().get(0).getFirstname(), reloaded.getCitizens().get(0).getFirstname());

        System.out.println("[DEBUG_LOG] Save/reload test passed");
    }
}
