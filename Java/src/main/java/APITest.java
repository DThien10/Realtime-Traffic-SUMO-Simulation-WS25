import org.eclipse.sumo.libtraci.*;
import SimulationWrapper.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Scanner;



//basic connection showcase of sumo using libtraci
public class APITest {

   //function to either connect to sumo with or without gui


    public static void main(String[] args) throws InterruptedException {
        //sets up a decimalformat class instance to use later in the output of car coordinates
        DecimalFormat cutoffdecimals = new DecimalFormat("#.00");

        //sets path to sumo files according to user directory
        String base = System.getProperty("user.dir");

        String configPath = base + "/Java/src/main/resources/SumoConfig/altstadt.sumocfg";
        String routePath  = base + "/Java/src/main/resources/SumoConfig/test.rou.xml";
        String netPath = base + "/Java/src/main/resources/SumoConfig/test.net.xml";
        SumoWrapper testwrapper = new SumoWrapper(configPath);
        testwrapper.start();


        List<String> route_list = testwrapper.get_RouteIDList();
        for (int i = 0; i < 50; i++) {
            testwrapper.step();


            System.out.println(route_list);
            testwrapper.add_Vehicle("test"+i, route_list.get(9));
            List<String> cars_list = testwrapper.getVehicleIDs();


            //prints position for every vehicle in the simulation

            for (String id : cars_list) {
                Position pos = testwrapper.get_VehiclePos(id);
                System.out.println("  " + id + " at (" + cutoffdecimals.format(pos.getX())  + ", " + cutoffdecimals.format(pos.getY()) + ")");

            }}

        testwrapper.quit();
/*
        Simulation.preloadLibraries();
        //loading up instance of sumo with given variables
        Simulation.start(new StringVector(new String[]{
                openguiYN(),
                "-c", configPath,
        }));


        //steps through simulation
        for (int i = 0; i < 1000; i++) {
            Simulation.step();
            int cars_amount = Vehicle.getIDCount();
            System.out.println("Step: " + Simulation.getTime() + " ("+cars_amount+" cars in the Simulation)");
            StringVector cars_list = Vehicle.getIDList();

            //prints position for every vehicle in the simulation

            for (String id : cars_list) {
                TraCIPosition pos = Vehicle.getPosition(id);
                System.out.println("  " + id + " at (" + cutoffdecimals.format(pos.getX())  + ", " + cutoffdecimals.format(pos.getY()) + ")");

            }}
        //summarizes amount and id of traffic lights at end of simulation
        int trafficlights_amount = TrafficLight.getIDCount();
        StringVector trafficlights_list =TrafficLight.getIDList();

            System.out.println("This Simulation contained "+trafficlights_amount+ " Traffic lights with the ids: "+ trafficlights_list);



        Simulation.close(); */

    }
}
