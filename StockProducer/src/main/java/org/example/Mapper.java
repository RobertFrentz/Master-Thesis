package org.example;

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
        return new Event(eventCSV.ID, eventCSV.SecType, eventCSV.Last, eventCSV.TradingTime, eventCSV.TradingDate);
    }
}
