package domain;

import java.io.Serializable;
import java.util.Objects;

public class Event implements Serializable {
    String id;
    String securityType;
    double lastTradePrice;
    String timeOfLastUpdate;
    String DateOfLastTrade;

    public Event() {
    }

    public Event(String id, String securityType, double lastTradePrice, String timeOfLastUpdate, String dateOfLastTrade) {
        this.id = id;
        this.securityType = securityType;
        this.lastTradePrice = lastTradePrice;
        this.timeOfLastUpdate = timeOfLastUpdate;
        DateOfLastTrade = dateOfLastTrade;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public double getLastTradePrice() {
        return lastTradePrice;
    }

    public void setLastTradePrice(double lastTradePrice) {
        this.lastTradePrice = lastTradePrice;
    }

    public String getTimeOfLastUpdate() {
        return timeOfLastUpdate;
    }

    public void setTimeOfLastUpdate(String timeOfLastUpdate) {
        this.timeOfLastUpdate = timeOfLastUpdate;
    }

    public String getDateOfLastTrade() {
        return DateOfLastTrade;
    }

    public void setDateOfLastTrade(String dateOfLastTrade) {
        DateOfLastTrade = dateOfLastTrade;
    }

    public boolean isAfter(Event otherEvent){
        final long event1Timestamp = helpers.EventDateTimeHelper.getDateTimeInMillis(this);
        final long event2Timestamp = helpers.EventDateTimeHelper.getDateTimeInMillis(otherEvent);

        return event1Timestamp >= event2Timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Double.compare(event.lastTradePrice, lastTradePrice) == 0 && Objects.equals(id, event.id) && Objects.equals(securityType, event.securityType) && Objects.equals(timeOfLastUpdate, event.timeOfLastUpdate) && Objects.equals(DateOfLastTrade, event.DateOfLastTrade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, securityType, lastTradePrice, timeOfLastUpdate, DateOfLastTrade);
    }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", securityType='" + securityType + '\'' +
                ", lastTradePrice=" + lastTradePrice +
                ", timeOfLastUpdate='" + timeOfLastUpdate + '\'' +
                ", DateOfLastTrade='" + DateOfLastTrade + '\'' +
                '}';
    }
}
