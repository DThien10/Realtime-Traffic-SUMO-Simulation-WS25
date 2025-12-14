package SimulationWrapper;

import org.eclipse.sumo.libtraci.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class SumoWrapper {
    private final static Logger WrapperLogger = Logger.getLogger(SumoWrapper.class.getName());
    private Process sumoProcess;
    private final int port=48042;

    private final String configPath;

    public SumoWrapper(String configPath) {
        this.configPath = configPath;
    }
    public static String openguiYN(){
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
        ProcessBuilder pb = new ProcessBuilder(openguiYN(),
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

    public double time(){
        return Simulation.getTime();
    }

    public List<String> getVehicleIDs (){
        return new ArrayList<>(Vehicle.getIDList());
    }
    public boolean VehicleExists(String CarID){
        if(Vehicle.getIDList().contains(CarID)){
            return true;
        }else{
            return false;
        }
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
        if(VehicleExists(CarID)){
            Vehicle.remove(CarID);}
        else WrapperLogger.warning("Vehicle with the ID: "+ CarID+" does not exist");

    }
    //Yet to see if usable or need to implement own color class
    public TraCIColor get_Vehiclecolor(String id){
        return Vehicle.getColor(id);
    }
    public double get_VehicleSpeed(String CarID) {
        return Vehicle.getSpeed(CarID);
    }
    public void set_VehicleSpeed(String CarID,double speed){
        Vehicle.setSpeed(CarID,speed);
    }
    public List<String> get_Trafficlightids(){
        return new ArrayList<>(TrafficLight.getIDList());
    }
    public int get_TrafficlightPhase(String id){
        return TrafficLight.getPhase(id);
    }
    public String get_TrafficlightColor(String id){
        return TrafficLight.getRedYellowGreenState(id);
    }
    public double get_Trafficlight_phaseduration(String id){
        return TrafficLight.getPhaseDuration(id);
    }
    public double get_Trafficlight_remaining_phaseduration(String id){
        return TrafficLight.getNextSwitch(id)-Simulation.getTime();
    }
    public List<String> get_EdgesIDList(){
        return new ArrayList<>(Edge.getIDList());
    }
    public String get_EdgeStreetname(String id){
        return Edge.getStreetName(id);
    }
    public List<String> get_RouteIDList(){
        return new ArrayList<>(Route.getIDList());
    }

    public List<String> get_customRouteIDList() {
        List<String> all_Routes = new ArrayList<>(Route.getIDList());
        List<String> custom_Routes = new ArrayList<>();

        for (String iter:all_Routes){
            if(!iter.startsWith("!")){
                custom_Routes.add(iter);
            }
        }
        return custom_Routes;
    }

    public void toggleTls(){
        List<String> allTls = get_Trafficlightids();

    }
}


