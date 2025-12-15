package SimulationWrapper;

import GUI.*;
import org.eclipse.sumo.libtraci.Simulation;

import javax.swing.*;
import java.util.List;
import java.text.DecimalFormat;
import java.util.logging.Logger;

public class SimRunner {

    String configPath;
    SumoWrapper wrapper;
    SimData data = new SimData();
    // Panel used to draw the map
    private final MapPanel mapPanel;
    DecimalFormat cutoffdecimals = new DecimalFormat("#.00");

    private final static Logger SimRunLogger = Logger.getLogger(SimRunner.class.getName());

    private volatile int stepDelayMs=100;


    public SimRunner(String configPath,MapPanel mapPanel){
        this.mapPanel=mapPanel;
        this.configPath=configPath;
        wrapper=new SumoWrapper(configPath);
    }

    public void start() throws InterruptedException {
        wrapper.start();

        data.initiate(wrapper);

    }
    public void pause(){
        wrapper.pause();
    }
    public void unpause(){
        wrapper.unpause();
    }

    public void run(int steps) {


        List<String> custom_route_list = data.get_customRoutes();
        for (int i = 0; i < steps; i++) {

            wrapper.step();
            data.update(wrapper);


            System.out.println(custom_route_list);
          /* if( wrapper.add_Vehicle("test"+i, custom_route_list.get(3))){
               data.UpdateAdded_Vehicles("test"+i);
           }*/
            //stress test adds 50 vehicles per step
              random_add_Vehicle(50);

            List<String> cars_list = data.get_allVehicles();


            //prints position for every vehicle in the simulation

            for (String id : cars_list) {
                Position pos = wrapper.get_VehiclePos(id);
                System.out.println("  " + id + " at (" + cutoffdecimals.format(pos.getX())  + ", " + cutoffdecimals.format(pos.getY()) + ")");


            }
            mapPanel.updateFromSimulation(data.getAllVehicle_Positions());
        }

        List<String> custom_cars_list = data.get_addedVehicles();
        System.out.println("List of added Cars throughout the simulation: "+custom_cars_list);
        SimRunLogger.info("Added "+data.get_addedVehicles().size()+" Vehicles throughout the whole Simulation");


        wrapper.quit();
    }

    public void random_add_Vehicle(int amount_to_add) {
        String newID;
        String random_route;
        List<String> all_routes = data.get_customRoutes();
        java.util.Random random_number = new java.util.Random((long) Simulation.getTime());
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
            mapPanel.updateFromSimulation(data.getAllVehicle_Positions());
        }
    }
    public void quit(){
        wrapper.quit();
    }
    public void setStepDelayMs(int ms) {
        this.stepDelayMs = ms;
    }
    public void forceToggleTrafficLight(){
        wrapper.toggleTls();
    }
    public double getSimulationTime(){
        return wrapper.time();
    }
    public int getVehicleCount(){
        return wrapper.get_VehicleCount();
    }
    public int getSpecialVehicleCount(){
        return data.get_addedVehicles().size();
    }
    public String getTlsStateName(){
        return wrapper.get_TrafficlightState();
    }
}
