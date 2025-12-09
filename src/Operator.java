//each operator goes inside the OperatorPanel

public class Operator {
    private String function;
    private String period;
    private double frequency;
    private int id;

    public Operator(int identification) {
        id = identification;
        function = "sin(t)";      // default
        period = "2*pi";          // default
        frequency = 200;          // default
    }
    // Getters
    public int getId() { return id; }
    public String getFunction() { return function; }
    public String getPeriod() { return period; }
    public double getFrequency() { return frequency; }

    // Setters
    public void setFunction(String f) { function = f; }
    public void setPeriod(String p) { period = p; }
    public void setFrequency(double f) { frequency = f; }
    public void setId(int id) { this.id = id; }
}