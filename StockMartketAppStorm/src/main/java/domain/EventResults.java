package domain;

import java.io.Serializable;
import java.util.Objects;

public class EventResults implements Serializable {
    String id;

    double price;

    double EMA38;

    double EMA100;

    double SMA2;

    String breakoutPattern;

    public EventResults() {
    }

    public EventResults(String id, double price, double EMA38, double EMA100, double SMA2, String breakoutPattern) {
        this.id = id;
        this.price = price;
        this.EMA38 = EMA38;
        this.EMA100 = EMA100;
        this.breakoutPattern = breakoutPattern;
        this.SMA2 = SMA2;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public double getSMA2() {
        return SMA2;
    }

    public void setSMA2(double SMA2) {
        this.SMA2 = SMA2;
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
        return Double.compare(that.price, price) == 0 && Double.compare(that.EMA38, EMA38) == 0 && Double.compare(that.EMA100, EMA100) == 0 && Double.compare(that.SMA2, SMA2) == 0 && Objects.equals(id, that.id) && Objects.equals(breakoutPattern, that.breakoutPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, price, EMA38, EMA100, SMA2, breakoutPattern);
    }

    @Override
    public String toString() {
        return "EventResults{" +
                "id='" + id + '\'' +
                ", price=" + price +
                ", EMA38=" + EMA38 +
                ", EMA100=" + EMA100 +
                ", SMA2=" + SMA2 +
                ", breakoutPattern='" + breakoutPattern + '\'' +
                '}';
    }
}
