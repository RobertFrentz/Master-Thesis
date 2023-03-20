package window.functions;

import domain.Event;
import org.apache.flink.api.common.functions.ReduceFunction;

import java.time.LocalTime;

public class LastObservedPriceReduceFunction implements ReduceFunction<Event> {
    @Override
    public Event reduce(Event event1, Event event2) throws Exception {

//        System.out.println("First Event");
//        System.out.println(event1.toString());
//        System.out.println("Second Event");
//        System.out.println(event2.toString());

        final LocalTime event1Time = LocalTime.parse(event1.getTimeOfLastUpdate());
        final LocalTime event2Time = LocalTime.parse(event2.getTimeOfLastUpdate());

        if(event1Time.isBefore(event2Time)){
            return event1;
        }

        return event2;
    }
}
