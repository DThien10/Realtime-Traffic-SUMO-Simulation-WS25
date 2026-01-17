package GUI;

import Filters.VehicleFilter;
import SimulationWrapper.SimRunner;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * customGUI class for controlling and visualizing the SUMO simulation.
 * Provides panels for status, map, simulation control, speed, vehicle injection, and traffic light control.
 */
public class CustomGUI extends JFrame {

    private final SimRunner controller;

    // ===== STATUS LABELS =====
    private final JLabel timeLabel = new JLabel("Time: 0.0 s");
    private final JLabel vehicleLabel = new JLabel("Vehicles: 0");
    private final JLabel specialLabel = new JLabel("Special: 0");
    private final JLabel tlsLabel = new JLabel("TLS: N/A");

    // ===== CONTROL FLAGS =====
    private boolean paused = false;
    private int stepDelayMs = 0;
    private double minSpeedFilter=0;
    private double maxSpeedFilter=100;
    private boolean isUserGeneratedFilter;

    // ===== LOG AREA =====
    private final JTextArea logArea = new JTextArea();

    private final DecimalFormat df = new DecimalFormat("#.00");


    private JCheckBox yellowBox;
    private JCheckBox cyanBox;
    private JCheckBox redBox;
    private JCheckBox tealBox;
    private JCheckBox blueBox;


    /**
     * Constructs the customGUI window and initializes all panels.
     *
     * @param controller the SimulationController instance used to control the simulation
     */
    public CustomGUI(SimRunner controller) {
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
        panel.add(createSimulationSpeedControl());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createFilterControl());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createColorFilterPanel());
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
    private JPanel createSimulationSpeedControl() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Simulation speed"));

        JSlider speedSlider = new JSlider(0, 500, stepDelayMs);
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
    private JPanel createFilterControl() {
        JPanel parentPanel = new JPanel();
        parentPanel.setLayout(new BorderLayout());

        parentPanel.setBorder(new TitledBorder("Speed Filter"));

        JPanel column1 = new JPanel();
        column1.setLayout(new BorderLayout());
        Dimension sliderSize = new Dimension(140, 40);


        JSlider filterSliderMinimum = new JSlider(0, 200, 10*(int) minSpeedFilter);
        filterSliderMinimum.setPreferredSize(sliderSize);
        JLabel labelMin = new JLabel("Min speed: " + minSpeedFilter*2 + " km/h");

        filterSliderMinimum.addChangeListener(e -> {
            minSpeedFilter = (double) filterSliderMinimum.getValue() /10;
            controller.setVehicleFilterForRenderingMinimum(minSpeedFilter);
            labelMin.setText("Min speed: " + minSpeedFilter*2 + " km/h");
        });
        column1.add(labelMin,BorderLayout.NORTH);
        column1.add(filterSliderMinimum,BorderLayout.CENTER);

        JPanel column2 = new JPanel();
        column2.setLayout(new BorderLayout());

        JSlider filterSliderMaximum = new JSlider(0, 200, 200);
        filterSliderMaximum.setPreferredSize(sliderSize);

        JLabel labelMax = new JLabel("Max speed: " + maxSpeedFilter*2 + " km/h");

        filterSliderMaximum.addChangeListener(e -> {
            maxSpeedFilter = (double) filterSliderMaximum.getValue() /10;
            controller.setVehicleFilterForRenderingMaximum(maxSpeedFilter);
            labelMax.setText("Max speed: " + maxSpeedFilter*2 + " km/h");
        });

        JCheckBox filterForUserGeneratedVehicles=new JCheckBox("Show only user generated cars");

        filterForUserGeneratedVehicles.addActionListener(e -> {
            controller.toggleVehicleFilterForRenderingIsUserGenerated();
        });

        column2.add(labelMax,BorderLayout.NORTH);
        column2.add(filterSliderMaximum,BorderLayout.CENTER);

        parentPanel.add(column1,BorderLayout.WEST);
        parentPanel.add(column2,BorderLayout.EAST);
        parentPanel.add(filterForUserGeneratedVehicles,BorderLayout.SOUTH);





        return parentPanel;
    }

    private JPanel createColorFilterPanel() {
        JPanel panel = new JPanel(new GridLayout(2,2,5,5));
        panel.setBorder(new TitledBorder("Filter by Vehicle Color"));

        yellowBox = createColorCheckbox(VehicleFilter.YELLOW,"Yellow");
        cyanBox = createColorCheckbox(VehicleFilter.CYAN,"Cyan");
        redBox = createColorCheckbox(VehicleFilter.RED,"Red");
        tealBox= createColorCheckbox(VehicleFilter.TEAL,"Teal");
        blueBox = createColorCheckbox(VehicleFilter.BLUE,"Blue");

        panel.add(yellowBox);
        panel.add(blueBox);
        panel.add(cyanBox);
        panel.add(redBox);
        panel.add(tealBox);

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
            controller.forceToggleallTrafficLights();
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


    private JCheckBox createColorCheckbox(Color color, String name) {
        JCheckBox box = new JCheckBox(name, true);

        box.setOpaque(true);
        box.setBackground(color);
        box.setForeground(getTextColorForBackground(color));
        box.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

        box.addActionListener(e -> updateColorFilterFromCheckboxes());

        return box;
    }

    private Color getTextColorForBackground(Color bg) {
        int brightness = (bg.getRed() + bg.getGreen() + bg.getBlue()) / 3;
        if (brightness<=128)return Color.WHITE;
        else return Color.black;
    }


    public void updateColorFilterFromCheckboxes(){
        Set<Color> checkedColors=new HashSet<>();

        if(yellowBox.isSelected()) checkedColors.add(VehicleFilter.YELLOW);
        if(redBox.isSelected()) checkedColors.add(VehicleFilter.RED);
        if(blueBox.isSelected()) checkedColors.add(VehicleFilter.BLUE);
        if(cyanBox.isSelected()) checkedColors.add(VehicleFilter.CYAN);
        if(tealBox.isSelected()) checkedColors.add(VehicleFilter.TEAL);

        boolean allSelected=yellowBox.isSelected()&& redBox.isSelected() &&blueBox.isSelected()&&
                cyanBox.isSelected()&& tealBox.isSelected();

        controller.setVehicleFilterForRendering_CheckForColors(!allSelected);

        controller.setVehicleFilterForRenderingColors(checkedColors);
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
