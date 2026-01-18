package GUI;

import Filters.VehicleFilter;
import SimulationWrapper.SimRunner;

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import SimulationObjects.SimEdge;
import SimulationWrapper.SimRunner;

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

    // ===== LIST MODELS =====
    private final DefaultListModel<String> vehicleModel = new DefaultListModel<>();
    private final DefaultListModel<String> tlsModel = new DefaultListModel<>();
    private final DefaultListModel<String> edgeModel = new DefaultListModel<>();

    // ===== LIST UI =====
    private final JList<String> vehicleList = new JList<>(vehicleModel);
    private final JList<String> tlsList = new JList<>(tlsModel);
    private final JList<String> edgeList = new JList<>(edgeModel);

    // ===== INJECTION UI =====
    private javax.swing.JComboBox<String> injectEdgeCombo;
    private javax.swing.JComboBox<String> injectRouteCombo;
    private javax.swing.JSpinner injectCountSpinner;
    private javax.swing.JSpinner injectSpeedSpinner;
    private javax.swing.JComboBox<String> injectColorCombo;

    // MAPPING DISPLAY STRINGS TO IDS
    private final Map<String, String> edgeDisplayToId = new HashMap<>();
    private final Map<String, String> vehicleDisplayToId = new HashMap<>();
    private final Map<String, String> tlsDisplayToId = new HashMap<>();

    // ===== TLS DETAILS UI =====
    private String selectedTlsId = null;

    private final JLabel tlsIdValue = new JLabel("-");
    private final JLabel tlsStateValue = new JLabel("-");
    private final JLabel tlsPhaseValue = new JLabel("-");
    private final JLabel tlsRemainingValue = new JLabel("-");

    private JSpinner tlsPhaseSpinner;
    private JSpinner tlsDurationSpinner;


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
        add(createLogPanel(), BorderLayout.WEST);
        add(createListsPane(), BorderLayout.SOUTH);


        setSize(1400, 800);
        setLocationRelativeTo(null);
        setVisible(true);

        addVehicleListListener();
        addTrafficLightListListener();
        addEdgeListListener();

        new Timer(700, e -> {
            updateVehicleList();
            updateTlsList();
            updateEdgeList();
        }).start();
        new Timer(300, e -> refreshSelectedTlsDetails()).start();

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

        panel.setPreferredSize(new Dimension(320, 0));
        panel.setMaximumSize(new Dimension(350, Integer.MAX_VALUE));

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
            Collection<SimEdge> edges = controller.getEdgesSnapshot();
            for(SimEdge edge:edges){
                appendLog(edge.getId()+": Average Speed: "+edge.getAverageSpeed()+" Average cars: "+edge.getAverageCars()+"\n");
            }



            appendLog("Simulation stopped");
            Set<String> custom_cars_list = controller.get_addedVehicles();




            appendLog("Amount of cars added throughout the simulation: "+custom_cars_list.size());
            controller.quit();
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
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Vehicle Injection"));

        // Row 1: Edge
        JPanel row1 = new JPanel(new GridLayout(1, 2, 5, 5));
        row1.add(new JLabel("Start Edge:"));
        injectEdgeCombo = new javax.swing.JComboBox<>();
        row1.add(injectEdgeCombo);

        // Row 2: Route
        JPanel row2 = new JPanel(new GridLayout(1, 2, 5, 5));
        row2.add(new JLabel("Route:"));
        injectRouteCombo = new javax.swing.JComboBox<>();
        row2.add(injectRouteCombo);

        // Row 3: Count
        JPanel row3 = new JPanel(new GridLayout(1, 2, 5, 5));
        row3.add(new JLabel("Count:"));
        injectCountSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 500, 1));
        row3.add(injectCountSpinner);

        // Row 4: Speed
        JPanel row4 = new JPanel(new GridLayout(1, 2, 5, 5));
        row4.add(new JLabel("Speed (m/s):"));
        injectSpeedSpinner = new JSpinner(new SpinnerNumberModel(10.0, 0.0, 50.0, 0.5));
        row4.add(injectSpeedSpinner);

        // Row 5: Color
        JPanel row5 = new JPanel(new GridLayout(1, 2, 5, 5));
        row5.add(new JLabel("Color:"));
        injectColorCombo = new javax.swing.JComboBox<>(new String[]{"(keep)", "Red", "Teal", "Blue", "Yellow", "Cyan"});
        row5.add(injectColorCombo);

        // Buttons
        JPanel row6 = new JPanel(new GridLayout(1, 2, 5, 5));
        JButton refreshBtn = new JButton("Refresh Edges/Routes");
        JButton injectBtn = new JButton("Inject");
        refreshBtn.addActionListener(e -> refreshInjectCombos());

        injectBtn.addActionListener(e -> {
            String edgeDisplay = (String) injectEdgeCombo.getSelectedItem();
            String routeId = (String) injectRouteCombo.getSelectedItem();
            if (edgeDisplay == null) { appendLog("No edge selected"); return; }
            if (routeId == null || routeId.isBlank()) { appendLog("No route selected"); return; }

            String edgeId = edgeDisplayToId.get(edgeDisplay);
            int count = (int) injectCountSpinner.getValue();
            double speed = (double) injectSpeedSpinner.getValue();
            java.awt.Color color = parseColor((String) injectColorCombo.getSelectedItem());

            int added = controller.addVehiclesOnRouteBatch(routeId, count, speed, color);
          //  controller.random_add_Vehicle(count);
            appendLog("Injected " + added + " vehicles on route=" + routeId + " (edge=" + edgeId + ")");
        });

        row6.add(refreshBtn);
        row6.add(injectBtn);

        panel.add(row1);
        panel.add(Box.createVerticalStrut(5));
        panel.add(row2);
        panel.add(Box.createVerticalStrut(5));
        panel.add(row3);
        panel.add(Box.createVerticalStrut(5));
        panel.add(row4);
        panel.add(Box.createVerticalStrut(5));
        panel.add(row5);
        panel.add(Box.createVerticalStrut(8));
        panel.add(row6);
        // init once
        refreshInjectCombos();

        // when edge changes => reload route list
        injectEdgeCombo.addActionListener(e -> reloadRoutesForSelectedEdge());

        return panel;
    }

    /**
     * Creates the traffic light control panel with a toggle button.
     *
     * @return a JPanel for traffic light control
     */

    private JPanel createTLSControl() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new TitledBorder("Traffic Light"));

        // Global toggle (gi? l?i nhu b?n c�)
        JButton toggleAllBtn = new JButton("Toggle ALL TLS (Default/AllRed/AllGreen)");
        toggleAllBtn.addActionListener(e -> {
            controller.forceToggleallTrafficLights();
            appendLog("Toggled ALL traffic lights mode");
        });

        // TLS details section
        JPanel info = new JPanel(new GridLayout(4, 2, 5, 5));
        info.setBorder(new TitledBorder("Selected TLS Details"));

        info.add(new JLabel("ID:"));        info.add(tlsIdValue);
        info.add(new JLabel("State:"));     info.add(tlsStateValue);
        info.add(new JLabel("Phase:"));     info.add(tlsPhaseValue);
        info.add(new JLabel("Remaining:")); info.add(tlsRemainingValue);

        // Controls: phase
        JPanel phaseRow = new JPanel(new GridLayout(1, 4, 5, 5));
        phaseRow.setBorder(new TitledBorder("Manual Phase"));

        tlsPhaseSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 50, 1));
        JButton prevBtn = new JButton("Prev");
        JButton nextBtn = new JButton("Next");
        JButton applyPhaseBtn = new JButton("Apply Phase");

        prevBtn.addActionListener(e -> stepSelectedTlsPhase(-1));
        nextBtn.addActionListener(e -> stepSelectedTlsPhase(+1));

        applyPhaseBtn.addActionListener(e -> {
            if (selectedTlsId == null) { appendLog("No TLS selected"); return; }
            int phase = (int) tlsPhaseSpinner.getValue();
            controller.setTlsPhase(selectedTlsId, phase);
            appendLog("Set TLS " + selectedTlsId + " phase=" + phase);
            refreshSelectedTlsDetails();
        });
        phaseRow.add(new JLabel("Phase:"));
        phaseRow.add(tlsPhaseSpinner);
        phaseRow.add(prevBtn);
        phaseRow.add(nextBtn);

        // Controls: duration
        JPanel durRow = new JPanel(new GridLayout(1, 3, 5, 5));
        durRow.setBorder(new TitledBorder("Phase Duration"));

        tlsDurationSpinner = new JSpinner(new SpinnerNumberModel(10.0, 0.0, 300.0, 1.0));
        JButton applyDurBtn = new JButton("Apply Duration");

        applyDurBtn.addActionListener(e -> {
            if (selectedTlsId == null) { appendLog("No TLS selected"); return; }
            double sec = (double) tlsDurationSpinner.getValue();
            controller.setTlsPhaseDuration(selectedTlsId, sec);
            appendLog("Set TLS " + selectedTlsId + " phaseDuration=" + sec + "s");
            refreshSelectedTlsDetails();
        });
        durRow.add(new JLabel("Seconds:"));
        durRow.add(tlsDurationSpinner);
        durRow.add(applyDurBtn);

        // Apply Phase button row (d? d? th?y)
        JPanel applyRow = new JPanel(new GridLayout(1, 1, 5, 5));
        applyRow.add(applyPhaseBtn);

        panel.add(toggleAllBtn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(info);
        panel.add(Box.createVerticalStrut(8));
        panel.add(phaseRow);
        panel.add(applyRow);
        panel.add(Box.createVerticalStrut(8));
        panel.add(durRow);
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






    private JComponent createListsPane() {
        vehicleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tlsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        edgeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Vehicles", new JScrollPane(vehicleList));
        tabs.addTab("Traffic Lights", new JScrollPane(tlsList));
        tabs.addTab("Edges", new JScrollPane(edgeList)); // ho?c "Routes" n?u b?n mu?n route

        tabs.setBorder(new TitledBorder("Lists"));
        return tabs;
    }

    private void updateVehicleList() {
        vehicleModel.clear();
        vehicleDisplayToId.clear();

        for (SimulationObjects.SimVehicle v : controller.getVehiclesSnapshot()) {
            String display = v.toDisplayString();
            vehicleDisplayToId.put(display, v.getId());
            vehicleModel.addElement(display);
        }
    }


    private void updateTlsList() {
        tlsModel.clear();
        tlsDisplayToId.clear();

        for (SimulationObjects.SimTrafficlight t : controller.getTrafficLightsSnapshot()) {
            String display = t.toDisplayString();
            tlsDisplayToId.put(display, t.getId());
            tlsModel.addElement(display);
        }
    }


    private void updateEdgeList() {
        edgeModel.clear();
        edgeDisplayToId.clear();

        for (SimEdge e : controller.getEdgesSnapshot()) {
            String id = e.getId();
            // b? internal edge :J58_0
            if (id.startsWith(":")) continue;

            String display = e.toDisplayString(); // chu?i d? hi?u
            edgeDisplayToId.put(display, id);     // map display ? id th?t
            edgeModel.addElement(display);        // hi?n th? l�n list
        }
    }


    private void addVehicleListListener() {
        vehicleList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            String selected = vehicleList.getSelectedValue();
            if (selected == null) return;

            String vehicleId = vehicleDisplayToId.get(selected);
            if (vehicleId == null) return;

            appendLog("Selected vehicle: " + vehicleId);
        });
    }

    private void addTrafficLightListListener() {
        tlsList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            String selected = tlsList.getSelectedValue();
            if (selected == null) return;

            String tlsId = tlsDisplayToId.get(selected);
            if (tlsId == null) return;

            selectedTlsId = tlsId;
            appendLog("Selected traffic light: " + tlsId);
            refreshSelectedTlsDetails();
        });
    }


    private void addEdgeListListener() {
        edgeList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;

            String selected = edgeList.getSelectedValue();
            if (selected == null) return;

            String edgeId = edgeDisplayToId.get(selected);
            if (edgeId == null) return;
            appendLog("Selected edge: " + edgeId);
        });
    }

    private void refreshInjectCombos() {
        // build edge combo from current edge list mapping
        injectEdgeCombo.removeAllItems();

        // ensure edge list model has latest data
        updateEdgeList();

        for (int i = 0; i < edgeModel.size(); i++) {
            injectEdgeCombo.addItem(edgeModel.get(i));
        }

        reloadRoutesForSelectedEdge();
    }

    private void reloadRoutesForSelectedEdge() {
        injectRouteCombo.removeAllItems();

        String edgeDisplay = (String) injectEdgeCombo.getSelectedItem();
        if (edgeDisplay == null) return;

        String edgeId = edgeDisplayToId.get(edgeDisplay);
        if (edgeId == null) return;

        java.util.List<String> routes = controller.getRoutesForStartEdge(edgeId);

        if (routes.isEmpty()) {
            // fallback: show all routes so user can still pick
            routes = controller.getAllCustomRoutes();
        }
        injectRouteCombo.removeAllItems();
        for (String r : routes) injectRouteCombo.addItem(r);
        if (routes.isEmpty()) injectRouteCombo.addItem("(no routes)");
    }

    private java.awt.Color parseColor(String s) {
        if (s == null) return null;
        return switch (s) {
            case "Red" -> java.awt.Color.RED;
            case "Teal" -> VehicleFilter.TEAL;
            case "Blue" -> java.awt.Color.BLUE;
            case "Yellow" -> java.awt.Color.YELLOW;
            case "Cyan" -> VehicleFilter.CYAN;
            default -> null; // "(keep)" or unknown
        };
    }


    private void refreshSelectedTlsDetails() {
        if (selectedTlsId == null) {
            tlsIdValue.setText("-");
            tlsStateValue.setText("-");
            tlsPhaseValue.setText("-");
            tlsRemainingValue.setText("-");
            return;
        }
        String state = controller.getTlsState(selectedTlsId);
        int phase = controller.getTlsPhase(selectedTlsId);
        double rem = controller.getTlsRemaining(selectedTlsId);

        tlsIdValue.setText(selectedTlsId);
        tlsStateValue.setText(state);
        tlsPhaseValue.setText(String.valueOf(phase));
        tlsRemainingValue.setText(df.format(rem) + " s");

        // sync spinner with current phase (d? user th?y d�ng)
        if (tlsPhaseSpinner != null) tlsPhaseSpinner.setValue(phase);
    }

    private void stepSelectedTlsPhase(int delta) {
        if (selectedTlsId == null) { appendLog("No TLS selected"); return; }
        int current = controller.getTlsPhase(selectedTlsId);
        int next = Math.max(0, current + delta);
        controller.setTlsPhase(selectedTlsId, next);
        appendLog("Set TLS " + selectedTlsId + " phase=" + next);
        refreshSelectedTlsDetails();
    }

}
