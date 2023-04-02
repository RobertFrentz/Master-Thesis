package org.example;

import java.util.Objects;

public class EventCSV {
    String ID;
    String SecType;
    double Last;
    String TradingTime;
    String TradingDate;

    public EventCSV() {
    }

    public EventCSV(String ID, String secType, double last, String tradingTime, String tradingDate) {
        this.ID = ID;
        SecType = secType;
        Last = last;
        TradingTime = tradingTime;
        TradingDate = tradingDate;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getSecType() {
        return SecType;
    }

    public void setSecType(String secType) {
        SecType = secType;
    }

    public double getLast() {
        return Last;
    }

    public void setLast(double last) {
        Last = last;
    }

    public String getTradingTime() {
        return TradingTime;
    }

    public void setTradingTime(String tradingTime) {
        TradingTime = tradingTime;
    }

    public String getTradingDate() {
        return TradingDate;
    }

    public void setTradingDate(String tradingDate) {
        TradingDate = tradingDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventCSV eventCSV = (EventCSV) o;
        return Double.compare(eventCSV.Last, Last) == 0 && Objects.equals(ID, eventCSV.ID) && Objects.equals(SecType, eventCSV.SecType) && Objects.equals(TradingTime, eventCSV.TradingTime) && Objects.equals(TradingDate, eventCSV.TradingDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ID, SecType, Last, TradingTime, TradingDate);
    }

    @Override
    public String toString() {
        return "EventCSV{" +
                "ID='" + ID + '\'' +
                ", SecType='" + SecType + '\'' +
                ", Last=" + Last +
                ", TradingTime='" + TradingTime + '\'' +
                ", TradingDate='" + TradingDate + '\'' +
                '}';
    }
}
