import org.eclipse.sumo.libtraci.*;

import java.text.DecimalFormat;
import java.util.Scanner;

//basic connection showcase of sumo using libtraci
public class APITest {

   //function to either connect to sumo with or without gui
    public static String openguiYN(){
        Scanner input =new Scanner(System.in);
        System.out.println("Do you want to open the Simulation with the Sumo GUI?[Y/N]");
        String answer = input.nextLine().trim().toLowerCase();
        input.close();
        boolean open_gui = answer.equals("y");
        String sumo;
        if(open_gui){
            sumo="sumo-gui.exe";
        }else{
            sumo="sumo.exe";
        }
        return sumo;
    }

    public static void main(String[] args) {
        //sets up a decimalformat class instance to use later in the output of car coordinates
        DecimalFormat cutoffdecimals = new DecimalFormat("#.00");
        //sets path to sumo files according to user directory
        String base = System.getProperty("user.dir");

        String configPath = base + "/Java/src/main/resources/SumoConfig/altstadt.sumocfg";
        String routePath  = base + "/Java/src/main/resources/SumoConfig/altstadt.rou.xml";



        Simulation.preloadLibraries();
        //loading up instance of sumo with given variables
        Simulation.start(new StringVector(new String[]{
                openguiYN(),
                "-c", configPath,"-r",routePath
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



        Simulation.close();
    }
}
