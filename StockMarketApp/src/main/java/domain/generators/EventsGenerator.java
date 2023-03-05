package domain.generators;

import domain.Event;
import domain.enums.SecurityType;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

public class EventsGenerator {

    public static List<Event> getDummyEvents(){
        return Arrays.asList(
                new Event("A2ASZ7.ETR", SecurityType.EQUITY, 7.1801, LocalTime.of(2, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("A0HN5C.ETR", SecurityType.EQUITY, 97.1288, LocalTime.of(4, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("A0HN5C.ETR", SecurityType.EQUITY, 90.8288, LocalTime.of(8, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("SGF.FR", SecurityType.EQUITY, 70.9673, LocalTime.of(6, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("SGF.FR", SecurityType.EQUITY, 45.0519, LocalTime.of(7, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("NRP.NL", SecurityType.EQUITY, 4.4919, LocalTime.of(10, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("NRP.NL", SecurityType.EQUITY, 11.1908, LocalTime.of(9, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("A2N9WV.ETR", SecurityType.EQUITY, 8.768, LocalTime.of(10, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("SGT1D.FR", SecurityType.INDEX, 58.0091, LocalTime.of(11, 5, 4, 1), LocalDate.of(2023, 3, 4)),
                new Event("MALA.FR", SecurityType.EQUITY, 54.4501, LocalTime.of(12, 5, 4, 1), LocalDate.of(2023, 3, 4))
        );
    }
}
