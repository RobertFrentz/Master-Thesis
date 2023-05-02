package topology;

import org.apache.storm.kafka.bolt.selector.KafkaTopicSelector;
import org.apache.storm.tuple.Tuple;

public class DynamicTopicSelector implements KafkaTopicSelector {

    @Override
    public String getTopic(Tuple tuple) {
        return tuple.getStringByField("key");
    }
}
