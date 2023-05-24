package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Mapper {

    public static List<Event> mapEventsFrom(List<EventCSV> eventCSVList)
    {
        List<Event> events = new ArrayList<>();

        for(EventCSV eventCSV : eventCSVList)
        {
            events.add(mapEventFrom(eventCSV));
        }

        return events;
    }

    public static Event mapEventFrom(EventCSV eventCSV)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        LocalTime time = LocalTime.parse(eventCSV.getTradingTime(), formatter);
        return new Event(eventCSV.ID, eventCSV.SecType, eventCSV.Last, time.toString(), eventCSV.TradingDate);
    }
}
