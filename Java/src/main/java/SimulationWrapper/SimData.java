package SimulationWrapper;

import java.util.ArrayList;
import java.util.List;

public class SimData {
    private  List<String> all_Vehicles;
    private  List<String> added_Vehicles=new ArrayList<>();
    private  List<String> custom_Routes;



    public  List<String> get_allVehicles(){return all_Vehicles;}
    public  List<String> get_addedVehicles(){return added_Vehicles;}
    public  List<String> get_customRoutes(){return custom_Routes;}

    public  void update_Vehicles(SumoWrapper wrapper){all_Vehicles=wrapper.getVehicleIDs();}
    public  void UpdateAdded_Vehicles(String newEntry){added_Vehicles.add(newEntry);}
    public  void setCustom_Routes(SumoWrapper wrapper){custom_Routes=wrapper.get_customRouteIDList();}

    public void initiate(SumoWrapper wrapper){
        setCustom_Routes(wrapper);
    }

    public void update(SumoWrapper wrapper){
        update_Vehicles(wrapper);
    }



}


