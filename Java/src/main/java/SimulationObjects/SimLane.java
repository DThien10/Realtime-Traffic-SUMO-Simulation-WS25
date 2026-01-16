package SimulationObjects;

import SimulationWrapper.Position;
import SimulationWrapper.SumoWrapper;

import java.util.ArrayList;
import java.util.List;

public class SimLane extends SimObject{

    List<Position> shape;

    public SimLane(String LaneID, SumoWrapper wrapper, List<Position> shape){
        super(LaneID,wrapper);
        this.shape=shape;
    }

    @Override
    public boolean exists() {
        return wrapper.existsLane(id);
    }

    public List<Position> getShape() {
        return shape;
    }


}
