package helper;

import domain.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

public class EventDateTimeHelper {

    public static long getDateTimeInMillis(Event event) {
        LocalTime time = LocalTime.parse(event.getTimeOfLastUpdate());
        LocalDate date = LocalDate.parse(event.getDateOfLastTrade());
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
