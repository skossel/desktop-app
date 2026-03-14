package ch.popsim.model;

public class Location {
    private String icon;
    private String name;
    private Coordinate coordinate;

    public Location() {
        this("🏠", "New Location", new Coordinate(5000, 5000));
    }

    public Location(String icon, String name, Coordinate coordinate) {
        this.icon = icon;
        this.name = name;
        this.coordinate = coordinate;
    }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Coordinate getCoordinate() { return coordinate; }
    public void setCoordinate(Coordinate coordinate) { this.coordinate = coordinate; }

    @Override
    public String toString() {
        return icon + " " + name;
    }
}
