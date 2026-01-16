package GUI;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import SimulationObjects.SimEdge;
import SimulationObjects.SimLane;
import SimulationObjects.SimTrafficlight;
import SimulationObjects.SimVehicle;
import SimulationWrapper.Position;
import SimulationWrapper.RenderSnapshot;

/**
 * MapPanel is a JPanel that visualizes vehicles in the SUMO simulation.
 * It supports zooming, panning, and distinguishes normal and special vehicles.
 */
public class MapPanel extends JPanel {
    //test//
    private Collection<SimVehicle> vehicles = Collections.emptyList();
    private Collection<SimTrafficlight> trafficLights = Collections.emptyList();
    private Collection<SimEdge> edges = Collections.emptyList();

    // test //   
    private double zoom = 1.0;
    private int offsetX = 0;
    private int offsetY = 0;

    private int lastMouseX, lastMouseY;
    private boolean dragging = false;

    //TEST TRƯỚC
    private final Map<String, SimLane> laneIndex = new HashMap<>();
    //TEST TRƯỚC

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

    public void setEdges(Collection<SimEdge> edges){
        this.edges=edges;
        rebuildLaneIndex(); // TEST TRƯỚC
    }

    // TEST TRƯỚC
    private Position laneEndWithOffset(List<Position> shape, double backOffsetMeters) {
        if (shape == null || shape.size() < 2) return null;

        Position last = shape.get(shape.size() - 1);
        Position prev = shape.get(shape.size() - 2);

        double dx = last.getX() - prev.getX();
        double dy = last.getY() - prev.getY();
        double len = Math.sqrt(dx*dx + dy*dy);

        if (len < 1e-6) return last;

        double ux = dx / len;
        double uy = dy / len;

        return new Position(
            last.getX() - backOffsetMeters * ux,
            last.getY() - backOffsetMeters * uy
        );
    }
    // TEST TRƯỚC

    public void updateFromSimulation(RenderSnapshot snapshot) {

        vehicles = (snapshot.vehicles() != null) ? snapshot.vehicles() : Collections.emptyList();
        trafficLights = (snapshot.trafficLights() != null) ? snapshot.trafficLights() : Collections.emptyList();
        repaint();
    }

    /**
     * Updates vehicle positions from the SUMO simulation.
     * Distinguishes between normal vehicles and special vehicles based on ID prefix.
     */


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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // Background
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Translate and scale: apply offset, zoom, and invert Y-axis
        g2.translate(offsetX, offsetY + getHeight()); // translate down
        g2.scale(zoom, -zoom); // invert Y-axis to match SUMO coordinates

        g2.setStroke(new BasicStroke(5));
        paintRoadSumoStyle(g2);

        // Reset stroke cho các thứ nhỏ phía sau
        g2.setStroke(new BasicStroke((float)(1.0 / zoom)));


        // Draw vehicles
        double s = 1.0 / zoom;

        for (SimVehicle v : vehicles) {
            Position p = v.getPosition();
            if (p == null) continue;

            if (v.isSpecial()) {
                int r = (int)Math.max(1, Math.round(3 * s)); // ~6px
                g2.setColor(Color.YELLOW);
                g2.fillOval((int)p.getX() - r, (int)p.getY() - r, 2*r, 2*r);
            } else {
                int r = (int)Math.max(1, Math.round(2 * s)); // ~4px
                g2.setColor(v.getColor());
                g2.fillOval((int)p.getX() - r, (int)p.getY() - r, 2*r, 2*r);
            }
        }

//TODO render trafficlights on specific edges/lanes

    // TEST TRAFFIC LIGHTS RENDERING
        for (SimTrafficlight t : trafficLights) {
            List<String> controlled = t.getControlledLanes();
            if (controlled == null || controlled.isEmpty()) continue;

            Map<String, Character> laneSignals = t.getLaneSignals();
            Set<String> unique = new HashSet<>(controlled);

            for (String laneId : unique) {
                SimLane lane = laneIndex.get(laneId);
                if (lane == null) continue;

                Position end = laneEndWithOffset(lane.getShape(), 3.0);
                if (end == null) continue;

                char sig = laneSignals.getOrDefault(laneId, 'u');
                Color c = sigToColor(sig);

                double[] dir = laneDirAtEnd(lane.getShape());
                if (dir != null) {
                    drawTrafficLightLine(g2, end, dir[0], dir[1], c);

                }
            }
        }
}
    // TEST TRƯỚC
    private double[] laneDirAtEnd(List<Position> shape) {
        if (shape == null || shape.size() < 2) return null;
        Position last = shape.get(shape.size() - 1);
        Position prev = shape.get(shape.size() - 2);

        double dx = last.getX() - prev.getX();
        double dy = last.getY() - prev.getY();
        double len = Math.sqrt(dx*dx + dy*dy);
        if (len < 1e-6) return null;

        return new double[]{ dx/len, dy/len }; // (ux, uy)
    }

    private void drawTrafficLightLine(Graphics2D g2, Position end, double ux, double uy, Color c) {
        double px = -uy, py = ux;
        double s = 1.0 / zoom;

        // ✅ fit hơn
        double halfLen = 5.0 * s;     // ngắn lại
        double back = 2.0 * s;

        double cx = end.getX() - back * ux;
        double cy = end.getY() - back * uy;

        int x1 = (int)(cx - px * halfLen);
        int y1 = (int)(cy - py * halfLen);
        int x2 = (int)(cx + px * halfLen);
        int y2 = (int)(cy + py * halfLen);

        g2.setStroke(new BasicStroke((float)(2.0 / zoom), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 90));
        g2.drawLine(x1, y1, x2, y2);

        g2.setStroke(new BasicStroke((float)(0.6 / zoom), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(c);
        g2.drawLine(x1, y1, x2, y2);
    }
    // TEST TRƯỚC

    // Draw all edges and their lanes
    private void paintRoadSumoStyle(Graphics2D g2) {
        // ====== STYLE GIỐNG SUMO (WORLD UNITS, mét) ======
        final float LANE_W_DEFAULT = 3.2f;    // m (SUMO thường ~3.2)
        final float OUTLINE_EXTRA  = 0.35f;   // m viền ngoài
        final float MARK_W         = 0.18f;   // m độ dày vạch kẻ
        final float DASH_LEN       = 2.0f;    // m
        final float DASH_GAP       = 2.0f;    // m

        // Road colors (giống screenshot của bạn)
        final Color MAJOR_ROAD = Color.BLACK;
        final Color MINOR_ROAD = new Color(160, 160, 160);
        final Color OUTLINE    = new Color(235, 205, 55, 220);
        final Color WHITE_DASH = new Color(235, 235, 235, 160);
        final Color YELLOW     = new Color(235, 205, 55, 220);

        // 1) VẼ LANE FILL (bao gồm cả internal ":" để junction đen như SUMO)
        // 1) VẼ ROAD BODY THEO EDGE (không vẽ outline từng lane nữa)
        for (SimEdge edge : edges) {
            List<SimLane> lanes0 = edge.getLanes();
            if (lanes0 == null || lanes0.isEmpty()) continue;

            List<SimLane> lanes = new ArrayList<>(lanes0);
            lanes.sort(Comparator.comparingInt(l -> laneIndexFromId(l.getId())));

            // Nếu là internal ":" (junction), chỉ tô đen, KHÔNG vẽ outline vàng
            boolean internal = lanes.stream().anyMatch(l -> {
                String id = l.getId();
                return id != null && id.startsWith(":");
            });

            int laneCount = lanes.size();

            // lấy 2 biên trái/phải để tạo centerline của cả cụm lane
            List<Position> left  = lanes.get(0).getShape();
            List<Position> right = lanes.get(laneCount - 1).getShape();
            if (left == null || right == null || left.size() < 2 || right.size() < 2) continue;

            Path2D centerPath = (laneCount == 1) ? toPath(left) : averagePath(left, right);

            // road width theo mét (world units)
            float roadW = laneCount * LANE_W_DEFAULT;

            // chọn màu fill như bạn đang làm
            boolean major = laneCount >= 3;
            Color fill = major ? MAJOR_ROAD : MINOR_ROAD;

            if (internal) {
                g2.setStroke(new BasicStroke(roadW, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
                g2.setColor(Color.BLACK);
                g2.draw(centerPath);
                continue;
            }

            // ✅ OUTLINE = VÀNG (1 lần cho cả edge)
            g2.setStroke(new BasicStroke(roadW + OUTLINE_EXTRA, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2.setColor(OUTLINE); // (bạn đã set OUTLINE = vàng)
            g2.draw(centerPath);

            // fill road
            g2.setStroke(new BasicStroke(roadW, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            g2.setColor(fill);
            g2.draw(centerPath);
        }


        // 2) VẠCH TRẮNG ĐỨT + 1 VẠCH VÀNG GIỮA (chỉ cho lane KHÔNG phải internal ":")
        BasicStroke dashStroke = new BasicStroke(
                MARK_W,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0,
                new float[]{DASH_LEN, DASH_GAP},
                0
        );

        BasicStroke centerDash = new BasicStroke(
            0.28f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_BEVEL,
            0,
            new float[]{2.0f, 2.0f},
            0
        );

        for (SimEdge edge : edges) {
            List<SimLane> lanes0 = edge.getLanes();
            if (lanes0 == null || lanes0.size() < 2) continue;

            // bỏ internal lanes cho phần markings để khỏi “mì spaghetti”
            List<SimLane> lanes = new ArrayList<>();
            for (SimLane ln : lanes0) {
                String id = ln.getId();
                if (id != null && id.startsWith(":")) continue;
                lanes.add(ln);
            }
            if (lanes.size() < 2) continue;

            lanes.sort(Comparator.comparingInt(l -> laneIndexFromId(l.getId())));

            int n = lanes.size();
            int mid = n / 2; // center line nằm giữa (mid-1) và mid nếu n>=2

            // (a) Vạch trắng đứt giữa các lane, trừ cái “center” để nhường cho vàng
            g2.setStroke(dashStroke);
            g2.setColor(WHITE_DASH);

            for (int i = 0; i < n - 1; i++) {
                if (i == mid - 1) continue; // chừa chỗ vạch vàng

                List<Position> a = lanes.get(i).getShape();
                List<Position> b = lanes.get(i + 1).getShape();
                if (a == null || b == null || a.size() < 2 || b.size() < 2) continue;

                Path2D sep = averagePath(a, b);
                g2.draw(sep);
            }

            // (b) Vạch vàng giữa (1 đường duy nhất mỗi edge)
            if (mid - 1 >= 0 && mid < n) {
                List<Position> a = lanes.get(mid - 1).getShape();
                List<Position> b = lanes.get(mid).getShape();
                if (a != null && b != null && a.size() >= 2 && b.size() >= 2) {
                    Path2D center = averagePath(a, b);
                    g2.setStroke(centerDash);
                    g2.setColor(new Color(235, 235, 235, 200));
                    g2.draw(center);
                }
            }
        }
    }


    private int laneIndexFromId(String id) {
        if (id == null) return 0;
        int k = id.lastIndexOf('_');
        if (k < 0 || k == id.length() - 1) return 0;
        try {
            return Integer.parseInt(id.substring(k + 1));
        } catch (Exception e) {
            return 0;
        }
    }

    private Path2D toPath(List<Position> pts) {
        Path2D p = new Path2D.Double();
        p.moveTo(pts.get(0).getX(), pts.get(0).getY());
        for (int i = 1; i < pts.size(); i++) p.lineTo(pts.get(i).getX(), pts.get(i).getY());
        return p;
    }

    private Path2D averagePath(List<Position> a, List<Position> b) {
        int n = Math.min(a.size(), b.size());
        Path2D p = new Path2D.Double();
        p.moveTo((a.get(0).getX() + b.get(0).getX()) / 2.0, (a.get(0).getY() + b.get(0).getY()) / 2.0);
        for (int i = 1; i < n; i++) {
            p.lineTo((a.get(i).getX() + b.get(i).getX()) / 2.0, (a.get(i).getY() + b.get(i).getY()) / 2.0);
        }
        return p;
    }




    // TEST TRƯỚC
    private void rebuildLaneIndex() {
    laneIndex.clear();
    for (SimEdge edge : edges) {
        for (SimLane lane : edge.getLanes()) {
            // TODO: đổi method lấy laneId cho đúng class SimLane của bạn
            // ví dụ: lane.getId() hoặc lane.getLaneId()
            laneIndex.put(lane.getId(), lane);
        }
    }
    // TEST TRƯỚC
    }
    private Color sigToColor(char sig) {
        return switch (sig) {
            case 'r', 'R' -> Color.RED;
            case 'y', 'Y' -> Color.ORANGE;
            case 'g', 'G' -> Color.GREEN;
            default -> Color.GRAY;
        };
    }

    private List<Position> trimPolyline(List<Position> pts, double trimStart, double trimEnd) {
        if (pts == null || pts.size() < 2) return pts;

        List<Position> a = new java.util.ArrayList<>(pts);
        a = trimFromStart(a, trimStart);
        a = trimFromEnd(a, trimEnd);
        return a;
    }

    private List<Position> trimFromStart(List<Position> pts, double d) {
        while (d > 0 && pts.size() >= 2) {
            Position p0 = pts.get(0);
            Position p1 = pts.get(1);
            double dx = p1.getX() - p0.getX();
            double dy = p1.getY() - p0.getY();
            double len = Math.sqrt(dx*dx + dy*dy);
            if (len < 1e-9) {
                pts.remove(0);
                continue;
            }
            if (len <= d) {
                pts.remove(0);
                d -= len;
            } else {
                double t = d / len;
                Position np = new Position(p0.getX() + t*dx, p0.getY() + t*dy);
                pts.set(0, np);
                break;
            }
        }
        return pts;
    }

    private List<Position> trimFromEnd(List<Position> pts, double d) {
        while (d > 0 && pts.size() >= 2) {
            int n = pts.size();
            Position p0 = pts.get(n - 2);
            Position p1 = pts.get(n - 1);
            double dx = p1.getX() - p0.getX();
            double dy = p1.getY() - p0.getY();
            double len = Math.sqrt(dx*dx + dy*dy);
            if (len < 1e-9) {
                pts.remove(n - 1);
                continue;
            }
            if (len <= d) {
                pts.remove(n - 1);
                d -= len;
            } else {
                double t = (len - d) / len; // giữ lại phần đầu đoạn cuối
                Position np = new Position(p0.getX() + t*dx, p0.getY() + t*dy);
                pts.set(n - 1, np);
                break;
            }
        }
        return pts;
    }


}
