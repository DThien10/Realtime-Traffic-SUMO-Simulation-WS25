import java.util.ArrayList;
import java.util.List;

import org.eclipse.sumo.libtraci.StringVector;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Vehicle;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class MapPanel extends Pane {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final List<TraCIPosition> normalVehicles = new ArrayList<>();
    private final List<TraCIPosition> specialVehicles = new ArrayList<>();
    private final int padding = 20;

    public MapPanel() {
        canvas = new Canvas(800, 600);
        gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);
    }

    /**
     * Update vehicle positions and redraw map.
     * Must be called on JavaFX Application Thread.
     */
    public void updateFromSimulation() {
        normalVehicles.clear();
        specialVehicles.clear();

        StringVector ids = Vehicle.getIDList();
        for (String id : ids) {
            TraCIPosition pos = Vehicle.getPosition(id);
            if (pos == null) continue;

            if (id.startsWith("PHU_SPECIAL_CAR_")) {
                specialVehicles.add(pos);
            } else {
                normalVehicles.add(pos);
            }
        }

        drawMap();
    }

    private void drawMap() {
        double width = canvas.getWidth();
        double height = canvas.getHeight();

        // Clear background
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, 0, width, height);

        if (normalVehicles.isEmpty() && specialVehicles.isEmpty()) return;

        // Compute bounding box
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;

        for (TraCIPosition p : normalVehicles) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }
        for (TraCIPosition p : specialVehicles) {
            minX = Math.min(minX, p.getX());
            maxX = Math.max(maxX, p.getX());
            minY = Math.min(minY, p.getY());
            maxY = Math.max(maxY, p.getY());
        }

        double rangeX = Math.max(1, maxX - minX);
        double rangeY = Math.max(1, maxY - minY);
        double scaleX = (width - 2.0 * padding) / rangeX;
        double scaleY = (height - 2.0 * padding) / rangeY;
        double scale = Math.min(scaleX, scaleY);

        // Draw normal vehicles
        gc.setFill(Color.WHITE);
        int normalSize = 6;
        for (TraCIPosition p : normalVehicles) {
            double x = (p.getX() - minX) * scale + padding;
            double y = height - ((p.getY() - minY) * scale + padding);
            gc.fillOval(x - normalSize / 2.0, y - normalSize / 2.0, normalSize, normalSize);
        }

        // Draw special vehicles
        gc.setFill(Color.YELLOW);
        int specialSize = 10;
        for (TraCIPosition p : specialVehicles) {
            double x = (p.getX() - minX) * scale + padding;
            double y = height - ((p.getY() - minY) * scale + padding);
            gc.fillOval(x - specialSize / 2.0, y - specialSize / 2.0, specialSize, specialSize);
        }
    }
}
