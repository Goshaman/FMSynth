//each operator goes inside the OperatorPanel

public class Operator {
    private String function;
    private String period;
    private double frequency;
    private int id;
    private boolean isCarrier;

    public Operator(int identification) {
        id = identification;
        function = "sin(t)";      // default
        period = "2*pi";          // default
        frequency = 200;          // default
        isCarrier = true;         // default to carrier
    }
    // Getters
    public int getId() { return id; }
    public String getFunction() { return function; }
    public String getPeriod() { return period; }
    public double getFrequency() { return frequency; }
    public boolean isCarrier() { return isCarrier; }

    // Setters
    public void setFunction(String f) { function = f; }
    public void setPeriod(String p) { period = p; }
    public void setFrequency(double f) { frequency = f; }
    public void setId(int id) { this.id = id; }
    public void setCarrier(boolean carrier) { isCarrier = carrier; }
}