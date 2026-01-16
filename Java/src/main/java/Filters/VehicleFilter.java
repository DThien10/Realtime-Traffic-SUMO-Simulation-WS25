package Filters;

import SimulationObjects.SimVehicle;

public class VehicleFilter implements Filter<SimVehicle>{

    private double minSpeed=0;
    private double maxSpeed=1000;
    private Boolean isUserGenerated=null;

    public VehicleFilter(){
    }

    public VehicleFilter(double minSpeed){
        this.minSpeed=minSpeed;
    }
    public VehicleFilter(double minSpeed,double maxSpeed){
        this.minSpeed=minSpeed;
        this.maxSpeed=maxSpeed;
    }
    public VehicleFilter(double minSpeed, double maxSpeed,boolean isUserGenerated){
        this.minSpeed=minSpeed;
        this.maxSpeed=maxSpeed;
        this.isUserGenerated=isUserGenerated;
    }
    @Override
    public boolean check(SimVehicle vehicle) {

        boolean result ;
        if(isUserGenerated!=null){
           result= checkUserGenerated(vehicle);
           if(!result){return false;}
        }
        result=checkSpeed(vehicle);

        return result;

    }

    public boolean checkSpeed(SimVehicle vehicle){
        double speed= vehicle.getSpeed();
        return speed>=minSpeed&&speed<=maxSpeed;
    }

    public boolean checkUserGenerated(SimVehicle vehicle){
        boolean userGenerated = vehicle.isUserGenerated();
        return userGenerated==isUserGenerated;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public void setMinSpeed(double minSpeed) {
        this.minSpeed = minSpeed;
    }

    public void setUserGenerated(Boolean userGenerated) {
        isUserGenerated = userGenerated;
    }
}
