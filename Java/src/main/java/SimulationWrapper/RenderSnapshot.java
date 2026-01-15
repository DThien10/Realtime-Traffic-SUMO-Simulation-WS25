package SimulationWrapper;

import SimulationObjects.*;

import java.util.Collection;


public record RenderSnapshot(Collection<SimVehicle> vehicles, Collection<SimTrafficlight> lights) {

}
