package SimulationObjects;

import SimulationWrapper.SumoWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

//TODO look into performance this took a big hit
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
}
