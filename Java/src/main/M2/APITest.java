
import java.util.Scanner; // for getting input from user

import javax.swing.JFrame; // for GUI frame
import javax.swing.SwingUtilities;// for running GUI in event-dispatch thread (luồng xử lý sự kiện , tránh treo ứng dụng khi chạy GUI)

// entry point of the application as the main class
public class APITest {
    // Ask user whether to open the SUMO GUI or not by console input
    public static boolean askOpenGui() {
        try (Scanner input = new Scanner(System.in)) { // use the source within try() to check for errors
            System.out.println("Do you want to open the Simulation with the Sumo GUI? [Y/N]");
            String answer = input.nextLine().trim().toLowerCase(); // get user input and normalize it ("y" ; "Y" are the same, similarly for "n" and "N" , trim spaces (no white space) )
            boolean openGui = answer.equals("y");
            return openGui;
        }
    }

    public static void main(String[] args) {
        // File paths for SUMO configuration and route files 
        /*
        String base = System.getProperty("user.dir");
        String configPath = base + "/Java/src/main/resources/SumoConfig/altstadt.sumocfg";
        String routePath  = base + "/Java/src/main/resources/SumoConfig/test.rou.xml";
        */
        String configPath = "\"D:\\Downloads\\Copy-Realtime-Traffic-SUMO-Simulation-WS25-main\\Realtime-Traffic-SUMO-Simulation-WS25-main\\SumoConfig\\altstadt.sumocfg\"";
        String routePath  = "\"D:\\Downloads\\Copy-Realtime-Traffic-SUMO-Simulation-WS25-main\\Realtime-Traffic-SUMO-Simulation-WS25-main\\SumoConfig\\altstadt.rou.xml\"";
        // Ask user whether to open the SUMO GUI
        boolean openGui = askOpenGui();
        //Map Visual
        MapPanel mapPanel = new MapPanel();
        JFrame frame = new JFrame("SUMO Realtime Map"); // create a frame(window) to hold the map panel
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // application is closed when the frame is closed
        frame.add(mapPanel); 
       
        frame.pack(); // adjust frame size to fit the preferred size of components inside it
        frame.setLocationRelativeTo(null); // center the frame on the screen
        frame.setVisible(true); // make the frame visible

        // Controller for Simulation and Map
        SimulationController controller = new SimulationController(mapPanel);
        
        // Start SUMO simulation
        controller.start(configPath, routePath, openGui);
        // Launch GUI with controls
        SwingUtilities.invokeLater(() -> new GUI(controller));
        // Continue running steps in a separate thread
        new Thread(() -> controller.runSteps(1000)).start();
        // Print traffic light summary to console
        controller.printTrafficLightSummary();
       
    }
}









