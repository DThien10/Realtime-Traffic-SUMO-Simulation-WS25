import java.util.Scanner;

import javafx.application.Application;
import javafx.stage.Stage;

public class APITest extends Application {

    public static boolean askOpenGui() {
        try (Scanner input = new Scanner(System.in)) {
            System.out.println("Do you want to open the Simulation with the Sumo GUI? [Y/N]");
            String answer = input.nextLine().trim().toLowerCase();
            return answer.equals("y");
        }
    }

    @Override
    public void start(Stage stage) {

        String configPath = "\"D:\\Downloads\\Realtime-Traffic-SUMO-Simulation-WS25-main\\Realtime-Traffic-SUMO-Simulation-WS25-main\\Java\\src\\main\\resources\\SumoConfig\\altstadt.sumocfg\"";
        String routePath  = "\"D:\\Downloads\\Realtime-Traffic-SUMO-Simulation-WS25-main\\Realtime-Traffic-SUMO-Simulation-WS25-main\\Java\\src\\main\\resources\\SumoConfig\\altstadt.rou.xml\"";

        boolean openGui = askOpenGui();

        // Map và Controller
        MapPanel mapPanel = new MapPanel();
        SimulationController simController = new SimulationController(mapPanel);

        // Khởi tạo GUI riêng
        GUI gui = new GUI(mapPanel, simController);
        gui.start(stage);

        // Chạy simulation trên thread riêng
        new Thread(() -> {
            simController.start(configPath, routePath, openGui);
            simController.runSteps(1000);
            simController.printTrafficLightSummary();
            simController.close();
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
