import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.SwingUtilities;

import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.StringVector;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.TrafficLight;
import org.eclipse.sumo.libtraci.Vehicle;

public class SimulationController {

    // Decimal formatter for cleaner output
    private final DecimalFormat cutoffDecimals = new DecimalFormat("#.00");

    // Panel used to draw the map
    private final MapPanel mapPanel;

    // Special route & type for SPECIAL vehicles
    // Empty route -> SUMO automatically selects a valid edge
    private static final String SPECIAL_ROUTE_ID = "";
    private static final String SPECIAL_TYPE_ID  = "DEFAULT_VEHTYPE";

    // Ensure SPECIAL vehicles are injected only once at the chosen time
    private boolean specialInjected = false;

    // Simulation step counter
    private int stepCounter = 0;

    // Counter to generate unique IDs for SPECIAL vehicles
    private int specialIdCounter = 0;

    // Store IDs of injected SPECIAL vehicles for logging / debugging
    private final List<String> specialVehicleIds = new ArrayList<>();

    // ==== TRAFFIC LIGHT CONTROL STATE ====

    // ID of the traffic light being controlled (the first one found)
    private String controlledTlsId = null;

    // Original state and all-red state of the traffic light
    private String tlsOriginalState = null;
    private String tlsAllRedState = null;

    // Last time the traffic light state was switched (simulation seconds)
    private double lastTlsSwitchTime = -1.0;

    // Whether the traffic light is currently showing the original state
    private boolean tlsShowingOriginal = true;

    // Interval between state switches (simulation seconds)
    private static final double TLS_SWITCH_INTERVAL = 15.0;

    private volatile boolean paused = false;  // simulation paused flag
    private volatile int stepDelayMs = 100;   // delay between steps in milliseconds

    public SimulationController(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }

    /**
     * Start SUMO with the given config and route files
     *
     * @param configPath path to *.sumocfg file
     * @param routePath  path to *.rou.xml file (can be empty if included in config)
     * @param openGui    true -> sumo-gui.exe, false -> sumo.exe
     */
    public void start(String configPath, String routePath, boolean openGui) {
        String sumo = openGui ? "sumo-gui.exe" : "sumo.exe";

        Simulation.preloadLibraries();

        Simulation.start(new StringVector(new String[]{
                sumo,
                "-c", configPath,
                "-r", routePath
        }));

        System.out.println("[START] SUMO started, time=" + Simulation.getTime());
        printTrafficLightSummary();
    }

    /**
     * Run the simulation for a given number of steps.
     *  - t ~ 10s: inject one SPECIAL vehicle
     *  - t = 30s: inject a wave of 5 SPECIAL vehicles (stress test)
     *  - each step: control traffic lights, update map, print logs
     */
    public void runSteps(int steps) {
        for (int i = 0; i < steps; i++) {
            // Pause if paused = true (from GUI)
            while (paused) {
                try {
                    Thread.sleep(100); // check every 100ms
                } catch (InterruptedException ignored) {}
            }
            double simTime = Simulation.getTime();
            // Inject exactly one SPECIAL vehicle at time >= 10s
            if (!specialInjected && simTime >= 10.0) {
                System.out.println("=== TIME " + cutoffDecimals.format(simTime)
                        + "s -> injecting ONE SPECIAL vehicle ===");
                injectSpecialVehicle();
                specialInjected = true;
            }

            // Example: inject a wave of 5 SPECIAL vehicles at t = 30s
            if (Math.abs(simTime - 30.0) < 1e-3) {
                System.out.println("=== TIME " + cutoffDecimals.format(simTime)
                        + "s -> injecting WAVE of 5 SPECIAL vehicles ===");
                injectVehicleWave(5);
            }

            // Control traffic lights
            controlTrafficLights();
            stepCounter++;

            // Advance simulation
            Simulation.step();

            // Update GUI
            SwingUtilities.invokeLater(this::updateMap);

            // Print general vehicle log
            printVehiclePositions();

            // Print SPECIAL vehicle log
            logSpecialVehiclePositions();
            // Delay between steps
            try {
                Thread.sleep(stepDelayMs);
            } catch (InterruptedException ignored) {}
        }
    }

    // ====== HELPER METHODS ======

    // Getter for MapPanel so GUI can access and render
    public MapPanel getMapPanel() {
        return mapPanel;
    }

    // Update MapPanel with latest simulation data
    private void updateMap() {
        if (mapPanel != null) {
            mapPanel.updateFromSimulation();
        }
    }

    // Print positions of ALL vehicles (normal + SPECIAL)
    private void printVehiclePositions() {
        StringVector ids = Vehicle.getIDList();
        int count = ids.size();
        System.out.println("[STEP " + stepCounter + "] Vehicle count = " + count);

        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            TraCIPosition pos = Vehicle.getPosition(id);
            if (pos != null) {
                System.out.println("   " + id + " at (" + cutoffDecimals.format(pos.getX())
                        + ", " + cutoffDecimals.format(pos.getY()) + ")");
            }
        }
    }

    // === VEHICLE INJECTION (ENHANCED) ===

    /**
     * Inject a single SPECIAL vehicle:
     *  - ID: SPECIAL_CAR_<time>_<counter>
     *  - route: empty -> SUMO selects automatically
     *  - type: SPECIAL_TYPE_ID
     *  - store ID for later tracking
     */
    public void injectSpecialVehicle() {
        double simTime = Simulation.getTime();
        String timeStr = cutoffDecimals.format(simTime);

        // Replace comma/dot with '_' for safe ID
        String safeTimeStr = timeStr.replace(',', '_').replace('.', '_');

        // Add counter to ensure unique ID even at same simTime
        String newVehId = "SPECIAL_CAR_" + safeTimeStr + "_" + specialIdCounter++;

        System.out.println();
        System.out.println("=== [SPECIAL INJECT] t = " + timeStr + "s ===");
        System.out.println("-> Before add: " + Vehicle.getIDCount() + " vehicles");

        try {
            Vehicle.add(
                    newVehId,
                    SPECIAL_ROUTE_ID,   // empty -> SUMO default
                    SPECIAL_TYPE_ID,    // special vehicle type
                    "now",              // depart immediately
                    "first",
                    "base",
                    "0",
                    "current",
                    "max",
                    "current",
                    "",
                    "",
                    "",
                    0,
                    0
            );

            // Store SPECIAL vehicle ID for position logging
            specialVehicleIds.add(newVehId);

            System.out.println("-> Injected " + newVehId +
                    " (type=" + SPECIAL_TYPE_ID + ", route=<SUMO default>)");
        } catch (Exception e) {
            System.err.println("!! Failed to inject " + newVehId + ": " + e.getMessage());
        }

        System.out.println("-> After add: " + Vehicle.getIDCount() + " vehicles");
    }

    /**
     * Inject a "wave" of multiple SPECIAL vehicles.
     * Used for stress testing / demonstration.
     */
    public void injectVehicleWave(int count) {
        for (int i = 0; i < count; i++) {
            injectSpecialVehicle();
        }
    }

    /**
     * Log positions of SPECIAL vehicles only.
     * Query only vehicles still present in the network
     * to avoid "vehicle is not known" errors.
     */
    private void logSpecialVehiclePositions() {
        if (specialVehicleIds.isEmpty()) return;

        // Get current vehicle IDs in SUMO
        StringVector currentIdsVec = Vehicle.getIDList();
        HashSet<String> currentIds = new HashSet<>();
        for (int i = 0; i < currentIdsVec.size(); i++) {
            currentIds.add(currentIdsVec.get(i));
        }

        System.out.println("   [SPECIAL CARS STATUS]");

        // Use iterator so we can safely remove elements while iterating
        Iterator<String> it = specialVehicleIds.iterator();
        while (it.hasNext()) {
            String id = it.next();

            // If vehicle is no longer in the network, stop tracking it
            if (!currentIds.contains(id)) {
                System.out.println("      " + id + " has left the network.");
                it.remove();
                continue;
            }

            // Only query position for existing vehicles
            try {
                TraCIPosition pos = Vehicle.getPosition(id);
                if (pos != null) {
                    System.out.println("      " + id + " at (" +
                            cutoffDecimals.format(pos.getX()) + ", " +
                            cutoffDecimals.format(pos.getY()) + ")");
                }
            } catch (Exception ignored) {
                // Safety fallback; normally should not occur
            }
        }
    }

    // === TRAFFIC LIGHT CONTROL ===

    // Public so APITest can call it
    public void printTrafficLightSummary() {
        int trafficLightsAmount = TrafficLight.getIDCount();
        StringVector trafficLightsList = TrafficLight.getIDList();
        System.out.println("This Simulation contains " +
                trafficLightsAmount + " Traffic lights with the ids: " + trafficLightsList);
    }

    /**
     * Initialize traffic light control:
     *  - select the first traffic light
     *  - read its original state (e.g., "GrGr")
     *  - build an all-red state ("rrrr")
     */
    private void initTrafficLightControl() {
        if (controlledTlsId != null) {
            return; // already initialized
        }

        int tlsCount = TrafficLight.getIDCount();
        if (tlsCount == 0) {
            System.out.println("[TLS] No traffic lights found in this simulation.");
            return;
        }

        StringVector tlsIds = TrafficLight.getIDList();
        controlledTlsId = tlsIds.get(0); // pick the first one for simplicity

        try {
            tlsOriginalState = TrafficLight.getRedYellowGreenState(controlledTlsId);

            // Build all-red state with same length
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tlsOriginalState.length(); i++) {
                sb.append('r');
            }
            tlsAllRedState = sb.toString();

            lastTlsSwitchTime = Simulation.getTime();
            tlsShowingOriginal = true;

            System.out.println("[TLS] Using traffic light " + controlledTlsId + " for control.");
            System.out.println("     originalState=" + tlsOriginalState +
                    ", allRedState=" + tlsAllRedState);
        } catch (Exception e) {
            System.err.println("[TLS] Failed to initialize traffic light control: " + e.getMessage());
            controlledTlsId = null;
        }
    }

    /**
     * Simple logic: every TLS_SWITCH_INTERVAL seconds:
     *  - if currently original -> switch to all-red
     *  - if currently all-red -> switch back to original
     * Uses TrafficLight.setRedYellowGreenState(...)
     */
    private void controlTrafficLights() {
        initTrafficLightControl();
        if (controlledTlsId == null || tlsOriginalState == null || tlsAllRedState == null) {
            return; // nothing to control
        }

        double simTime = Simulation.getTime();
        if (lastTlsSwitchTime < 0) {
            lastTlsSwitchTime = simTime;
            return;
        }

        if (simTime - lastTlsSwitchTime >= TLS_SWITCH_INTERVAL) {
            String newState;
            String stateName;
            if (tlsShowingOriginal) {
                newState = tlsAllRedState;
                stateName = "ALL-RED";
            } else {
                newState = tlsOriginalState;
                stateName = "ORIGINAL";
            }

            try {
                TrafficLight.setRedYellowGreenState(controlledTlsId, newState);
                tlsShowingOriginal = !tlsShowingOriginal;
                lastTlsSwitchTime = simTime;

                System.out.println("[TLS] Switch " + controlledTlsId + " to " +
                        stateName + " state at t=" + cutoffDecimals.format(simTime));
            } catch (Exception e) {
                System.err.println("[TLS] Failed to set state for " +
                        controlledTlsId + ": " + e.getMessage());
            }
        }
    }

    // Allow GUI to force toggle traffic light state
    public void forceToggleTrafficLight() {
        initTrafficLightControl();
        if (controlledTlsId == null) return;

        tlsShowingOriginal = !tlsShowingOriginal;
        String newState = tlsShowingOriginal ? tlsOriginalState : tlsAllRedState;
        try {
            TrafficLight.setRedYellowGreenState(controlledTlsId, newState);
            System.out.println("[TLS] Force toggled to " + (tlsShowingOriginal ? "ORIGINAL" : "ALL-RED"));
        } catch (Exception e) {
            System.err.println("Failed to toggle TLS: " + e.getMessage());
        }
    }

    // Allow APITest to close the controller
    public void close() {
        Simulation.close();
        System.out.println("[END] SUMO simulation closed.");
    }

    // Getter for GUI (extended)
    public double getSimulationTime() {
        return Simulation.getTime();
    }   

    public int getVehicleCount() {
        return Vehicle.getIDCount();
    }

    public int getSpecialVehicleCount() {
        return specialVehicleIds.size();
    }

    public String getTlsStateName() {
        if (controlledTlsId == null) return "N/A";
        return tlsShowingOriginal ? "ORIGINAL" : "ALL-RED";
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void setStepDelayMs(int ms) {
        this.stepDelayMs = ms;
    }
}

