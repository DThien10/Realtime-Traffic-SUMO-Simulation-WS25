package SimulationWrapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import SimulationObjects.SimEdge;
import SimulationObjects.SimTrafficlight;
import SimulationObjects.SimVehicle;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class SimData {
    private final SumoWrapper wrapper;

    SimData(SumoWrapper wrapper) {
        this.wrapper = wrapper;
    }

    //Simulation State Data
    private final Map<String, SimVehicle> vehicles = new HashMap<>();
    private final Set<String> added_Vehicles = new HashSet<>();
    private List<String> custom_Routes;
    private final Set<SimTrafficlight> trafficlights = new HashSet<>();
    private Set<SimEdge> edges;
    private final Map<String, List<String>> routesByStartEdge = new HashMap<>();

    private final static Logger simDataLogger = Logger.getLogger(SimData.class.getName());



    //getter/setter/updater methods
    public Collection<SimVehicle> get_allVehicles() {
        return Collections.unmodifiableCollection(vehicles.values());
    }

    public Set<String> get_addedVehicles() {
        return Collections.unmodifiableSet(added_Vehicles);
    }

    public List<String> get_customRoutes() {
        return custom_Routes;
    }

    public Set<SimTrafficlight> getTrafficlightsSet() {
        return Collections.unmodifiableSet(trafficlights);
    }

    public Set<SimEdge> getEdgesSet() {
        return edges;
    }

    public void initiateEdges() {
        List<String> allEdgesIDs = wrapper.get_EdgeIDList();

        for (String id : allEdgesIDs) {
            edges.add(new SimEdge(id, wrapper));
        }
    }

    public RenderSnapshot getSimulationSnapshot() {
        return new RenderSnapshot(new HashSet<>(vehicles.values()), trafficlights,edges);
    }

    public void initiateEdges(String netPath) {
        NetworkReader networkReader = new NetworkReader(netPath, wrapper);
        try {
            edges = networkReader.readEdgeData();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);

        }
    }

    public void initiate_trafficlights() {
        List<String> allTrafficLightIDs = wrapper.get_Trafficlightids();

        for (String id : allTrafficLightIDs) {
            trafficlights.add(new SimTrafficlight(id, wrapper));
        }
    }

    public void update_Vehicles() {
        List<String> currentIDs = wrapper.getVehicleIDs();


        for (String id : currentIDs) {

            if (!vehicles.containsKey(id)) {
                vehicles.put(id, new SimVehicle(id, wrapper));
            }

        }
        vehicles.keySet().removeIf(id -> !currentIDs.contains(id));

        for (SimVehicle v : vehicles.values()) {
            try {
                v.update();
            }catch(SimObjectException e){
                simDataLogger.warning(e.getMessage());
            }
        }
    }

    public void updateEdgeData() {
        for (SimEdge e : edges) {
            e.update();
        }
    }

    public void UpdateAdded_Vehicles(String newEntry) {
        added_Vehicles.add(newEntry);
    }

    public void setCustom_Routes() {
        custom_Routes = wrapper.get_customRouteIDList();
    }

    public void initiate(String netPath) {
        setCustom_Routes();
        initiate_trafficlights();
        initiateEdges(netPath);
        buildRoutesByStartEdge();
    }

    public Collection<SimVehicle> get_SimVehicles() {
        return vehicles.values().stream().collect(Collectors.toUnmodifiableSet());
    }

    public void update() {
        update_Vehicles();
        updateEdgeData();

        //TEST TRAFFIC LIGHT
        for (SimTrafficlight t : trafficlights) {
            t.update();
        }
        //TEST TRAFFIC LIGHT

    }

    public void registerVehicle(String id) {
       // vehicles.computeIfAbsent(id, k -> new SimulationObjects.SimVehicle(k, wrapper));
       // vehicles.get(id).update(); // lấy position/speed ngay
    }

    public void buildRoutesByStartEdge() {
        routesByStartEdge.clear();
        if (custom_Routes == null) return;

        for (String routeId : custom_Routes) {
            List<String> edgesInRoute = wrapper.getRouteEdges(routeId);
            if (!edgesInRoute.isEmpty()) {
                String startEdge = edgesInRoute.get(0);
                routesByStartEdge.computeIfAbsent(startEdge, k -> new java.util.ArrayList<>()).add(routeId);
            }
        }
    }

    public Map<String, List<String>> getRoutesByStartEdge() {
        return Collections.unmodifiableMap(routesByStartEdge);
    }





}



