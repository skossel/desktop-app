package ch.popsim.model;

public class Coordinate {
    private double x;
    private double y;

    public Coordinate() {
        this(0, 0);
    }

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public double distanceTo(Coordinate other) {
        double dx = other.x - this.x;
        double dy = other.y - this.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public Coordinate interpolate(Coordinate destination, double traveled, double totalDistance) {
        if (totalDistance == 0) return new Coordinate(this.x, this.y);
        double ratio = traveled / totalDistance;
        return new Coordinate(
            this.x + (destination.x - this.x) * ratio,
            this.y + (destination.y - this.y) * ratio
        );
    }

    public Coordinate copy() {
        return new Coordinate(x, y);
    }

    @Override
    public String toString() {
        return String.format("(%.0f, %.0f)", x, y);
    }
}
