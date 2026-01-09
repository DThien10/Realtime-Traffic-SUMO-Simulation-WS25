package SimulationWrapper;

public record Position(double x, double y,boolean special) {


    public double getX(){return x;}
    public double getY(){return y;}
    public boolean getSpecial(){return special;}
}
