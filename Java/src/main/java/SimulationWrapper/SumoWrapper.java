package SimulationWrapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import org.eclipse.sumo.libtraci.*;
/**
 * Control center for all communication between the program and sumo
 */
public class SumoWrapper {
    private final static Logger WrapperLogger = Logger.getLogger(SumoWrapper.class.getName());
    private Process sumoProcess;
    private final int port=48042;
    private String TrafficLightState="DEFAULT";
    private final String configPath;



    public SumoWrapper(String configPath) {
        this.configPath = configPath;
    }
    private static String openguiYN(){
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

    public void start () throws InterruptedException {
        Simulation.preloadLibraries();
        System.out.println("TraCI native libs loaded");


        try{
            ProcessBuilder pb = new ProcessBuilder("sumo",
                    "-c", configPath,
                    "--remote-port", String.valueOf(port)
            );
            pb.inheritIO();
            sumoProcess= pb.start();}
        catch (IOException e){
            System.err.println("Failed to start SUMO: " + e.getMessage());
            e.printStackTrace();
        }

        IntStringPair connection;
        for(int i=0; i<10; i++) {
            try {
                connection = Simulation.init(port);
                System.out.println("Connected to : " + connection.getSecond() + " on port: " + port);
                break;
            } catch(Exception e) {
                Thread.sleep(500);
            }
        }
    }



    public void start (String routePath,String netPath) throws InterruptedException {
        Simulation.preloadLibraries();
        System.out.println("TraCI native libs loaded");


        try{
            ProcessBuilder pb = new ProcessBuilder("sumo-gui",
                    "-r", routePath,"-n",netPath,
                    "--remote-port", String.valueOf(port)
            );
            pb.inheritIO();
            sumoProcess= pb.start();}
        catch (IOException e){
            System.err.println("Failed to start SUMO: " + e.getMessage());
            e.printStackTrace();
        }

        IntStringPair connection;
        for(int i=0; i<10; i++) {
            try {
                connection = Simulation.init(port);
                System.out.println("Connected to : " + connection.getSecond() + " on port: " + port);
                break;
            } catch(Exception e) {
                Thread.sleep(500);
            }
        }

    }

    public void quit(){

        if(sumoProcess != null && sumoProcess.isAlive() ){
            sumoProcess.destroy();
        }
        WrapperLogger.info("Sumo process terminated");
    }

    public void step(){

        Simulation.step();

    }

    public double getTime(){
        return Simulation.getTime();
    }

    public List<String> getVehicleIDs (){
        return new ArrayList<>(Vehicle.getIDList());
    }
    public boolean vehicleExists(String CarID){
        return Vehicle.getIDList().contains(CarID);
    }
    public int get_VehicleCount () {
        return Vehicle.getIDCount();
    }

    public Position get_VehiclePos (String id){


        TraCIPosition pos = Vehicle.getPosition(id);
        return new Position(pos.getX(), pos.getY());

    }

    public boolean add_Vehicle(String CarID, String RouteID){
        try{
            Vehicle.add(CarID, RouteID);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to add vehicle: " + CarID+ " on route"+RouteID+" :"+e.getMessage());
            return false;
        }

    }
    public void remove_Vehicle(String CarID){
        if(vehicleExists(CarID)){
            Vehicle.remove(CarID);}
        else WrapperLogger.warning("Vehicle with the ID: "+ CarID+" does not exist");

    }
    //Yet to see if usable or need to implement own color class
    public TraCIColor get_Vehiclecolor(String id){
        return Vehicle.getColor(id);
    }
    public double get_VehicleSpeed(String carID) {
        if(vehicleExists(carID)){
            return Vehicle.getSpeed(carID);
        }
        throw new SimObjectException("Vehicle " + carID + " does not exist");
    }
    public void set_VehicleSpeed(String CarID,double speed){
        Vehicle.setSpeed(CarID,speed);
    }
    public String getVehicleRouteID(String CarID){
        return Vehicle.getRouteID(CarID);
    }
    public double getVehicle_waitingtime(String CarID){
        return Vehicle.getWaitingTime(CarID);
    }
    public double getVehicle_accumulatedwaitingtime(String CarID){
        return Vehicle.getAccumulatedWaitingTime(CarID);
    }
    public List<String> get_Trafficlightids(){
        return new ArrayList<>(TrafficLight.getIDList());
    }
    public boolean trafficlight_exists(String TlID){
        return get_Trafficlightids().contains(TlID);
    }
    public int get_TrafficlightPhase(String id){
        return TrafficLight.getPhase(id);
    }
    public String get_Trafficstate(String id){
        return TrafficLight.getRedYellowGreenState(id);
    }
    public double get_Trafficlight_phaseduration(String id){
        return TrafficLight.getPhaseDuration(id);
    }
    public double get_Trafficlight_remaining_phaseduration(String id){
        return TrafficLight.getNextSwitch(id)-Simulation.getTime();
    }
    public List<String> getTrafficlightControlledLanes(String TrafficlightID){
        return TrafficLight.getControlledLanes(TrafficlightID);
    }
    public void set_TrafficLightState(String id,String state){
        TrafficLight.setRedYellowGreenState(id,state);
    }
    public String get_TrafficLightProgramm(String trafficLightID) {
        return TrafficLight.getProgram(trafficLightID);
    }
    public void set_TrafficLightProgramm(String trafficLightID,String programm){
        TrafficLight.setProgram(trafficLightID,programm);
    }
    public TraCILogicVector getTrafficLightLogic(String trafficlightID){
        return TrafficLight.getCompleteRedYellowGreenDefinition(trafficlightID);
    }
    public void setTrafficLightLogic(String trafficlightID,TraCILogic logic){
        TrafficLight.setCompleteRedYellowGreenDefinition(trafficlightID,logic);
    }

    public boolean existsEdge(String edgeID) {
        return get_EdgeIDList().contains(edgeID);
    }
    public List<String> get_EdgeIDList(){
        return new ArrayList<>(Edge.getIDList());
    }

    public List<String> getRouteEdges(String routeId) {
        return new ArrayList<>(org.eclipse.sumo.libtraci.Route.getEdges(routeId));
    }

    public String get_EdgeStreetname(String id){
        return Edge.getStreetName(id);
    }
    public List<String> get_EdgeLastStepVehicleIDs(String edgeID){
        return new ArrayList<>(Edge.getLastStepVehicleIDs(edgeID));
    }
    public int get_EdgeLastStepVehicleCount(String edgeID){
        return Edge.getLastStepVehicleNumber(edgeID);
    }
    public double get_EdgeLastStepAverageSpeed(String edgeID){
        return Edge.getLastStepMeanSpeed(edgeID);
    }
    public List<String> get_RouteIDList(){
        return new ArrayList<>(Route.getIDList());
    }
    public boolean existsLane(String LaneID){
        return Lane.getIDList().contains(LaneID);
    }

    public void set_VehicleColor(String vehicleId, java.awt.Color c) {
        org.eclipse.sumo.libtraci.TraCIColor tc =
                new org.eclipse.sumo.libtraci.TraCIColor(c.getRed(), c.getGreen(), c.getBlue(), 255);
        org.eclipse.sumo.libtraci.Vehicle.setColor(vehicleId, tc);
    }

    public void setVehicleRouteId(String vehicleId, String routeId) {
        org.eclipse.sumo.libtraci.Vehicle.setRouteID(vehicleId, routeId);
    }


    public List<String> get_customRouteIDList() {
        List<String> all_Routes = get_RouteIDList();
        List<String> custom_Routes = new ArrayList<>();

        for (String iter:all_Routes){
            if(!iter.startsWith("!")){
                custom_Routes.add(iter);
            }
        }
        return custom_Routes;
    }


    public String get_TrafficlightState(){return TrafficLightState;}

    // đéo thể hiểu gì được
    public Map<String, Character> getLaneSignalMap(String tlId) {
        Map<String, Character> laneSig = new HashMap<>();

        String state = TrafficLight.getRedYellowGreenState(tlId);
        if (state == null || state.isEmpty()) return laneSig;

        List<String> lanes = TrafficLight.getControlledLanes(tlId);
        if (lanes == null || lanes.isEmpty()) return laneSig;

        int n = Math.min(state.length(), lanes.size());
        for (int i = 0; i < n; i++) {
            String laneId = lanes.get(i);
            if (laneId == null || laneId.isEmpty()) continue;

            char sig = state.charAt(i);
            laneSig.put(laneId, mergeMostPermissive(laneSig.get(laneId), sig));
        }
        return laneSig;
    }



    private char mergeMostPermissive(Character cur, char next) {
        if (cur == null) return next;
        return priority(next) > priority(cur) ? next : cur;
    }

    private int priority(char c) {
        c = Character.toLowerCase(c);
        if (c == 'g') return 3;
        if (c == 'y') return 2;
        if (c == 'r') return 1;
        return 0;
    }

    // TEST TRAFFIC LIGHT
    public Position get_TrafficLightPosition(String trafficLightId) {
        TraCIPosition p = Junction.getPosition(trafficLightId);
        return new Position(p.getX(), p.getY());
        // TEST TRAFFIC LIGHT
    }
    public void setTrafficLightPhase(String tlsId, int phaseIndex) {
        org.eclipse.sumo.libtraci.TrafficLight.setPhase(tlsId, phaseIndex);
    }

    public void setTrafficLightPhaseDuration(String tlsId, double seconds) {
        org.eclipse.sumo.libtraci.TrafficLight.setPhaseDuration(tlsId, seconds);
    }

    // đéo thể hiểu gì được


}


