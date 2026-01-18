import javax.swing.SwingUtilities;

import GUI.CustomGUI;
import SimulationWrapper.SimRunner;

import java.io.IOException;


//basic connection showcase of sumo using libtraci
public class Main {


    public static void main(String[] args) throws InterruptedException {


        //sets path to sumo files according to user directory
        String base = System.getProperty("user.dir");

        String configPath;
        String netPath;
        try {
            configPath = FileFinder.findMyFile("altstadt.sumocfg").toString();
            netPath = FileFinder.findMyFile("altstadt.net.xml").toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        // Controller for Simulation and Map
        SimRunner controller = new SimRunner(configPath, netPath);

        // Start SUMO simulation
        controller.start();
        // Launch customGUI with controls
        SwingUtilities.invokeLater(() -> new CustomGUI(controller));
        // Continue running steps in a separate thread
        new Thread(() -> controller.run()).start();




    }
}
