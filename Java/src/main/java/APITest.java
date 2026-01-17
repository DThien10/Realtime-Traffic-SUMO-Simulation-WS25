import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import GUI.CustomGUI;
import GUI.MapPanel;
import SimulationWrapper.SimRunner;


//basic connection showcase of sumo using libtraci
public class APITest {


    public static void main(String[] args) throws InterruptedException {
   

        //sets path to sumo files according to user directory
        String base = System.getProperty("user.dir");

        String configPath = "C:\\Users\\lenovo\\Documents\\GitHub\\Realtime-Traffic-SUMO-Simulation-WS25\\Java\\src\\main\\resources\\SumoConfig/altstadt.sumocfg";
        String netPath  = "C:\\Users\\lenovo\\Documents\\GitHub\\Realtime-Traffic-SUMO-Simulation-WS25\\Java\\src\\main\\resources\\SumoConfig/altstadt.net.xml";

        MapPanel mapPanel = new MapPanel();
        JFrame frame = new JFrame("SUMO Realtime Map"); // create a frame(window) to hold the map panel
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // application is closed when the frame is closed
        frame.add(mapPanel);

        frame.pack(); // adjust frame size to fit the preferred size of components inside it
        frame.setLocationRelativeTo(null); // center the frame on the screen
        frame.setVisible(true); // make the frame visible

        // Controller for Simulation and Map
        SimRunner controller = new SimRunner(configPath, netPath, mapPanel);

        // Start SUMO simulation
        controller.start();
        // Launch customGUI with controls
        SwingUtilities.invokeLater(() -> new CustomGUI(controller));
        // Continue running steps in a separate thread
        new Thread(() -> controller.run(10000)).start();




    }
}
