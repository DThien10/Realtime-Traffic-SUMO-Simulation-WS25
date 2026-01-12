package SimulationObjects;

import SimulationWrapper.SumoWrapper;
import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.TrafficLight;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimTrafficlight extends SimObject{

    private String state;
    private String originalState;

    SimTrafficlight(String id, SumoWrapper wrapper){
        super(id, wrapper);
    }

    @Override
    public boolean exists() {
        return wrapper.trafficlight_exists(id);
    }

    public String getState(){
        return state;
    }

    public int getPhase(String id){
        return wrapper.get_TrafficlightPhase(id);
    }

    public double get_phaseduration(String id){
        return wrapper.get_Trafficlight_phaseduration(id);
    }
    public double get_remaining_phaseduration(String id) {
        return wrapper.get_Trafficlight_remaining_phaseduration(id);
    }


    public void set_red(){

        // Source - https://stackoverflow.com/a
        // Posted by Sean Patrick Floyd, modified by community. See post 'Timeline' for change history
        // Retrieved 2026-01-11, License - CC BY-SA 4.0

        String state= "*".repeat(originalState.length());

    }


}
