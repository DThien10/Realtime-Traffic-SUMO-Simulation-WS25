package SimulationObjects;

import SimulationWrapper.Position;
import SimulationWrapper.SumoWrapper;
import org.eclipse.sumo.libtraci.TraCIColor;

import java.awt.*;

public class SimVehicle extends SimObject{

    private Position position;
    private Color color;
    private double speed;
    private final boolean special;

    public SimVehicle(String id, SumoWrapper wrapper){
        super(id,wrapper);
        TraCIColor traCIColor= wrapper.get_Vehiclecolor(id);
        color=new Color(traCIColor.getR(),traCIColor.getG(),traCIColor.getB());
        special= id.startsWith("Random_add");
    }

    @Override
    public boolean exists() {
        return wrapper.VehicleExists(id);
    }

    public void update(){
        position=wrapper.get_VehiclePos(id);
        speed= wrapper.get_VehicleSpeed(id);
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
    public Color getColor(){return color;}
}
