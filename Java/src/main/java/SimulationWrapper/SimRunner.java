package SimulationWrapper;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import Filters.VehicleFilter;
import org.eclipse.sumo.libtraci.Simulation;

import GUI.MapPanel;
import SimulationObjects.SimTrafficlight;

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






    public SimRunner(String configPath,String netPath,MapPanel mapPanel){
        this.mapPanel=mapPanel;
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
    public void pause(){
        pause=true;
    }
    public void unpause(){
        pause=false;
    }

    public void run(int steps) {



        for (int i = 0; i < steps; i++) {
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
            newID = "Random_add" + (int) Simulation.getTime()+"_" + i;
            random = random_number.nextInt(0, all_route_length);
            random_route = all_routes.get(random);

            if(wrapper.add_Vehicle(newID, random_route)){
                data.UpdateAdded_Vehicles(newID);
                addition_counter++;
            }
        }
        SimRunLogger.info("Injected "+addition_counter+" Vehicles randomly to the Simulation");
    }
    public MapPanel getMapPanel() {
        return mapPanel;
    }
    private void updateMap() {
        if (mapPanel != null) {
            mapPanel.updateFromSimulation(data.getSimulationSnapshot());
        }
    }
    public void quit(){
        wrapper.quit();
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
    //TODO make a clear() function to delete all current cars in the simulation
    //TODO be able to restart simulation
}
