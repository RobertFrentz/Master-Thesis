package window.functions;

import domain.Event;
import domain.generators.EventsGenerator;
import org.apache.flink.streaming.api.functions.source.RichParallelSourceFunction;

import java.security.SecureRandom;
import java.util.List;

public class RandomEventSource extends RichParallelSourceFunction<Event> {

    private volatile boolean cancelled = false;
    private final SecureRandom random = new SecureRandom();

    @Override
    public void run(SourceContext<Event> sourceContext){
        List<Event> events = EventsGenerator.getDummyEvents();
        System.out.println(events);
        int currentGenerations = 0;
        while (!cancelled && currentGenerations < 100) {
            int max = events.size();
            int randomIndex = random.nextInt(max);
            synchronized (sourceContext.getCheckpointLock()) {
                sourceContext.collect(events.get(randomIndex));
            }
            System.out.println("Current generations" + currentGenerations);
            currentGenerations++;
        }
    }

    @Override
    public void cancel() {
        cancelled = true;
    }
}
