package ch.popsim.sim;

import ch.popsim.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SimulationEngine {
    private static final double SPEED_M_PER_MIN = 5000.0 / 60.0; // 5 km/h in m/min

    private WorldState world;
    private LocalDateTime currentTime;

    public SimulationEngine() {
        this.currentTime = LocalDate.now().atStartOfDay();
    }

    public void setWorld(WorldState world) {
        this.world = world;
        this.currentTime = LocalDate.now().atStartOfDay();
        if (world != null) {
            world.resetAllCitizens();
        }
    }

    public WorldState getWorld() { return world; }
    public LocalDateTime getCurrentTime() { return currentTime; }
    public void setCurrentTime(LocalDateTime currentTime) { this.currentTime = currentTime; }

    public void step() {
        if (world == null) return;

        // Advance time by 1 minute
        currentTime = currentTime.plusMinutes(1);
        int currentMinuteOfDay = currentTime.getHour() * 60 + currentTime.getMinute();

        for (Citizen citizen : world.getCitizens()) {
            stepCitizen(citizen, currentMinuteOfDay);
        }
    }

    private void stepCitizen(Citizen citizen, int currentMinuteOfDay) {
        List<ScheduleEvent> schedule = citizen.getSchedule();
        if (schedule.isEmpty()) {
            // No schedule: citizen stays at home
            return;
        }

        // Check if a new event starts at current time
        for (int i = 0; i < schedule.size(); i++) {
            ScheduleEvent event = schedule.get(i);
            if (event.getTimeInMinutes() == currentMinuteOfDay) {
                startEvent(citizen, event, i);
                break;
            }
        }

        // If travelling, advance travel
        if (citizen.isTravelling()) {
            advanceTravel(citizen);
        }
    }

    private void startEvent(Citizen citizen, ScheduleEvent event, int eventIndex) {
        Location destination = event.getTargetLocation();
        if (destination == null) return;

        citizen.setCurrentEventIndex(eventIndex);
        citizen.setCurrentActivity(event.getActivity());

        // Determine departure location
        Location departure = citizen.getCurrentLocation(); // may be null if mid-travel

        // Check if already at destination
        if (departure != null && (departure == destination ||
                departure.getName().equals(destination.getName()))) {
            citizen.setTravelling(false);
            citizen.setCurrentLocation(destination);
            citizen.setCurrentPosition(destination.getCoordinate().copy());
            citizen.setDepartureLocation(null);
            citizen.setDestinationLocation(null);
            citizen.setTravelElapsedMinutes(0);
            citizen.setTravelTotalMinutes(0);
            return;
        }

        // Calculate travel time from current position
        Coordinate currentPos = citizen.getCurrentPosition();
        if (currentPos == null) {
            currentPos = citizen.getHome() != null ? citizen.getHome().getCoordinate().copy() : new Coordinate(0, 0);
            citizen.setCurrentPosition(currentPos);
        }
        double distance = currentPos.distanceTo(destination.getCoordinate());
        int travelMinutes = (int) Math.ceil(distance / SPEED_M_PER_MIN);

        if (travelMinutes == 0) {
            citizen.setTravelling(false);
            citizen.setCurrentLocation(destination);
            citizen.setCurrentPosition(destination.getCoordinate().copy());
            return;
        }

        // Store departure position for interpolation
        citizen.setTravelStartPosition(currentPos.copy());
        citizen.setTravelling(true);
        citizen.setDepartureLocation(departure);
        citizen.setDestinationLocation(destination);
        citizen.setCurrentLocation(null); // in transit
        citizen.setTravelElapsedMinutes(0);
        citizen.setTravelTotalMinutes(travelMinutes);
    }

    private void advanceTravel(Citizen citizen) {
        int elapsed = citizen.getTravelElapsedMinutes() + 1;
        citizen.setTravelElapsedMinutes(elapsed);

        Location destination = citizen.getDestinationLocation();
        if (destination == null) {
            citizen.setTravelling(false);
            return;
        }

        // Use stored travel start position for interpolation
        Coordinate startPos = citizen.getTravelStartPosition();
        if (startPos == null) {
            startPos = citizen.getCurrentPosition();
        }

        double totalDistance = startPos.distanceTo(destination.getCoordinate());
        double traveled = elapsed * SPEED_M_PER_MIN;

        if (elapsed >= citizen.getTravelTotalMinutes()) {
            // Arrival step: this step still counts as travelling
            // The citizen arrives but spending begins next step
            citizen.setCurrentPosition(destination.getCoordinate().copy());
            citizen.setTravelling(false);
            citizen.setCurrentLocation(destination);
            citizen.setDepartureLocation(null);
            citizen.setDestinationLocation(null);
            citizen.setTravelStartPosition(null);
        } else {
            // Still travelling - update intermediate position
            Coordinate intermediate = startPos.interpolate(destination.getCoordinate(), traveled, totalDistance);
            citizen.setCurrentPosition(intermediate);
        }
    }
}
