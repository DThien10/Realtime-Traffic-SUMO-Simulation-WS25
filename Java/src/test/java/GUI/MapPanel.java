package GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;

import javax.swing.JPanel;

import org.eclipse.sumo.libtraci.StringVector;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Vehicle;

/**
 * MapPanel is a JPanel that visualizes vehicles in the SUMO simulation.
 * It supports zooming, panning, and distinguishes normal and special vehicles.
 */
public class MapPanel extends JPanel {

    private final java.util.List<TraCIPosition> normalVehicles = new ArrayList<>();
    private final java.util.List<TraCIPosition> specialVehicles = new ArrayList<>();

    private double zoom = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    private int lastMouseX, lastMouseY;
    private boolean dragging = false;

    /**
     * Constructs the MapPanel and sets up mouse listeners for panning and zooming.
     */
    public MapPanel() {
        setPreferredSize(new Dimension(800, 600));

        // Mouse wheel zoom
        addMouseWheelListener(this::zoomMap);

        // Mouse press/release for dragging
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                dragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                dragging = false;
            }
        });

        // Mouse drag to pan
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragging) {
                    int dx = e.getX() - lastMouseX;
                    int dy = e.getY() - lastMouseY;
                    offsetX += dx;
                    offsetY += dy;
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                }
            }
        });
    }

    /**
     * Sets the zoom factor for the map.
     *
     * @param zoom the zoom factor (1.0 = default scale)
     */
    public void setZoom(double zoom) {
        this.zoom = zoom;
        repaint();
    }

    /**
     * Handles mouse wheel events to zoom in/out of the map.
     *
     * @param e the MouseWheelEvent
     */
    private void zoomMap(MouseWheelEvent e) {
        zoom += -e.getPreciseWheelRotation() * 0.1;
        if (zoom < 0.1) zoom = 0.1;
        if (zoom > 5.0) zoom = 5.0;
        repaint();
    }

    /**
     * Updates vehicle positions from the SUMO simulation.
     * Distinguishes between normal vehicles and special vehicles based on ID prefix.
     */
    public void updateFromSimulation() {
        normalVehicles.clear();
        specialVehicles.clear();

        StringVector ids = Vehicle.getIDList();
        for (String id : ids) {
            TraCIPosition pos = Vehicle.getPosition(id);
            if (pos == null) continue;

            // Distinguish special vehicles by ID prefix
            if (id.startsWith("SPECIAL_CAR_")) {
                specialVehicles.add(pos);
            } else {
                normalVehicles.add(pos);
            }
        }
        repaint();
    }

    /**
     * Paints the panel with the current vehicle positions.
     * Normal vehicles are drawn in white, special vehicles in yellow.
     *
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Background
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Translate and scale: apply offset, zoom, and invert Y-axis
        g2.translate(offsetX, offsetY + getHeight()); // translate down
        g2.scale(zoom, -zoom); // invert Y-axis to match SUMO coordinates

        // Draw normal vehicles
        g2.setColor(Color.WHITE);
        for (TraCIPosition p : normalVehicles) {
            g2.fillOval((int)p.getX() - 3, (int)p.getY() - 3, 6, 6);
        }

        // Draw special vehicles
        g2.setColor(Color.YELLOW);
        for (TraCIPosition p : specialVehicles) {
            g2.fillOval((int)p.getX() - 5, (int)p.getY() - 5, 10, 10);
        }
    }
}
