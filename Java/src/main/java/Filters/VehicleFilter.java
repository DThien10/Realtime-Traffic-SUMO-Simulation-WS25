package Filters;

import SimulationObjects.SimVehicle;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class VehicleFilter implements Filter<SimVehicle>{

    private double minSpeed=0;
    private double maxSpeed=1000;
    private boolean checkForUserGenerated =false;
    private boolean checkForColor=true;
    private Set<Color> colors=new HashSet<>(allDefaultColors);

    //Default colors used in SUMO
    public static final Color YELLOW=new Color(255,255,0);
    public static final Color CYAN=new Color(0,255,255);
    public static final Color RED=new Color(255,0,0);
    public static final Color BLUE=new Color(0,0,255);
    public static final Color TEAL=new Color(10,255,160);
    private static final Set<Color> allDefaultColors= Set.of(CYAN,TEAL,BLUE,RED,YELLOW);

    public VehicleFilter(){
    }

    public VehicleFilter(double minSpeed){
        this.minSpeed=minSpeed;
    }
    public VehicleFilter(double minSpeed,double maxSpeed){
        this.minSpeed=minSpeed;
        this.maxSpeed=maxSpeed;
    }
    public VehicleFilter(double minSpeed, double maxSpeed,boolean CheckForUserGenerated){
        this.minSpeed=minSpeed;
        this.maxSpeed=maxSpeed;
        this.checkForUserGenerated = CheckForUserGenerated;
    }
    @Override
    public boolean check(SimVehicle vehicle) {


        if(checkForUserGenerated&&!checkUserGenerated(vehicle)){
            return false;
        }
        if(checkForColor&&!checkColors(vehicle)) {
            return false;  }

        return checkSpeed(vehicle);

    }

    public boolean checkSpeed(SimVehicle vehicle){
        double speed= vehicle.getSpeed();
        return speed>=minSpeed&&speed<=maxSpeed;
    }

    public boolean checkUserGenerated(SimVehicle vehicle){
        return vehicle.isUserGenerated();
    }
    public boolean checkColors(SimVehicle vehicle){
        Color color=vehicle.getColor();
        return colors.contains(color);
    }
    public void setColors(Set<Color> colors){
        this.colors=new HashSet<>(colors);

    }
    public void addColor(Color color){
        colors.add(color);
    }
    public void removeColor(Color color) {
        colors.remove(color);
    }
    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setMinSpeed(double minSpeed) {
        this.minSpeed = minSpeed;
    }

    public void setCheckForUserGenerated(Boolean checkForUserGenerated) {
        this.checkForUserGenerated = checkForUserGenerated;
    }

    public void setCheckForColor(boolean checkForColor) {
        this.checkForColor = checkForColor;
    }

    public boolean getCheckForUserGenerated(){
        return checkForUserGenerated;
    }
}
