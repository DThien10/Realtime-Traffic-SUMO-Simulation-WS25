package SimulationObjects;

import SimulationWrapper.SumoWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Object corresponding to Traci.Edge. Keeps track of average Cars and speed
 */
public class SimEdge extends SimObject {

    private Set<Integer> averageCarsSet=new HashSet<>();
    private Set<Double> averageSpeedSet=new HashSet<>();
    private List<SimLane> lanes= new ArrayList<>();

    public SimEdge(String id, SumoWrapper wrapper){
        super(id,wrapper);
    }

    @Override
    public boolean exists() {
        return wrapper.existsEdge(id);
    }
    public int getLastStepCars(){
        return wrapper.get_EdgeLastStepVehicleCount(id);
    }
    public double getLastStepSpeed(){
        return wrapper.get_EdgeLastStepAverageSpeed(id);
    }


    public void update(){
        averageCarsSet.add(getLastStepCars());
        averageSpeedSet.add(getLastStepSpeed());
    }

    public double getAverageCars(){
        int sum=0;

        for(Integer i:averageCarsSet){
            sum+=i;
        }

        double result=0;
        result= (double) sum /averageCarsSet.size();
        return result;
    }

    public double getAverageSpeed(){
        double sum=0;

        for(double i:averageSpeedSet){
            sum+=i;
        }

        sum= sum /averageSpeedSet.size();
        return sum;
    }

    public List<SimLane> getLanes() {
        return lanes;
    }

    public void setLanes(List<SimLane> lanes) {
        this.lanes = lanes;
    }
    public void addLane(SimLane lane){
        lanes.add(lane);
    }

    public String getStreetName() {
        try {
            String n = wrapper.get_EdgeStreetname(id);
            return (n == null || n.isBlank()) ? null : n;
        } catch (Exception e) {
            return null;
        }
    }

    public String toDisplayString() {
        int cars = getLastStepCars();
        double speed = getLastStepSpeed();

        String name = getStreetName();
        String title = (name != null) ? name : "Edge";

        // congestion rule (tùy bạn chỉnh)
        boolean congested = cars >= 10 && speed <= 2.0;

        return String.format(
                "%s | id=%s | cars=%d | v=%.1f m/s%s",
                title, id, cars, speed, congested ? " | CONGESTED" : ""
        );
    }
}
