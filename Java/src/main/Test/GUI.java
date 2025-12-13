import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class GUI {

    private final SimulationController simController;
    private final MapPanel mapPanel;

    private double zoomFactor = 1.0;

    private Label stepLabel;
    private Label vehicleLabel;
    private Label specialVehicleLabel;
    private Label tlsStateLabel;

    public GUI(MapPanel mapPanel, SimulationController simController) {
        this.mapPanel = mapPanel;
        this.simController = simController;
    }

    public void start(Stage stage) {

        // --- Dashboard ---
        stepLabel = new Label("Step: 0");
        vehicleLabel = new Label("Vehicles: 0");
        specialVehicleLabel = new Label("Special Vehicles: 0");
        tlsStateLabel = new Label("TLS State: UNKNOWN");
        VBox dashboard = new VBox(5, stepLabel, vehicleLabel, specialVehicleLabel, tlsStateLabel);
        dashboard.setStyle("-fx-padding: 10; -fx-border-color: red;");

        // --- Map Scroll / Zoom ---
        ScrollPane mapScroll = new ScrollPane(mapPanel);
        mapScroll.setPannable(true);
        mapPanel.setOnScroll((ScrollEvent e) -> {
            zoomFactor = e.getDeltaY() > 0 ? zoomFactor * 1.1 : zoomFactor / 1.1;
            mapPanel.setScaleX(zoomFactor);
            mapPanel.setScaleY(zoomFactor);
        });

        // --- Control Panel ---
        Button injectSpecialBtn = new Button("Inject SPECIAL");
        injectSpecialBtn.setOnAction(e -> Platform.runLater(simController::injectSpecialVehicle));

        Button injectWaveBtn = new Button("Inject WAVE");
        injectWaveBtn.setOnAction(e -> Platform.runLater(() -> simController.injectVehicleWave(5)));

        Button tlsToggleBtn = new Button("TLS Toggle");
        tlsToggleBtn.setOnAction(e -> Platform.runLater(simController::controlTrafficLights));

        HBox controls = new HBox(10, injectSpecialBtn, injectWaveBtn, tlsToggleBtn);
        controls.setStyle("-fx-padding: 10; -fx-border-color: gray;");

        // --- Layout ---
        BorderPane root = new BorderPane();
        root.setCenter(mapScroll);
        root.setRight(dashboard);
        root.setBottom(controls);

        Scene scene = new Scene(root, 1200, 800);
        stage.setScene(scene);
        stage.setTitle("SUMO Interactive Simulation");
        stage.show();

        // --- Update Dashboard Periodically ---
        new Thread(() -> {
            while (true) {
                Platform.runLater(this::updateDashboard);
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }).start();
    }

    private void updateDashboard() {
        stepLabel.setText("Step: " + simController.getStepCounter());
        vehicleLabel.setText("Vehicles: " + simController.getVehicleCount());
        specialVehicleLabel.setText("Special Vehicles: " + simController.getSpecialVehicleCount());
        tlsStateLabel.setText("TLS State: " + simController.getTlsState());
    }
}
