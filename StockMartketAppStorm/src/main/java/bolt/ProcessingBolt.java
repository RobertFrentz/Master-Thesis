package bolt;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.*;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.*;
import topology.KafkaTopology;

import java.util.Map;

public class ProcessingBolt extends BaseRichBolt {
    private OutputCollector collector;
    private static final Logger LOG = LogManager.getLogger(ProcessingBolt.class);

    public void prepare(Map config, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    public void execute(Tuple tuple) {
        // Get the message from the Kafka Spout

        String key = tuple.getString(0);
        String value = tuple.getString(1);

        LOG.info("Reading message: " + key);
        LOG.info("Object: " + value);

        System.out.println("Hello World: " + key);
        System.out.println("Right: " + value);

        // Perform any processing or transformations
        //String processedMessage = processMessage(key);

        // Emit the processed message to the next bolt or sink
        collector.emit(new Values(key + " " + value));
        collector.ack(tuple);
    }

    private String processMessage(String message) {
        // Implement any custom processing logic here
        return message.toUpperCase();
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("value"));
    }
}
