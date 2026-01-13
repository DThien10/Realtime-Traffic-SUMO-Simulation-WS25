package SimulationObjects;

import SimulationWrapper.SumoWrapper;


public abstract class SimObject {

    protected final String id;
    protected final SumoWrapper wrapper;

    SimObject(String id, SumoWrapper wrapper){
        this.id=id;
        this.wrapper=wrapper;
    }

    public String getId(){return id;}

    public abstract boolean exists();

}
