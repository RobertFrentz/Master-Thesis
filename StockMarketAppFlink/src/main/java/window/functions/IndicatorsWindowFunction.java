package window.functions;

import com.codahale.metrics.UniformReservoir;
import domain.Event;
import domain.EventResults;
import domain.enums.SmoothingFactors;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.dropwizard.metrics.DropwizardHistogramWrapper;
import org.apache.flink.metrics.Histogram;
import org.apache.flink.shaded.guava30.com.google.common.util.concurrent.AtomicDouble;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class IndicatorsWindowFunction extends ProcessWindowFunction<Event, EventResults, String, TimeWindow> {
    private ValueState<EventResults> previousResults;

    //private transient AtomicDouble windowLatency;

    private transient Histogram histogram;



    @Override
    public void open(Configuration config) {
        ValueStateDescriptor<EventResults> descriptor =
                new ValueStateDescriptor<>("previousResults", Types.POJO(EventResults.class));
        previousResults = getRuntimeContext().getState(descriptor);
        //windowLatency = new AtomicDouble(0);
        com.codahale.metrics.Histogram dropwizardHistogram =
                new com.codahale.metrics.Histogram(new UniformReservoir());
        histogram = getRuntimeContext()
                .getMetricGroup()
                .histogram("windowLatencyInMilliseconds", new DropwizardHistogramWrapper(dropwizardHistogram));

    }

    @Override
    public void process(String s, ProcessWindowFunction<Event, EventResults, String, TimeWindow>.Context context, Iterable<Event> iterable, Collector<EventResults> collector) throws Exception {
        double eventsLatencyAverage = 0;
        long size = 0;
        for (Event event : iterable) {
            event.setWindowProcessingTime(event.getWindowProcessingTime() != null ? event.getWindowProcessingTime() : System.currentTimeMillis());
//            System.out.println("Computing indicators for event " + event.getId());
//            System.out.println(event.getWindowProcessingTime());

            EventResults previousResults = this.previousResults.value();
            EventResults results = new EventResults();

            results.setId(event.getId());
            results.setPrice(event.getLastTradePrice());
            results.setTime(event.getTimeOfLastUpdate());
            results.setDate(event.getDateOfLastTrade());
            results.setProcessingTime(event.getProcessingTime());

            computeEMA(event, results, previousResults);
            computeSMA(event, results, previousResults);

            //System.out.println("Collecting the following results for event " + results);

            this.previousResults.update(results);

            collector.collect(results);

            long windowEventLatency = System.currentTimeMillis() - event.getWindowProcessingTime();
            eventsLatencyAverage = (eventsLatencyAverage * size + windowEventLatency) / (size + 1);
            size++;
        }
        histogram.update((long)eventsLatencyAverage);
//        double latency = eventsLatencyAverage / 1000;
//        //System.out.println(latency);
//        windowLatency.set(latency);

        //System.out.println("\n \n \n");
        //System.out.println("Finished processing window" + "\n");
        //System.out.println(context.globalState() + "\n");
        //System.out.println("\n \n \n");
    }

    private void computeEMA(Event event, EventResults results, EventResults previousResults){
        //System.out.println("Checking EMA previous results for event " + event.getId());

        if (previousResults != null) {
            //System.out.println("Previous EMA results for event " + event.getId() + ": " + previousResults.getEMA38() + ", " + previousResults.getEMA100());

            results.setEMA38(computeEmaForCurrentWindow(event.getLastTradePrice(), previousResults.getEMA38(), SmoothingFactors.LOW));
            results.setEMA100(computeEmaForCurrentWindow(event.getLastTradePrice(), previousResults.getEMA100(), SmoothingFactors.HIGH));
            results.setBreakoutPattern(getBreakoutPattern(results, previousResults));
        } else {
            results.setEMA38(event.getLastTradePrice());
            results.setEMA100(event.getLastTradePrice());
            results.setBreakoutPattern("No Pattern Detected");
        }
    }

    private void computeSMA(Event event,  EventResults results, EventResults previousResults) {

        if (previousResults != null) {
            //System.out.println("Previous SMA result for event " + event.getId() + ": " + previousResults.getSMA2());
            results.setSMA2((previousResults.getPrice() + event.getLastTradePrice()) / 2);
        } else {
            results.setSMA2(event.getLastTradePrice());
        }

        //System.out.println("Current Window SMA result for event is: " + results.getSMA2());
    }

    private double computeEmaForCurrentWindow(double currentWindowSymbolPrice, double previousWindowSymbolPrice, Integer smoothingFactor) {

        //System.out.println("Current window last price for event: " + currentWindowSymbolPrice);
        //System.out.println("Last window EMA for event: " + previousWindowSymbolPrice);

        double factor = smoothingFactor;

        double result = currentWindowSymbolPrice * (2 / (1 + factor)) + previousWindowSymbolPrice * (1 - (2 / (1 + factor)));

        //System.out.println("Current Window Ema Result is: " + result);

        return result;
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

        return previousResults.getBreakoutPattern() == null ? "No Pattern Detected" : previousResults.getBreakoutPattern();
    }
}
