package SimulationWrapper;

public class SimObjectException extends RuntimeException {
    public SimObjectException(String message) {
        super(message);
    }

    public SimObjectException(String message, Throwable cause) {
        super(message, cause);
    }
}
