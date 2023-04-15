package window.functions;

import domain.EventResults;
import domain.Event;
import domain.enums.SmoothingFactors;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

//TODO CREATE A EMA CLASS WITH THE FOLLOWING PROPERTIES: SYMBOL ID, QUERY WITH SMOOTHING FACTOR 38, QUERY WITH SMOOTHING FACTOR 100
//TODO CHECK IF THIS IT WORKS


public class EMAWindowFunction extends ProcessWindowFunction<Event, EventResults, String, TimeWindow> {
    private ValueState<EventResults> previousResults;

    @Override
    public void open(Configuration config) throws Exception {
        ValueStateDescriptor<EventResults> descriptor =
                new ValueStateDescriptor<>("previousResults", Types.POJO(EventResults.class));
        previousResults = getRuntimeContext().getState(descriptor);
    }

    @Override
    public void process(String s, ProcessWindowFunction<Event, EventResults, String, TimeWindow>.Context context, Iterable<Event> iterable, Collector<EventResults> collector) throws Exception {
        for (Event event : iterable) {
            System.out.println("Computing EMA for event " + event.getId());

            EventResults previousResults = this.previousResults.value();
            EventResults results = new EventResults();

            System.out.println("Checking EMA previous results for event " + event.getId());

            if (previousResults != null) {
                System.out.println("Previous EMA results for event " + event.getId() + ": " + previousResults.getEMA38() + ", " + previousResults.getEMA100());

                results.setEMA38(computeEmaForCurrentWindow(event.getLastTradePrice(), previousResults.getEMA38(), SmoothingFactors.LOW));
                results.setEMA100(computeEmaForCurrentWindow(event.getLastTradePrice(), previousResults.getEMA100(), SmoothingFactors.HIGH));
                results.setBreakoutPattern(getBreakoutPattern(results, previousResults));
            } else {
                results.setEMA38(event.getLastTradePrice());
                results.setEMA100(event.getLastTradePrice());
                results.setBreakoutPattern("No pattern Detected");
            }

            System.out.println("Collecting the following EMA results for event " + event.getId() + ": " + results.getEMA38() + ", " + results.getEMA100());

            collector.collect(results);
            this.previousResults.update(results);
        }
    }

    private Double computeEmaForCurrentWindow(Double currentWindowSymbolPrice, Double previousWindowSymbolPrice, Integer smoothingFactor) {
        return currentWindowSymbolPrice * (2 / (1 + smoothingFactor)) + previousWindowSymbolPrice * (1 - (2 / (1 + smoothingFactor)));
    }

    private String getBreakoutPattern(EventResults previousResults, EventResults currentResults){
        final boolean isBearishBreakoutPattern = currentResults.getEMA38() < currentResults.getEMA100()
                && previousResults.getEMA38() >= previousResults.getEMA100();

        if(isBearishBreakoutPattern){
            return "Bearish";
        }

        final boolean isBullishBreakoutPattern = currentResults.getEMA38() > currentResults.getEMA100()
                && previousResults.getEMA38() <= previousResults.getEMA100();

        if(isBullishBreakoutPattern)
        {
            return "Bullish";
        }

        return "No Pattern Detected";
    }
}
