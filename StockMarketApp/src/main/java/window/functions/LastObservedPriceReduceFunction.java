package window.functions;

import domain.Event;
import helpers.EventDateTimeHelper;
import org.apache.flink.api.common.functions.ReduceFunction;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

//TODO TEST THIS SEPARATELY, TEST WITH TIMESTAMPS THEN TEST WITH DEDUPLICATION

public class LastObservedPriceReduceFunction implements ReduceFunction<Event> {

    //private final Set<String> seen = new HashSet<>();
    @Override
    public Event reduce(Event event1, Event event2) throws Exception {

        //System.out.println("Hash set contains: " + seen);
        System.out.println("First Event");
        System.out.println(event1.toString());
        System.out.println("Second Event");
        System.out.println(event2.toString());


        final long event1Timestamp = EventDateTimeHelper.getDateTimeInMillis(event1);
        final long event2Timestamp = EventDateTimeHelper.getDateTimeInMillis(event2);

        if(event1Timestamp < event2Timestamp){
            return event1;
        }

        return event2;
//
//        final LocalTime event1Time = LocalTime.parse(event1.getTimeOfLastUpdate());
//        final LocalTime event2Time = LocalTime.parse(event2.getTimeOfLastUpdate());

//        if(event1Time.isBefore(event2Time)){
//            if(!seen.contains(event1.getId())) {
//                seen.add(event1.getId());
//                return event1;
//            }
//        } else{
//            if(!seen.contains(event2.getId())){
//                seen.add(event2.getId());
//                return event2;
//            }
//        }
//
//        return null;

//        if(event1Time.isBefore(event2Time)){
//            return event1;
//        }
//
//        return event2;
    }
}
