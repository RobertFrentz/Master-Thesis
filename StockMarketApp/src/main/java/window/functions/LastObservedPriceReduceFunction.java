package window.functions;

import domain.Event;
import org.apache.flink.api.common.functions.ReduceFunction;

public class LastObservedPriceReduceFunction implements ReduceFunction<Event> {
    @Override
    public Event reduce(Event event1, Event event2) throws Exception {

//        System.out.println("First Event");
//        System.out.println(event1.toString());
//        System.out.println("Second Event");
//        System.out.println(event2.toString());

        if(event1.getTimeOfLastUpdate().isBefore(event2.getTimeOfLastUpdate())){
            return event1;
        }

        return event2;
    }
}
