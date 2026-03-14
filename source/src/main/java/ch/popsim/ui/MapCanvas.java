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
    private static final double ICON_SIZE = 18;
    private static final double MIN_SPACING = 250.0; // 250m minimum spacing for overlap check

    private WorldState world;
    private Citizen selectedCitizen;
    private Location selectedLocation;

    public MapCanvas() {
        widthProperty().addListener(e -> draw());
        heightProperty().addListener(e -> draw());
    }

    @Override
    public boolean isResizable() { return true; }

    @Override
    public double prefWidth(double height) { return getWidth(); }

    @Override
    public double prefHeight(double width) { return getHeight(); }

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

        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, w, h);

        // Maintain 1:1 aspect ratio
        double mapPixelSize = Math.min(w, h);
        double offsetX = (w - mapPixelSize) / 2;
        double offsetY = (h - mapPixelSize) / 2;

        // Draw map background
        gc.setFill(Color.web("#f0f4e8"));
        gc.fillRect(offsetX, offsetY, mapPixelSize, mapPixelSize);

        // Draw grid
        gc.setStroke(Color.web("#d0d8c0"));
        gc.setLineWidth(0.5);
        for (int i = 0; i <= 10; i++) {
            double pos = offsetX + (i / 10.0) * mapPixelSize;
            gc.strokeLine(pos, offsetY, pos, offsetY + mapPixelSize);
            pos = offsetY + (i / 10.0) * mapPixelSize;
            gc.strokeLine(offsetX, pos, offsetX + mapPixelSize, pos);
        }

        // Draw border
        gc.setStroke(Color.web("#888888"));
        gc.setLineWidth(1.5);
        gc.strokeRect(offsetX, offsetY, mapPixelSize, mapPixelSize);

        if (world == null) return;

        double scale = mapPixelSize / MAP_SIZE;
        Font iconFont = Font.font("System", ICON_SIZE);
        Font smallFont = Font.font("System", 11);

        // Draw locations
        gc.setTextAlign(TextAlignment.CENTER);
        for (Location loc : world.getLocations()) {
            double px = offsetX + loc.getCoordinate().getX() * scale;
            double py = offsetY + (MAP_SIZE - loc.getCoordinate().getY()) * scale; // Y inverted

            // Clip to map bounds
            if (px < offsetX || px > offsetX + mapPixelSize || py < offsetY || py > offsetY + mapPixelSize)
                continue;

            boolean isSelected = loc == selectedLocation;

            if (isSelected) {
                gc.setFill(Color.web("#FFD700", 0.5));
                gc.fillOval(px - 14, py - 14, 28, 28);
                gc.setStroke(Color.web("#FF8C00"));
                gc.setLineWidth(2);
                gc.strokeOval(px - 14, py - 14, 28, 28);
            }

            gc.setFont(iconFont);
            gc.setFill(Color.BLACK);
            gc.fillText(loc.getIcon(), px, py + 6);

            // Draw location name below
            gc.setFont(smallFont);
            gc.setFill(Color.web("#333333"));
            gc.fillText(loc.getName(), px, py + ICON_SIZE + 8);
        }

        // Draw citizens
        List<Citizen> citizens = world.getCitizens();
        for (Citizen c : citizens) {
            Coordinate pos = c.getCurrentPosition();
            if (pos == null) continue;

            double px = offsetX + pos.getX() * scale;
            double py = offsetY + (MAP_SIZE - pos.getY()) * scale;

            if (px < offsetX || px > offsetX + mapPixelSize || py < offsetY || py > offsetY + mapPixelSize)
                continue;

            boolean isSelected = c == selectedCitizen;

            if (isSelected) {
                gc.setFill(Color.web("#87CEEB", 0.5));
                gc.fillOval(px - 12, py - 18, 24, 24);
                gc.setStroke(Color.web("#4169E1"));
                gc.setLineWidth(2);
                gc.strokeOval(px - 12, py - 18, 24, 24);
            }

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

            gc.setFont(iconFont);
            gc.setFill(Color.BLACK);
            gc.fillText(c.getIcon(), px, py);
        }
    }
}
