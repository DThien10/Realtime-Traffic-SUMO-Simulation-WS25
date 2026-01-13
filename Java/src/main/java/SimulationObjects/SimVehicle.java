package SimulationObjects;

import SimulationWrapper.Position;
import SimulationWrapper.SumoWrapper;

public class SimVehicle extends SimObject{
//TODO add a color class
    private Position position;
    private final boolean special;

    public SimVehicle(String id, SumoWrapper wrapper){
        super(id,wrapper);
        special= id.startsWith("Random_add");
    }

    @Override
    public boolean exists() {
        return wrapper.VehicleExists(id);
    }

    public void update(){
        position=wrapper.get_VehiclePos(id);
    }

    public Position getPosition(){
        return position;
    }

    public double getSpeed(){
        return wrapper.get_VehicleSpeed(id);
    }

    public boolean isSpecial(){
        return special;
    }
}
