package ch.popsim.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Citizen {
    private String icon;
    private String firstname;
    private String lastname;
    private Location home;
    private List<ScheduleEvent> schedule;

    // Simulation state
    private transient boolean travelling;
    private transient Location currentLocation;
    private transient Location departureLocation;
    private transient Location destinationLocation;
    private transient Coordinate currentPosition;
    private transient Coordinate travelStartPosition;
    private transient int travelElapsedMinutes;
    private transient int travelTotalMinutes;
    private transient String currentActivity;
    private transient int currentEventIndex;

    public Citizen() {
        this("👨", "New", "Citizen", null, new ArrayList<>());
    }

    public Citizen(String icon, String firstname, String lastname, Location home, List<ScheduleEvent> schedule) {
        this.icon = icon;
        this.firstname = firstname;
        this.lastname = lastname;
        this.home = home;
        this.schedule = schedule != null ? schedule : new ArrayList<>();
        this.travelling = false;
        this.currentActivity = "";
        this.currentEventIndex = -1;
    }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public Location getHome() { return home; }
    public void setHome(Location home) { this.home = home; }
    public List<ScheduleEvent> getSchedule() { return schedule; }
    public void setSchedule(List<ScheduleEvent> schedule) { this.schedule = schedule; }

    public boolean isTravelling() { return travelling; }
    public void setTravelling(boolean travelling) { this.travelling = travelling; }
    public Location getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(Location currentLocation) { this.currentLocation = currentLocation; }
    public Location getDepartureLocation() { return departureLocation; }
    public void setDepartureLocation(Location departureLocation) { this.departureLocation = departureLocation; }
    public Location getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(Location destinationLocation) { this.destinationLocation = destinationLocation; }
    public Coordinate getCurrentPosition() { return currentPosition; }
    public void setCurrentPosition(Coordinate currentPosition) { this.currentPosition = currentPosition; }
    public int getTravelElapsedMinutes() { return travelElapsedMinutes; }
    public void setTravelElapsedMinutes(int travelElapsedMinutes) { this.travelElapsedMinutes = travelElapsedMinutes; }
    public int getTravelTotalMinutes() { return travelTotalMinutes; }
    public void setTravelTotalMinutes(int travelTotalMinutes) { this.travelTotalMinutes = travelTotalMinutes; }
    public String getCurrentActivity() { return currentActivity; }
    public void setCurrentActivity(String currentActivity) { this.currentActivity = currentActivity; }
    public Coordinate getTravelStartPosition() { return travelStartPosition; }
    public void setTravelStartPosition(Coordinate travelStartPosition) { this.travelStartPosition = travelStartPosition; }
    public int getCurrentEventIndex() { return currentEventIndex; }
    public void setCurrentEventIndex(int currentEventIndex) { this.currentEventIndex = currentEventIndex; }

    public void sortSchedule() {
        schedule.sort(Comparator.comparing(ScheduleEvent::getTimeInMinutes));
    }

    public void resetSimulationState() {
        this.travelling = false;
        this.currentLocation = home;
        this.departureLocation = null;
        this.destinationLocation = null;
        this.currentPosition = home != null ? home.getCoordinate().copy() : new Coordinate(0, 0);
        this.travelStartPosition = null;
        this.travelElapsedMinutes = 0;
        this.travelTotalMinutes = 0;
        this.currentActivity = home != null ? "At " + home.getName() : "";
        this.currentEventIndex = -1;
    }

    public String getStatusText() {
        if (travelling) {
            return "Travelling to " + (destinationLocation != null ? destinationLocation.toString() : "?");
        } else if (currentLocation != null) {
            String activity = currentActivity != null && !currentActivity.isEmpty() ? currentActivity : "";
            return activity + " at " + currentLocation.toString();
        }
        return "Idle";
    }

    public String getDisplayName() {
        return icon + " " + firstname + " " + lastname;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
