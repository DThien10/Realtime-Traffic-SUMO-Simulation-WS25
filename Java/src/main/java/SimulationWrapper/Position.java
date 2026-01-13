package SimulationWrapper;
//this class is being used instead of the libtraci Traciposition object for ease of use
public record Position(double x, double y) {


    public double getX(){return x;}
    public double getY(){return y;}

}
