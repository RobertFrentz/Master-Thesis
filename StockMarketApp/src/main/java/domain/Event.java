package domain;

import domain.enums.SecurityType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Event {
    String id;
    SecurityType securityType;
    double lastTradePrice;
    LocalTime timeOfLastUpdate;
    LocalDate DateOfLastTrade;

    public Event() {
    }

    public Event(String id, SecurityType securityType, double lastTradePrice, LocalTime timeOfLastUpdate, LocalDate dateOfLastTrade) {
        this.id = id;
        this.securityType = securityType;
        this.lastTradePrice = lastTradePrice;
        this.timeOfLastUpdate = timeOfLastUpdate;
        this.DateOfLastTrade = dateOfLastTrade;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SecurityType getSecurityType() {
        return securityType;
    }

    public void setSecurityType(SecurityType securityType) {
        this.securityType = securityType;
    }

    public double getLastTradePrice() {
        return lastTradePrice;
    }

    public void setLastTradePrice(double lastTradePrice) {
        this.lastTradePrice = lastTradePrice;
    }

    public LocalTime getTimeOfLastUpdate() {
        return timeOfLastUpdate;
    }

    public void setTimeOfLastUpdate(LocalTime timeOfLastUpdate) {
        this.timeOfLastUpdate = timeOfLastUpdate;
    }

    public LocalDate getDateOfLastTrade() {
        return DateOfLastTrade;
    }

    public void setDateOfLastTrade(LocalDate dateOfLastTrade) {
        DateOfLastTrade = dateOfLastTrade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Double.compare(event.lastTradePrice, lastTradePrice) == 0 && Objects.equals(id, event.id) && securityType == event.securityType && Objects.equals(timeOfLastUpdate, event.timeOfLastUpdate) && Objects.equals(DateOfLastTrade, event.DateOfLastTrade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, securityType, lastTradePrice, timeOfLastUpdate, DateOfLastTrade);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", securityType=" + securityType +
                ", lastTradePrice=" + lastTradePrice +
                ", timeOfLastUpdate=" + timeOfLastUpdate +
                ", DateOfLastTrade=" + DateOfLastTrade +
                '}';
    }
}
