package domain.generators;

import domain.Event;
import domain.enums.SecurityType;
import helpers.DateTimeCustomFormatter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;

public class EventsGenerator {

    public static List<Event> getDummyEvents(){

        return Arrays.asList(
                new Event("A2ASZ7.ETR", SecurityType.EQUITY.toString(), 7.1801, "02:06:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("A0HN5C.ETR", SecurityType.EQUITY.toString(), 97.1288, "04:05:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("A0HN5C.ETR", SecurityType.EQUITY.toString(), 90.8288, "08:05:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("SGF.FR", SecurityType.EQUITY.toString(), 70.9673, "06:05:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("SGF.FR", SecurityType.EQUITY.toString(), 45.0519, "07:05:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("NRP.NL", SecurityType.EQUITY.toString(), 4.4919, "10:05:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("NRP.NL", SecurityType.EQUITY.toString(), 11.1908, "09:05:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("A2N9WV.ETR", SecurityType.EQUITY.toString(), 8.768, "10:05:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("SGT1D.FR", SecurityType.INDEX.toString(), 58.0091, "11:05:04", LocalDate.of(2023, 3, 4).toString()),
                new Event("MALA.FR", SecurityType.EQUITY.toString(), 54.4501, "12:05:04", LocalDate.of(2023, 3, 4).toString())
        );
    }
}
