package SimulationObjects;

import SimulationWrapper.SumoWrapper;

public class SimTrafficlight extends SimObject{
//TODO add phase duration for map rendering
    private String state;
    private String originalState;
    private final String originalProgramm;

    public SimTrafficlight(String id, SumoWrapper wrapper){
        super(id, wrapper);
        state = wrapper.get_Trafficstate(id);
        originalState=state;
        originalProgramm=wrapper.get_TrafficLightProgramm(id);
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

        System.out.println(state+" : old state");
        String state= "r".repeat(originalState.length());
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
        wrapper.set_TrafficLightProgramm(id,"0");
        System.out.println(originalProgramm+" : program running");
    }


}
