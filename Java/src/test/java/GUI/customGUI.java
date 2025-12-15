package GUI;

import SimulationWrapper.SimRunner;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

/**
 * customGUI class for controlling and visualizing the SUMO simulation.
 * Provides panels for status, map, simulation control, speed, vehicle injection, and traffic light control.
 */
public class customGUI extends JFrame {

    private final SimRunner controller;

    // ===== STATUS LABELS =====
    private final JLabel timeLabel = new JLabel("Time: 0.0 s");
    private final JLabel vehicleLabel = new JLabel("Vehicles: 0");
    private final JLabel specialLabel = new JLabel("Special: 0");
    private final JLabel tlsLabel = new JLabel("TLS: N/A");

    // ===== CONTROL FLAGS =====
    private boolean paused = false;
    private int stepDelayMs = 100;

    // ===== LOG AREA =====
    private final JTextArea logArea = new JTextArea();

    private final DecimalFormat df = new DecimalFormat("#.00");

    /**
     * Constructs the customGUI window and initializes all panels.
     *
     * @param controller the SimulationController instance used to control the simulation
     */
    public customGUI(SimRunner controller) {
        super("SUMO Simulation Control Center");
        this.controller = controller;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        add(createStatusPanel(), BorderLayout.NORTH);
        add(createMapPanel(), BorderLayout.CENTER);
        add(createControlPanel(), BorderLayout.EAST);
        add(createLogPanel(), BorderLayout.SOUTH);

        setSize(1200, 800);
        setLocationRelativeTo(null);
        setVisible(true);

        startStatusUpdater();
    }

    /**
     * Creates the status panel displaying simulation time, vehicle counts, and TLS state.
     *
     * @return a JPanel containing the status labels
     */
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        panel.setBorder(BorderFactory.createEtchedBorder());

        panel.add(timeLabel);
        panel.add(vehicleLabel);
        panel.add(specialLabel);
        panel.add(tlsLabel);

        return panel;
    }

    /**
     * Creates the map panel containing the simulation map visualization.
     *
     * @return a JScrollPane containing the MapPanel
     */
    private JScrollPane createMapPanel() {
        return new JScrollPane(controller.getMapPanel());
    }

    /**
     * Creates the control panel with simulation control, speed adjustment,
     * vehicle injection, and traffic light control.
     *
     * @return a JPanel containing all control sub-panels
     */
    private JPanel createControlPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(createSimulationControl());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createSpeedControl());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createInjectControl());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createTLSControl());

        return panel;
    }

    /**
     * Creates the simulation control panel with Pause, Resume, and Stop buttons.
     *
     * @return a JPanel for simulation control
     */
    private JPanel createSimulationControl() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 5, 5));
        panel.setBorder(new TitledBorder("Simulation"));

        JButton pauseBtn = new JButton("Pause");
        JButton resumeBtn = new JButton("Resume");
        JButton stopBtn = new JButton("Quit");

        pauseBtn.addActionListener(e -> {
            controller.pause();
            appendLog("Simulation paused");
        });

        resumeBtn.addActionListener(e -> {
            controller.unpause();
            appendLog("Simulation resumed");
        });

        stopBtn.addActionListener(e -> {
            controller.quit();
            appendLog("Simulation stopped");
        });

        panel.add(pauseBtn);
        panel.add(resumeBtn);
        panel.add(stopBtn);

        return panel;
    }

    /**
     * Creates the speed control panel with a slider to adjust simulation step delay.
     *
     * @return a JPanel for speed adjustment
     */
    private JPanel createSpeedControl() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Speed"));

        JSlider speedSlider = new JSlider(10, 500, stepDelayMs);
        JLabel label = new JLabel("Delay: " + stepDelayMs + " ms");

        speedSlider.addChangeListener(e -> {
            stepDelayMs = speedSlider.getValue();
            controller.setStepDelayMs(stepDelayMs);
            label.setText("Delay: " + stepDelayMs + " ms");
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(speedSlider, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the vehicle injection control panel allowing injection of SPECIAL vehicles.
     *
     * @return a JPanel for vehicle injection
     */
    private JPanel createInjectControl() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.setBorder(new TitledBorder("Vehicle Injection"));

        JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        JButton injectBtn = new JButton("Inject SPECIAL");

        injectBtn.addActionListener(e -> {
            int count = (int) countSpinner.getValue();
            controller.random_add_Vehicle(count);
            appendLog("Injected " + count + " SPECIAL vehicles");
        });

        panel.add(new JLabel("Count:"));
        panel.add(countSpinner);
        panel.add(new JLabel());
        panel.add(injectBtn);

        return panel;
    }

    /**
     * Creates the traffic light control panel with a toggle button.
     *
     * @return a JPanel for traffic light control
     */
    private JPanel createTLSControl() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        panel.setBorder(new TitledBorder("Traffic Light"));

        JButton toggleBtn = new JButton("Toggle TLS");

        toggleBtn.addActionListener(e -> {
            controller.forceToggleTrafficLight();
            appendLog("Traffic light toggled");
        });

        panel.add(toggleBtn);
        return panel;
    }

    /**
     * Creates the simulation log panel.
     *
     * @return a JScrollPane containing the simulation log JTextArea
     */
    private JScrollPane createLogPanel() {
        logArea.setEditable(false);
        logArea.setRows(6);
        JScrollPane pane = new JScrollPane(logArea);
        pane.setBorder(new TitledBorder("Simulation Log"));
        return pane;
    }

    /**
     * Appends a message to the simulation log.
     *
     * @param msg the message to append
     */
    private void appendLog(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    /**
     * Starts a timer to periodically update the status panel with simulation data.
     */
    private void startStatusUpdater() {
        new Timer(300, e -> {
            double time = controller.getSimulationTime();
            int vehicles = controller.getVehicleCount();
            int special = controller.getSpecialVehicleCount();
            String tlsState = controller.getTlsStateName();

            timeLabel.setText("Time: " + df.format(time) + " s");
            vehicleLabel.setText("Vehicles: " + vehicles);
            specialLabel.setText("Custom added Vehicles: " + special);
            tlsLabel.setText("TLS: " + tlsState);
        }).start();
    }

    /**
     * Returns whether the simulation is currently paused.
     *
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Returns the current simulation step delay in milliseconds.
     *
     * @return the step delay in milliseconds
     */
    public int getStepDelayMs() {
        return stepDelayMs;
    }
}
