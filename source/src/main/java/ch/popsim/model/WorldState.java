package ch.popsim.model;

import java.util.ArrayList;
import java.util.List;

public class WorldState {
    private List<Location> locations;
    private List<Citizen> citizens;

    public WorldState() {
        this.locations = new ArrayList<>();
        this.citizens = new ArrayList<>();
    }

    public WorldState(List<Location> locations, List<Citizen> citizens) {
        this.locations = locations != null ? locations : new ArrayList<>();
        this.citizens = citizens != null ? citizens : new ArrayList<>();
    }

    public List<Location> getLocations() { return locations; }
    public void setLocations(List<Location> locations) { this.locations = locations; }
    public List<Citizen> getCitizens() { return citizens; }
    public void setCitizens(List<Citizen> citizens) { this.citizens = citizens; }

    public Location findLocationByName(String name) {
        if (name == null) return null;
        for (Location loc : locations) {
            if (name.equals(loc.getName())) return loc;
        }
        return null;
    }

    public boolean isLocationNameUnique(String name, Location exclude) {
        for (Location loc : locations) {
            if (loc != exclude && name.equals(loc.getName())) return false;
        }
        return true;
    }

    public void resetAllCitizens() {
        for (Citizen c : citizens) {
            c.resetSimulationState();
        }
    }
}
