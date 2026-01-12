package SimulationWrapper;

import SimulationObjects.SimVehicle;

import java.util.*;

public class SimData {
    //Simulation State Data
    private final Map<String, SimVehicle> vehicles=new HashMap<>();
    private final Set<String> added_Vehicles=new HashSet<>();
    private  List<String> custom_Routes;


    //getter/setter/updater methods
    public  Collection<SimVehicle> get_allVehicles(){return Collections.unmodifiableCollection(vehicles.values());}
    public  Set<String> get_addedVehicles(){return Collections.unmodifiableSet(added_Vehicles);}
    public  List<String> get_customRoutes(){return custom_Routes;}

    public  void update_Vehicles(SumoWrapper wrapper){
        List<String> currentIDs = wrapper.getVehicleIDs();



        for(String id:currentIDs){

           if(!vehicles.containsKey(id)){
               vehicles.put(id,new SimVehicle(id,wrapper));
           }

        }
        vehicles.keySet().removeIf(id -> !currentIDs.contains(id));

        for(SimVehicle v:vehicles.values()){
            v.update();
        }
    }
    public  void UpdateAdded_Vehicles(String newEntry){added_Vehicles.add(newEntry);}
    public  void setCustom_Routes(SumoWrapper wrapper){custom_Routes=wrapper.get_customRouteIDList();}
    public void initiate(SumoWrapper wrapper){
        setCustom_Routes(wrapper);

    }
    public Collection<SimVehicle> get_SimVehicles(){
        return vehicles.values();
    }

    public void update(SumoWrapper wrapper){
        update_Vehicles(wrapper);

    }



}


