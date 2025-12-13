import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.StringVector;
import org.eclipse.sumo.libtraci.TrafficLight;
import org.eclipse.sumo.libtraci.Vehicle;

public class SimulationController {

    private final DecimalFormat cutoffDecimals = new DecimalFormat("#.00");
    private final MapPanel mapPanel;

    private static final String SPECIAL_ROUTE_ID = "";
    private static final String SPECIAL_TYPE_ID  = "DEFAULT_VEHTYPE";

    private boolean specialInjected = false;
    private int stepCounter = 0;
    private int specialIdCounter = 0;
    private final List<String> specialVehicleIds = new ArrayList<>();

    private String controlledTlsId = null;
    private String tlsOriginalState = null;
    private String tlsAllRedState = null;
    private double lastTlsSwitchTime = -1.0;
    private boolean tlsShowingOriginal = true;
    private static final double TLS_SWITCH_INTERVAL = 15.0;

    public SimulationController(MapPanel mapPanel) {
        this.mapPanel = mapPanel;
    }

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

    public void runSteps(int steps) {
        for (int i = 0; i < steps; i++) {
            double simTime = Simulation.getTime();

            if (!specialInjected && simTime >= 10.0) {
                injectSpecialVehicle();
                specialInjected = true;
            }

            if (Math.abs(simTime - 30.0) < 1e-3) {
                injectVehicleWave(5);
            }

            controlTrafficLights();
            stepCounter++;

            Simulation.step();
            updateMap();
        }
    }

    private void updateMap() {
        if (mapPanel != null) mapPanel.updateFromSimulation();
    }

    public void printTrafficLightSummary() {
        int trafficLightsAmount = TrafficLight.getIDCount();
        StringVector trafficLightsList = TrafficLight.getIDList();
        System.out.println("This Simulation contains " +
                trafficLightsAmount + " Traffic lights with the ids: " + trafficLightsList);
    }

    public void close() {
        Simulation.close();
        System.out.println("[END] SUMO simulation closed.");
    }

    // =================== PUBLIC METHODS FOR GUI ===================

    public void injectSpecialVehicle() {
        double simTime = Simulation.getTime();
        String timeStr = cutoffDecimals.format(simTime);
        String safeTimeStr = timeStr.replace(',', '_').replace('.', '_');
        String newVehId = "PHU_SPECIAL_CAR_" + safeTimeStr + "_" + specialIdCounter++;

        try {
            Vehicle.add(
                    newVehId,
                    SPECIAL_ROUTE_ID,
                    SPECIAL_TYPE_ID,
                    "now",
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
            specialVehicleIds.add(newVehId);
            System.out.println("Injected " + newVehId);
        } catch (Exception e) {
            System.err.println("Failed to inject " + newVehId + ": " + e.getMessage());
        }
    }

    public void injectVehicleWave(int count) {
        for (int i = 0; i < count; i++) injectSpecialVehicle();
    }

    public void controlTrafficLights() {
        initTrafficLightControl();
        if (controlledTlsId == null || tlsOriginalState == null || tlsAllRedState == null) return;

        double simTime = Simulation.getTime();
        if (lastTlsSwitchTime < 0) lastTlsSwitchTime = simTime;

        if (simTime - lastTlsSwitchTime >= TLS_SWITCH_INTERVAL) {
            String newState = tlsShowingOriginal ? tlsAllRedState : tlsOriginalState;
            try {
                TrafficLight.setRedYellowGreenState(controlledTlsId, newState);
                tlsShowingOriginal = !tlsShowingOriginal;
                lastTlsSwitchTime = simTime;
                System.out.println("[TLS] Switched " + controlledTlsId + " to " +
                        (tlsShowingOriginal ? "ORIGINAL" : "ALL-RED"));
            } catch (Exception e) {
                System.err.println("Failed to set TLS state: " + e.getMessage());
            }
        }
    }

    private void initTrafficLightControl() {
        if (controlledTlsId != null) return;
        int tlsCount = TrafficLight.getIDCount();
        if (tlsCount == 0) return;

        StringVector tlsIds = TrafficLight.getIDList();
        controlledTlsId = tlsIds.get(0);

        try {
            tlsOriginalState = TrafficLight.getRedYellowGreenState(controlledTlsId);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < tlsOriginalState.length(); i++) sb.append('r');
            tlsAllRedState = sb.toString();
            lastTlsSwitchTime = Simulation.getTime();
            tlsShowingOriginal = true;
        } catch (Exception e) {
            System.err.println("Failed init TLS: " + e.getMessage());
            controlledTlsId = null;
        }
    }

    // =================== DASHBOARD GETTERS ===================

    public int getStepCounter() { return stepCounter; }
    public int getVehicleCount() { return Vehicle.getIDCount(); }
    public int getSpecialVehicleCount() { return specialVehicleIds.size(); }
    public String getTlsState() { return tlsShowingOriginal ? "ORIGINAL" : "ALL-RED"; }
}
