package helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.EventResults;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.metrics.Gauge;
import org.apache.flink.shaded.guava30.com.google.common.util.concurrent.AtomicDouble;

public class CustomRichMapper extends RichMapFunction<String, String> {
    private transient AtomicDouble endToEndLatencyInSeconds;
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        endToEndLatencyInSeconds = new AtomicDouble(0);
        getRuntimeContext()
                .getMetricGroup()
                .gauge("endToEndLatencyInSeconds", (Gauge<Double>) endToEndLatencyInSeconds::get);
    }
    @Override
    public String map(String json) throws Exception {
        EventResults event = mapper.readValue(json, EventResults.class);
        double latency = (double)(System.currentTimeMillis() - event.getProcessingTime())/1000;
        endToEndLatencyInSeconds.set(latency);
        return json;
    }
}
