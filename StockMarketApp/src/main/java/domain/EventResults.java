package domain;

import java.util.Objects;

//TODO CREATE A EMA CLASS WITH THE FOLLOWING PROPERTIES: SYMBOL ID, QUERY WITH SMOOTHING FACTOR 38, QUERY WITH SMOOTHING FACTOR 100
public class EventResults {
    String id;
    double EMA38;

    double EMA100;

    String breakoutPattern;

    public EventResults() {
    }

    public EventResults(String id, double EMA38, double EMA100, String breakoutPattern) {
        this.id = id;
        this.EMA38 = EMA38;
        this.EMA100 = EMA100;
        this.breakoutPattern = breakoutPattern;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getEMA38() {
        return EMA38;
    }

    public void setEMA38(double EMA38) {
        this.EMA38 = EMA38;
    }

    public double getEMA100() {
        return EMA100;
    }

    public void setEMA100(double EMA100) {
        this.EMA100 = EMA100;
    }

    public String getBreakoutPattern() {
        return breakoutPattern;
    }

    public void setBreakoutPattern(String breakoutPattern) {
        this.breakoutPattern = breakoutPattern;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventResults that = (EventResults) o;
        return Double.compare(that.EMA38, EMA38) == 0 && Double.compare(that.EMA100, EMA100) == 0 && Objects.equals(id, that.id) && Objects.equals(breakoutPattern, that.breakoutPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, EMA38, EMA100, breakoutPattern);
    }

    @Override
    public String toString() {
        return "EventResults{" +
                "id='" + id + '\'' +
                ", EMA38=" + EMA38 +
                ", EMA100=" + EMA100 +
                ", breakoutPattern='" + breakoutPattern + '\'' +
                '}';
    }
}
