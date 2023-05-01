package bolt;

import domain.Event;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.state.KeyValueState;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseStatefulBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProcessingBolt extends BaseStatefulBolt<KeyValueState<String, String>> {

    public KeyValueState<String, String> state;
    private OutputCollector collector;
    private static final Logger LOG = LogManager.getLogger(ProcessingBolt.class);
    private AdminClient kafkaAdminClient;

    public void prepare(Map config, TopologyContext context, OutputCollector collector) {
        this.kafkaAdminClient = createKafkaAdminClient();
        this.collector = collector;
    }

    public void execute(Tuple tuple) {
        String key = tuple.getStringByField("key");
        Event value = (Event) tuple.getValueByField("value");

        LOG.info("Reading message: " + key);
        LOG.info("Object: " + value);

        LOG.info("Previous Event with id " + key + ": " + state.get(key));

        try {
            if(!topicAlreadyExists(key)){
                createNewTopic(key);
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        LOG.info("Created topic: " + key);

        state.put(key, value.toString());

        collector.emit(tuple, new Values(key, value.toString()));
        collector.ack(tuple);
    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("key", "value", "__anchor"));
    }

    private AdminClient createKafkaAdminClient(){
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "kafka:9092");
        return KafkaAdminClient.create(properties);
    }

    private boolean topicAlreadyExists(String topicName) throws ExecutionException, InterruptedException {
        ListTopicsResult topicsResult = kafkaAdminClient.listTopics();
        return topicsResult.names().get().contains(topicName);
    }

    private void createNewTopic(String topicName){
        int numPartitions = 1;
        short replicationFactor = 1;
        NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
        kafkaAdminClient.createTopics(Collections.singletonList(newTopic));
    }

    @Override
    public void cleanup() {
        super.cleanup();
        kafkaAdminClient.close();
    }

    @Override
    public void initState(KeyValueState<String, String> state) {
        this.state = state;
    }
}
