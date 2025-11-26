import org.eclipse.sumo.libtraci.*;

import java.util.Scanner;

public class APITest {
    public static String openguiYN(){
        Scanner input =new Scanner(System.in);
        System.out.println("Do you want to open the Simulation with the Sumo GUI?[Y/N]");
        String answer = input.nextLine().trim().toLowerCase();
        boolean opengui = answer.equals("y");
        String sumo;
        if(opengui){
            sumo="sumo-gui.exe";
        }else{
            sumo="sumo.exe";
        }
        return sumo;
    }

    public static void main(String[] args) {


        String base = System.getProperty("user.dir");

        String configPath = base + "/SumoConfig/altstadt.sumocfg";
        String routePath  = base + "/SumoConfig/altstadt.rou.xml";



        Simulation.preloadLibraries();
        Simulation.start(new StringVector(new String[]{
                openguiYN(),
                "-c", configPath,"-r",routePath
        }));



        for (int i = 0; i < 100; i++) {
            Simulation.step();

            System.out.println("Step: " + Simulation.getTime());
            StringVector cars = Vehicle.getIDList();

            for (String id : cars) {
                TraCIPosition pos = Vehicle.getPosition(id);
                System.out.println("  " + id + " at (" + pos.getX() + ", " + pos.getY() + ")");

            }}

        StringVector tlights =TrafficLight.getIDList();

            System.out.println("Traffic light id: "+ tlights);



        Simulation.close();
    }
}
