package spout;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.UniformReservoir;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.tuple.Tuple;

import java.util.Map;

public class KafkaBoltCustom extends KafkaBolt<String, String> {

    //private transient AtomicDouble endToEndLatencyInSeconds;
   // private static final Logger LOG = LogManager.getLogger(KafkaBoltCustom.class);
    private transient Histogram histogram;
    private transient Meter meter;
    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        super.prepare(topoConf, context, collector);
//        endToEndLatencyInSeconds = new AtomicDouble(0);
//        Gauge<Double> gauge = () -> endToEndLatencyInSeconds.get();
//        context.registerGauge("endToEndLatencyInSeconds", gauge);
        histogram = context.registerHistogram("frameworkLatencyInMilliSeconds");
        meter = context.registerMeter("frameworkThroughput");
    }

    @Override
    protected void process(Tuple input) {
        super.process(input);
        this.meter.mark();
        long frameworkLatency = System.currentTimeMillis() - (long)input.getValueByField("processingTime");
        //LOG.info("Framework Latency " + frameworkLatency);
        this.histogram.update(frameworkLatency);
        //endToEndLatencyInSeconds.set((double)(System.currentTimeMillis() - (long)input.getValueByField("processingTime"))/1000);
    }
}
