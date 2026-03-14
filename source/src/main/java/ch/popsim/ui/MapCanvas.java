package ch.popsim.ui;

import ch.popsim.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class MapCanvas extends Canvas {
    private static final double MAP_SIZE = 10000.0;
    private static final double ICON_SIZE = 16;

    private WorldState world;
    private Citizen selectedCitizen;
    private Location selectedLocation;

    // Cache layout values to avoid recomputation
    private double cachedWidth, cachedHeight;
    private double mapPixelSize, offsetX, offsetY, scale;

    public MapCanvas() {
        widthProperty().addListener(e -> {
            recalcLayout();
            draw();
        });
        heightProperty().addListener(e -> {
            recalcLayout();
            draw();
        });
    }

    @Override
    public boolean isResizable() { return true; }

    @Override
    public double prefWidth(double height) { return getWidth(); }

    @Override
    public double prefHeight(double width) { return getHeight(); }

    private void recalcLayout() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;
        mapPixelSize = Math.min(w, h);
        offsetX = (w - mapPixelSize) / 2;
        offsetY = (h - mapPixelSize) / 2;
        scale = mapPixelSize / MAP_SIZE;
        cachedWidth = w;
        cachedHeight = h;
    }

    public void setWorld(WorldState world) {
        this.world = world;
        draw();
    }

    public void setSelectedCitizen(Citizen citizen) {
        this.selectedCitizen = citizen;
        draw();
    }

    public void setSelectedLocation(Location location) {
        this.selectedLocation = location;
        draw();
    }

    public void draw() {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Recalc layout if size changed
        if (w != cachedWidth || h != cachedHeight) {
            recalcLayout();
        }

        GraphicsContext gc = getGraphicsContext2D();

        // Clear entire canvas
        gc.clearRect(0, 0, w, h);

        // Draw map background
        gc.setFill(Color.web("#f0f4e8"));
        gc.fillRect(offsetX, offsetY, mapPixelSize, mapPixelSize);

        // Draw grid lines
        gc.setStroke(Color.web("#d0d8c0"));
        gc.setLineWidth(0.5);
        gc.setLineDashes(); // ensure solid
        for (int i = 0; i <= 10; i++) {
            double xPos = offsetX + (i / 10.0) * mapPixelSize;
            gc.strokeLine(xPos, offsetY, xPos, offsetY + mapPixelSize);
            double yPos = offsetY + (i / 10.0) * mapPixelSize;
            gc.strokeLine(offsetX, yPos, offsetX + mapPixelSize, yPos);
        }

        // Draw border
        gc.setStroke(Color.web("#888888"));
        gc.setLineWidth(1.5);
        gc.strokeRect(offsetX, offsetY, mapPixelSize, mapPixelSize);

        if (world == null) return;

        Font iconFont = Font.font("System", ICON_SIZE);
        Font smallFont = Font.font("System", 10);

        // Draw locations
        gc.setTextAlign(TextAlignment.CENTER);
        List<Location> locations = world.getLocations();
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            double px = offsetX + loc.getCoordinate().getX() * scale;
            double py = offsetY + (MAP_SIZE - loc.getCoordinate().getY()) * scale;

            // Clip to map bounds
            if (px < offsetX - 10 || px > offsetX + mapPixelSize + 10 ||
                py < offsetY - 10 || py > offsetY + mapPixelSize + 10)
                continue;

            boolean isSelected = loc == selectedLocation;

            if (isSelected) {
                gc.setFill(Color.web("#FFD700", 0.5));
                gc.fillOval(px - 16, py - 16, 32, 32);
                gc.setStroke(Color.web("#FF8C00"));
                gc.setLineWidth(2);
                gc.setLineDashes();
                gc.strokeOval(px - 16, py - 16, 32, 32);
            }

            // Location icon
            gc.setFont(iconFont);
            gc.setFill(Color.BLACK);
            gc.fillText(loc.getIcon(), px, py + 5);

            // Location name below icon
            gc.setFont(smallFont);
            gc.setFill(Color.web("#333333"));
            gc.fillText(loc.getName(), px, py + ICON_SIZE + 6);
        }

        // Draw citizens
        List<Citizen> citizens = world.getCitizens();
        for (int i = 0; i < citizens.size(); i++) {
            Citizen c = citizens.get(i);
            Coordinate pos = c.getCurrentPosition();
            if (pos == null) continue;

            double px = offsetX + pos.getX() * scale;
            double py = offsetY + (MAP_SIZE - pos.getY()) * scale;

            // Clip to map bounds
            if (px < offsetX - 10 || px > offsetX + mapPixelSize + 10 ||
                py < offsetY - 10 || py > offsetY + mapPixelSize + 10)
                continue;

            boolean isSelected = c == selectedCitizen;

            // Draw travel line if travelling
            if (c.isTravelling() && c.getDestinationLocation() != null) {
                Location dest = c.getDestinationLocation();
                double destPx = offsetX + dest.getCoordinate().getX() * scale;
                double destPy = offsetY + (MAP_SIZE - dest.getCoordinate().getY()) * scale;
                gc.setStroke(isSelected ? Color.web("#4169E1") : Color.web("#aaaaaa"));
                gc.setLineWidth(1);
                gc.setLineDashes(4, 4);
                gc.strokeLine(px, py, destPx, destPy);
                gc.setLineDashes();
            }

            // Selection highlight
            if (isSelected) {
                gc.setFill(Color.web("#87CEEB", 0.5));
                gc.fillOval(px - 12, py - 16, 24, 24);
                gc.setStroke(Color.web("#4169E1"));
                gc.setLineWidth(2);
                gc.setLineDashes();
                gc.strokeOval(px - 12, py - 16, 24, 24);
            }

            // Citizen icon
            gc.setFont(iconFont);
            gc.setFill(Color.BLACK);
            gc.fillText(c.getIcon(), px, py);
        }
    }
}
