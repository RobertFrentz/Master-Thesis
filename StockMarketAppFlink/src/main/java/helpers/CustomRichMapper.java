package helpers;

import com.codahale.metrics.UniformReservoir;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Event;
import domain.EventResults;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.dropwizard.metrics.DropwizardHistogramWrapper;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.metrics.Histogram;
import org.apache.flink.shaded.guava30.com.google.common.util.concurrent.AtomicDouble;

public class CustomRichMapper extends RichMapFunction<EventResults, String> {
    private transient AtomicDouble endToEndLatencyInSeconds;
    private final ObjectMapper mapper = new ObjectMapper();

    private transient Histogram histogram;

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        //endToEndLatencyInSeconds = new AtomicDouble(0);
//        getRuntimeContext()
//                .getMetricGroup()
//                .gauge("endToEndLatencyInSeconds", (Gauge<Double>) endToEndLatencyInSeconds::get);
        com.codahale.metrics.Histogram dropwizardHistogram =
                new com.codahale.metrics.Histogram(new UniformReservoir());
        histogram = getRuntimeContext()
                .getMetricGroup()
                .histogram("endToEndLatencyInMilliseconds", new DropwizardHistogramWrapper(dropwizardHistogram));
    }
    @Override
    public String map(EventResults event) throws Exception {
        String mappedEvent = mapper.writeValueAsString(event);
        //double latency = (double)(System.currentTimeMillis() - event.getProcessingTime())/1000;
        //endToEndLatencyInSeconds.set(latency);
        histogram.update(System.currentTimeMillis() - event.getProcessingTime());
        return mappedEvent;
    }
}
