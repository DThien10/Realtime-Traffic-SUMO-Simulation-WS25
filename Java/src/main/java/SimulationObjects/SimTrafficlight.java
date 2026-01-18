package SimulationObjects;

import java.util.List;
import java.util.Map;

import SimulationWrapper.SumoWrapper;

public class SimTrafficlight extends SimObject{
//TODO add phase duration for map rendering
    private String state;
    private String originalState;
    private final String originalProgramm;

    // TEST TRAFFIC LIGHT
    private int phase;
    private double phaseDuration;
    private double remainingPhaseDuration;
    // TEST TRAFFIC LIGHT



    public SimTrafficlight(String id, SumoWrapper wrapper){
        super(id, wrapper);
        state = wrapper.get_Trafficstate(id);
        originalState=state;
        originalProgramm=wrapper.get_TrafficLightProgramm(id);
    }

    public void update() {
        state = wrapper.get_Trafficstate(id);

        // TEST TRAFFIC LIGHT

        phase = wrapper.get_TrafficlightPhase(id);
        phaseDuration = wrapper.get_Trafficlight_phaseduration(id);
        remainingPhaseDuration = wrapper.get_Trafficlight_remaining_phaseduration(id);

    }
    // TEST TRAFFIC LIGHT

    // lấy map lane-signals cho việc render đèn giao thông
    public Map<String, Character> getLaneSignals() {
    return wrapper.getLaneSignalMap(id);
    // lấy map lane-signals cho việc render đèn giao thông
}




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


    public void set_red() {
        String cur = wrapper.get_Trafficstate(id);
        if (cur == null) return;
        wrapper.set_TrafficLightState(id, "r".repeat(cur.length()));
        update();

    }

    // TEST TRAFFIC LIGHT
    public void set_green() {
        String cur = wrapper.get_Trafficstate(id);
        if (cur == null) return;
        wrapper.set_TrafficLightState(id, "G".repeat(cur.length()));
        update();

    }

    public void set_original() {
        // Trả quyền điều khiển lại cho SUMO (auto-phase)
        wrapper.set_TrafficLightProgramm(id, originalProgramm);
        update();

    }

    public String toDisplayString() {
        String s = getState();
        int phase = getPhase();
        double rem = get_remaining_phaseduration();

        // quick “dominant” color label
        String colorLabel = "RED";
        if (s != null && (s.contains("G") || s.contains("g"))) colorLabel = "GREEN";
        else if (s != null && (s.contains("y") || s.contains("Y"))) colorLabel = "YELLOW";

        return String.format("%s | %s | phase=%d | rem=%.1fs", id, colorLabel, phase, rem);
    }
    // TEST TRAFFIC LIGHT
}