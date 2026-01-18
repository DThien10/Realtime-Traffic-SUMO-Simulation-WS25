package SimulationObjects;

import Filters.VehicleFilter;
import SimulationWrapper.Position;
import SimulationWrapper.SumoWrapper;
import org.eclipse.sumo.libtraci.TraCIColor;

import java.awt.*;

public class SimVehicle extends SimObject{

    private Position position;
    private Color color;
    private double speed;
    private final boolean userGenerated;

    public SimVehicle(String id, SumoWrapper wrapper){
        super(id,wrapper);
        TraCIColor traCIColor= wrapper.get_Vehiclecolor(id);
        color=new Color(traCIColor.getR(),traCIColor.getG(),traCIColor.getB());
        userGenerated= id.startsWith("Random_add") || id.startsWith("GUI");

    }

    @Override
    public boolean exists() {
        return wrapper.vehicleExists(id);
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

    public boolean isUserGenerated(){
        return userGenerated;
    }
    public Color getColor(){return color;}
    public void setColor(Color color){
        this.color=color;
        wrapper.set_VehicleColor(id,color);
    }

    private String getColorName(Color c) {
        if (c.equals(Color.YELLOW)) return "Yellow";
        if (c.equals(Color.RED)) return "Red";
        if (c.equals(Color.BLUE)) return "Blue";
        if (c.equals(VehicleFilter.TEAL)) return "Teal";
        if (c.equals(VehicleFilter.CYAN)) return "Cyan";

        // fallback nếu màu lạ
        return String.format("RGB(%d,%d,%d)", c.getRed(), c.getGreen(), c.getBlue());
    }

    public String toDisplayString() {


        // đổi màu từ Color → tên dễ hiểu
        String colorName = getColorName(color);

        // đổi normal/special → chữ dễ hiểu
        String source = userGenerated ? "Injected vehicle" : "System vehicle";

        return String.format(
                "%s | Speed: %.1f m/s | Color: %s | %s",
                id, speed, colorName, source
        );
    }
}
