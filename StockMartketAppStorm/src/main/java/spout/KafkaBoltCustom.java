package spout;

import com.codahale.metrics.Gauge;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

public class KafkaBoltCustom extends KafkaBolt<String, String> {

    private transient AtomicDouble endToEndLatencyInSeconds;
    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        super.prepare(topoConf, context, collector);
        endToEndLatencyInSeconds = new AtomicDouble(0);
        Gauge<Double> gauge = () -> endToEndLatencyInSeconds.get();
        context.registerGauge("endToEndLatencyInSeconds", gauge);
    }

    @Override
    protected void process(Tuple input) {
        super.process(input);
        endToEndLatencyInSeconds.set((double)(System.currentTimeMillis() - (long)input.getValueByField("processingTime"))/1000);
    }
}
