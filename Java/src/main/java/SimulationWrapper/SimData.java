package SimulationWrapper;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import SimulationObjects.SimEdge;
import SimulationObjects.SimTrafficlight;
import SimulationObjects.SimVehicle;

public class SimData {
    private final SumoWrapper wrapper;

    SimData(SumoWrapper wrapper){
        this.wrapper=wrapper;
    }

    //Simulation State Data
    private final Map<String, SimVehicle> vehicles=new HashMap<>();
    private final Set<String> added_Vehicles=new HashSet<>();
    private  List<String> custom_Routes;
    private final Set<SimTrafficlight> trafficlights= new HashSet<>();
    private final Set<SimEdge> edges = new HashSet<>();



    //getter/setter/updater methods
    public  Collection<SimVehicle> get_allVehicles(){return Collections.unmodifiableCollection(vehicles.values());}
    public  Set<String> get_addedVehicles(){return Collections.unmodifiableSet(added_Vehicles);}
    public  List<String> get_customRoutes(){return custom_Routes;}
    public Set<SimTrafficlight> getTrafficlightsSet(){return Collections.unmodifiableSet(trafficlights);}
    public Set<SimEdge> getEdgesSet(){return Collections.unmodifiableSet(edges);}
    public void initiateEdges(){
        List<String> allEdgesIDs = wrapper.get_EdgeIDList();

        for(String id:allEdgesIDs){
            edges.add(new SimEdge(id,wrapper));
        }
    }
    public void initiate_trafficlights() {
        List<String> allTrafficLightIDs = wrapper.get_Trafficlightids();

        for(String id:allTrafficLightIDs) {
        trafficlights.add(new SimTrafficlight(id,wrapper));
        }
    }
    public  void update_Vehicles(){
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
    public void updateEdgeData(){
        for(SimEdge e:edges){
            e.update();
        }
    }
    public  void UpdateAdded_Vehicles(String newEntry){added_Vehicles.add(newEntry);}
    public  void setCustom_Routes(){custom_Routes=wrapper.get_customRouteIDList();}
    public void initiate(){
        setCustom_Routes();
        initiate_trafficlights();
        initiateEdges();

    }
    public Collection<SimVehicle> get_SimVehicles(){
        return vehicles.values();
    }

    public void update(){
        update_Vehicles();
        updateEdgeData();

        //TEST TRAFFIC LIGHT
        for (SimTrafficlight t : trafficlights) {
        t.update();
    }
        //TEST TRAFFIC LIGHT

    }

//TODO start working on filtering methods


}


