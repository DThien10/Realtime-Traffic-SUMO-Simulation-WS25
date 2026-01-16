package SimulationObjects;

import SimulationWrapper.Position;
import SimulationWrapper.SumoWrapper;// TEST TRAFFIC LIGHT
import org.eclipse.sumo.libtraci.TraCILogicVector;

import java.util.List;

public class SimTrafficlight extends SimObject{
//TODO add phase duration for map rendering
    private String state;
    private String originalState;
    private final String originalProgramm;

    private Position position; // TEST TRAFFIC LIGHT

    public SimTrafficlight(String id, SumoWrapper wrapper){
        super(id, wrapper);
        state = wrapper.get_Trafficstate(id);
        originalState=state;
        originalProgramm=wrapper.get_TrafficLightProgramm(id);
       // System.out.println("Trafficlight logic: "+ originalProgramm.getFirst());

        // TEST TRAFFIC LIGHT
        try {
            position = wrapper.get_TrafficLightPosition(id);
        } catch (Exception e) {
            position = null;
        }
        // TEST TRAFFIC LIGHT
    }

    // TEST TRAFFIC LIGHT
    public Position getPosition() {
        return position;
    }

    public void update() {
        state = wrapper.get_Trafficstate(id);
    }
    // TEST TRAFFIC LIGHT



    @Override
    public boolean exists() {
        return wrapper.trafficlight_exists(id);
    }

    public String getState(){
        return state;
    }

    public int getPhase(){
        return wrapper.get_TrafficlightPhase(this.id);
    }

    public double get_phaseduration(){
        return wrapper.get_Trafficlight_phaseduration(this.id);
    }
    public double get_remaining_phaseduration() {
        return wrapper.get_Trafficlight_remaining_phaseduration(id);
    }
    public List<String> getControlledLanes(){
        return wrapper.getTrafficlightControlledLanes(id);
    }


    public void set_red(){

        // Source - https://stackoverflow.com/a
        // Posted by Sean Patrick Floyd, modified by community. See post 'Timeline' for change history
        // Retrieved 2026-01-11, License - CC BY-SA 4.0

        System.out.println(state+" : old state");
        state= "r".repeat(originalState.length());
        wrapper.set_TrafficLightState(id,state);
        System.out.println(state+" : new state");


    }

    public void set_green(){


        System.out.println(state+" : old state");
        state= "G".repeat(originalState.length());
        wrapper.set_TrafficLightState(id,state);
        System.out.println(state+" : new state");


    }

    public void set_original(){
        System.out.println(state+" : old state");
        state= originalState;
        wrapper.set_TrafficLightState(id,state);
        wrapper.set_TrafficLightProgramm(id, originalProgramm); // TEST TRAFFIC LIGHT
        System.out.println(originalProgramm+" : program running");
    }


}
