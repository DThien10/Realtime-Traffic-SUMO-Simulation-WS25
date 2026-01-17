package Filters;

import SimulationObjects.SimVehicle;

public class VehicleFilter implements Filter<SimVehicle>{

    private double minSpeed=0;
    private double maxSpeed=1000;
    private boolean checkForUserGenerated =false;

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

        boolean result=true ;
        if(checkForUserGenerated){
            result=checkUserGenerated(vehicle);
        }
        if(result) {
            result = checkSpeed(vehicle);
        }
        return result;

    }

    public boolean checkSpeed(SimVehicle vehicle){
        double speed= vehicle.getSpeed();
        return speed>=minSpeed&&speed<=maxSpeed;
    }

    public boolean checkUserGenerated(SimVehicle vehicle){
        return vehicle.isUserGenerated();
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
    public boolean getCheckForUserGenerated(){
        return checkForUserGenerated;
    }
}
