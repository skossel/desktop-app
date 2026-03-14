package ch.popsim.model;

import java.time.LocalTime;

public class ScheduleEvent {
    private LocalTime time;
    private Location targetLocation;
    private String activity;

    public ScheduleEvent() {
        this(LocalTime.of(8, 0), null, "");
    }

    public ScheduleEvent(LocalTime time, Location targetLocation, String activity) {
        this.time = time;
        this.targetLocation = targetLocation;
        this.activity = activity;
    }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }
    public Location getTargetLocation() { return targetLocation; }
    public void setTargetLocation(Location targetLocation) { this.targetLocation = targetLocation; }
    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public int getTimeInMinutes() {
        return time.getHour() * 60 + time.getMinute();
    }

    /**
     * Calculate travel time in minutes from departure location to target location.
     * Speed = 5 km/h = 5000m / 60min ≈ 83.33 m/min
     */
    public int calculateTravelTime(Location departureLocation) {
        if (departureLocation == null || targetLocation == null) return 0;
        if (departureLocation == targetLocation ||
            departureLocation.getName().equals(targetLocation.getName())) return 0;
        double distance = departureLocation.getCoordinate().distanceTo(targetLocation.getCoordinate());
        double speedPerMinute = 5000.0 / 60.0;
        return (int) Math.ceil(distance / speedPerMinute);
    }

    /**
     * Calculate spend time = time until next event starts - travel time.
     * @param nextEventTimeMinutes the start time of the next event in minutes from midnight
     * @param departureLocation where the citizen departs from
     */
    public int calculateSpendTime(int nextEventTimeMinutes, Location departureLocation) {
        int travelTime = calculateTravelTime(departureLocation);
        int totalTime = calculateTotalTime(nextEventTimeMinutes);
        return Math.max(0, totalTime - travelTime);
    }

    /**
     * Calculate total time = time from this event start to next event start.
     */
    public int calculateTotalTime(int nextEventTimeMinutes) {
        int thisMinutes = getTimeInMinutes();
        int diff = nextEventTimeMinutes - thisMinutes;
        if (diff <= 0) diff += 24 * 60; // wrap around midnight
        return diff;
    }

    public static String formatDuration(int totalMinutes) {
        int h = totalMinutes / 60;
        int m = totalMinutes % 60;
        return String.format("%dh%02d", h, m);
    }

    @Override
    public String toString() {
        return String.format("%s → %s (%s)",
            time.toString(),
            targetLocation != null ? targetLocation.getName() : "?",
            activity);
    }
}
