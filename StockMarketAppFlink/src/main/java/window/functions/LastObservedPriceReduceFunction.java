package window.functions;

import domain.Event;
import helpers.EventDateTimeHelper;
import org.apache.flink.api.common.functions.ReduceFunction;

public class LastObservedPriceReduceFunction implements ReduceFunction<Event> {

    @Override
    public Event reduce(Event event1, Event event2) {

        long currentTime = System.currentTimeMillis();
        event1.setWindowProcessingTime(currentTime);
        event2.setWindowProcessingTime(currentTime);

        final long event1Timestamp = EventDateTimeHelper.getDateTimeInMillis(event1);
        final long event2Timestamp = EventDateTimeHelper.getDateTimeInMillis(event2);

        if(event2Timestamp >= event1Timestamp){
            return event2;
        }

        return event1;
    }
}
