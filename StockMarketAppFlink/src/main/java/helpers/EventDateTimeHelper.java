package helpers;

import domain.Event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class EventDateTimeHelper {

    public static long getDateTimeInMillis(Event event) {
        //ToDo needs to be tested
        LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        LocalTime time = LocalTime.parse(event.getTimeOfLastUpdate(), formatter);
        LocalDate date = LocalDate.parse(event.getDateOfLastTrade());
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
