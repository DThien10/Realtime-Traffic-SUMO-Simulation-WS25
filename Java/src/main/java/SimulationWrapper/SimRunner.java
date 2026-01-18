package SimulationWrapper;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import Filters.VehicleFilter;

import GUI.MapPanel;
import SimulationObjects.SimEdge;
import SimulationObjects.SimTrafficlight;
import SimulationObjects.SimVehicle;

public class SimRunner {

    int testcounter =0;
    final static int DEFAULT =0;
    final static int ALL_RED=1;
    final static int ALL_GREEN=2;

    private int TRAFFIC_LIGHT_STATUS = DEFAULT;

    String configPath;
    String netPath;
    SumoWrapper wrapper;
    SimData data ;
    // Panel used to draw the map
    private final MapPanel mapPanel;
    private final VehicleFilter vehicleFilterForRendering = new VehicleFilter();
    DecimalFormat cutoffdecimals = new DecimalFormat("#.00");

    private final static Logger SimRunLogger = Logger.getLogger(SimRunner.class.getName());

    private volatile int stepDelayMs=0;

    private volatile boolean pause = false;
    private boolean running;

    private RenderSnapshot currentSnapshot;




    public SimRunner(String configPath,String netPath){
        mapPanel=new MapPanel();
        this.configPath=configPath;
        this.netPath=netPath;
        wrapper=new SumoWrapper(configPath);
        data=new SimData(wrapper);
    }
    //TODO add gui initialization to start() instead of main method
    public void start() throws InterruptedException {
        wrapper.start();

        data.initiate(netPath);
        mapPanel.setEdges(data.getEdgesSet());

    }
    //pauses the simulation run
    public void pause(){
        pause=true;
    }
    //unpauses the simulation run
    public void unpause(){
        pause=false;
    }

    public void run() {
        running=true;

        while (running) {
            while(pause){
                try{
                    Thread.sleep(100);
                }catch (Exception e){
                    throw new RuntimeException();
                }
            }

            wrapper.step();

            data.update();
            updateMap();

/*
            //Edge average test
            if(testcounter%100==0) {
                for (SimEdge e : data.getEdgesSet()) {
                    System.out.println(e.getId() + ": Average speed: " + e.getAverageSpeed());
                    System.out.println(e.getId() + ": Average cars: " + e.getAverageCars());

                }
            }
            testcounter++;
                            */
            try {
                Thread.sleep(stepDelayMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        Set<String> custom_cars_list = data.get_addedVehicles();
        System.out.println("List of added Cars throughout the simulation: "+custom_cars_list);
        SimRunLogger.info("Added "+data.get_addedVehicles().size()+" Vehicles throughout the whole Simulation");
        

        wrapper.quit();
    }

    public void random_add_Vehicle(int amount_to_add) {
        String newID;
        String random_route;
        List<String> all_routes = data.get_customRoutes();
        Random random_number = new Random((long) wrapper.getTime());
        int random;
        int all_route_length = all_routes.size();
        int addition_counter=0;

        for (int i = 0; i < amount_to_add; i++) {
            newID = "Random_add_" + System.nanoTime(); // unique ID based on time
            random = random_number.nextInt(0, all_route_length);
            random_route = all_routes.get(random);

            if(wrapper.add_Vehicle(newID, random_route)){
                data.UpdateAdded_Vehicles(newID);
              //  data.registerVehicle(newID);
                addition_counter++;
            }
        }
        SimRunLogger.info("Injected "+addition_counter+" Vehicles randomly to the Simulation");
    }


    public boolean addVehicleWithParams(String routeId, double speed, java.awt.Color color) {
        String id = "GUI_" + (int) wrapper.getTime() + "_" + System.nanoTime();

        boolean ok = wrapper.add_Vehicle(id, routeId);
        if (!ok) return false;

        // set speed (optional)
       // if (!Double.isNaN(speed)) wrapper.set_VehicleSpeed(id, speed);

        // set color (optional)
        if (color != null) wrapper.set_VehicleColor(id, color);

        data.UpdateAdded_Vehicles(id);
       // data.registerVehicle(id); // để vehicle xuất hiện ngay trong list/snapshot
        return true;
    }

    public int addVehiclesOnStartEdgeBatch(String startEdgeId, int count, double speed, java.awt.Color color) {
        List<String> routes = getRoutesForStartEdge(startEdgeId);
        if (routes.isEmpty()) return 0;

        int added = 0;
        // đơn giản: cứ dùng route đầu tiên (hoặc random nếu bạn muốn)
        String routeId = routes.get(0);

        for (int i = 0; i < count; i++) {
            if (addVehicleWithParams(routeId, speed, color)) added++;
        }
        return added;
    }

    public int addVehiclesOnRouteBatch(String routeId, int count, double speed, java.awt.Color color) {
        int added = 0;
        for (int i = 0; i < count; i++) {
            if (addVehicleWithParams(routeId, speed, color)) added++;
        }
        return added;
    }


    public MapPanel getMapPanel() {
        return mapPanel;
    }
    private void updateMap() {
        if (mapPanel != null) {
            currentSnapshot=data.getSimulationSnapshot();
            mapPanel.updateFromSimulation(currentSnapshot);
        }
    }
    public void quit(){
        running=false;
    }
    public void setStepDelayMs(int ms) {
        this.stepDelayMs = ms;
    }


    //cycles through all red, all green and default states for the trafficlights
    public void forceToggleallTrafficLights() {


        Set<SimTrafficlight> trafficlights = data.getTrafficlightsSet();


        if (TRAFFIC_LIGHT_STATUS == DEFAULT) {

            for (SimTrafficlight t : trafficlights) {
                t.set_red();

            }
            TRAFFIC_LIGHT_STATUS=ALL_RED;

        } else if (TRAFFIC_LIGHT_STATUS == ALL_RED) {
            for (SimTrafficlight t : trafficlights) {
                t.set_green();
            }
            TRAFFIC_LIGHT_STATUS=ALL_GREEN;
        }else if(TRAFFIC_LIGHT_STATUS == ALL_GREEN){
            for (SimTrafficlight t : trafficlights){
                t.set_original();
            }
            TRAFFIC_LIGHT_STATUS=DEFAULT;
        }
    }
    public double getSimulationTime(){
        return wrapper.getTime();
    }
    public int getVehicleCount(){
        return wrapper.get_VehicleCount();
    }
    public int getSpecialVehicleCount(){
        return data.get_addedVehicles().size();
    }
    public String getTlsStateName(){
        return switch (TRAFFIC_LIGHT_STATUS){
            case DEFAULT -> "Default";
            case ALL_RED -> "All Red";
            case ALL_GREEN -> "All Green";
            default -> throw new IllegalStateException("Unexpected value: " + TRAFFIC_LIGHT_STATUS);
        };
    }

    public boolean addVehicleOnEdge(String edgeId, String routeId, double speed, java.awt.Color color) {
        String id = "GUI_" + (int) org.eclipse.sumo.libtraci.Simulation.getTime() + "_" + System.nanoTime();

        boolean ok = wrapper.add_Vehicle(id, routeId);
        if (!ok) return false;

        // Optional: force speed
        if (!Double.isNaN(speed)) wrapper.set_VehicleSpeed(id, speed);

        // Optional: set color (needs wrapper method, see section B)
        // if (color != null) wrapper.set_VehicleColor(id, color);

        data.UpdateAdded_Vehicles(id);
        return true;
    }
    public int addVehiclesOnEdgeBatch(String edgeId, String routeId, int count, double speed, java.awt.Color color) {
        int added = 0;
        for (int i = 0; i < count; i++) {
            if (addVehicleOnEdge(edgeId, routeId, speed, color)) added++;
        }
        return added;
    }
    public void setVehicleSpeed(String id, double speed) { wrapper.set_VehicleSpeed(id, speed); }
    //public void setVehicleColor(String id, Color c) { wrapper.set_VehicleColor(id, c); }
    public void setVehicleRoute(String id, String routeId) { wrapper.setVehicleRouteId(id, routeId); }
    public List<String> getTrafficLightIds() { return wrapper.get_Trafficlightids(); }

    public int getTlsPhase(String tlsId) { return wrapper.get_TrafficlightPhase(tlsId); }
    public double getTlsRemaining(String tlsId) { return wrapper.get_Trafficlight_remaining_phaseduration(tlsId); }
    public String getTlsState(String tlsId) { return wrapper.get_Trafficstate(tlsId); }

    public void setTlsPhase(String tlsId, int phase) { wrapper.setTrafficLightPhase(tlsId, phase); }
    public void setTlsPhaseDuration(String tlsId, double seconds) { wrapper.setTrafficLightPhaseDuration(tlsId, seconds); }

    public Collection<SimVehicle> getVehiclesSnapshot() { return currentSnapshot.vehicles();    }
    public Collection<SimTrafficlight> getTrafficLightsSnapshot() { return currentSnapshot.trafficLights(); }
    public Collection<SimEdge> getEdgesSnapshot() {  return currentSnapshot.edges();    }

    public List<String> getRoutesForStartEdge(String edgeId) {
        Map<String, List<String>> m = data.getRoutesByStartEdge();
        List<String> routes = m.get(edgeId);
        return (routes == null) ? java.util.List.of() : java.util.List.copyOf(routes);
    }

    public java.util.List<String> getAllCustomRoutes() {
        return java.util.List.copyOf(data.get_customRoutes());
    }

    public void setVehicleFilterForRenderingMinimum(double minSpeed){
        vehicleFilterForRendering.setMinSpeed(minSpeed);
        mapPanel.setVehicleFilterForRendering(vehicleFilterForRendering);
    }

    public void setVehicleFilterForRenderingMaximum(double maxSpeed) {
        vehicleFilterForRendering.setMaxSpeed(maxSpeed);
        mapPanel.setVehicleFilterForRendering(vehicleFilterForRendering);
    }
    public void setVehicleFilterForRenderingIsUserGenerated(boolean isUserGenerated) {
        vehicleFilterForRendering.setCheckForUserGenerated(isUserGenerated);
        mapPanel.setVehicleFilterForRendering(vehicleFilterForRendering);
    }

    public void toggleVehicleFilterForRenderingIsUserGenerated(){
        if(vehicleFilterForRendering.getCheckForUserGenerated()){
            setVehicleFilterForRenderingIsUserGenerated(false);
        }else{
            setVehicleFilterForRenderingIsUserGenerated(true);
        }
    }
    public void setVehicleFilterForRenderingColors(Set<Color> colors){
        vehicleFilterForRendering.setColors(colors);
        mapPanel.setVehicleFilterForRendering(vehicleFilterForRendering);
    }
    public void setVehicleFilterForRendering_CheckForColors(boolean checkForColors){
        vehicleFilterForRendering.setCheckForColor(checkForColors);
        mapPanel.setVehicleFilterForRendering(vehicleFilterForRendering);
    }

    //TODO make a clear() function to delete all current cars in the simulation
    //TODO be able to restart simulation
    public void refreshVehiclesNow() {
        synchronized (wrapper) {
            data.update_Vehicles();
        }
        updateMap(); // cái này nên invokeLater trong updateMap như tôi đã nhắc trước đó
    }

    public Set<String> get_addedVehicles() {
        return data.get_addedVehicles();
    }
}