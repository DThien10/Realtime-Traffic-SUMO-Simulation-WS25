package SimulationWrapper;

import org.eclipse.sumo.libtraci.*;
import java.io.IOException;
import java.util.List;

public class SumoWrapper {

    private Process sumoProcess;
    private final int port=48042;

    private final String configPath;

    public SumoWrapper(String configPath) {
        this.configPath = configPath;
    }

    public void start () throws InterruptedException {

        Simulation.preloadLibraries();
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
    public void quit(){

        if(sumoProcess != null && sumoProcess.isAlive() ){
            sumoProcess.destroy();
        }
        System.out.println("Sumo process terminated");
    }

    public void step(){
        Simulation.step();
    }

    public StringVector getVehicleIDs (){
        return Vehicle.getIDList();
    }

    public double time(){
        return Simulation.getTime();
    }

}


