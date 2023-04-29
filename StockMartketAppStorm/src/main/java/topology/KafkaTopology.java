package topology;

import bolt.ProcessingBolt;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
import bolt.WindowBolt;
import domain.Event;
import domain.JsonEventDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.Bolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.kafka.spout.*;
import org.apache.storm.kafka.bolt.*;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.state.KeyValueState;
import org.apache.storm.topology.*;
import org.apache.storm.Config;
import org.apache.storm.topology.base.BaseWindowedBolt;

import java.time.Duration;
import java.util.Properties;

public class KafkaTopology {

    private static final Logger LOG = LogManager.getLogger(KafkaTopology.class);
    public static void main(String[] args) throws Exception {
        // Set up the Kafka Spout

        String brokerUrl = "kafka:9092";
        String topicName = "trade-data";
        KafkaSpoutConfig<String, String> kafkaSpoutConfig = KafkaSpoutConfig.builder(brokerUrl, topicName)
                .setFirstPollOffsetStrategy(FirstPollOffsetStrategy.EARLIEST)
                .setProp(ConsumerConfig.GROUP_ID_CONFIG, "storm-group")
                .setProp(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "600000")
                .setProp(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
                .setProp(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonEventDeserializer.class)
                .setProp(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                .build();
        KafkaSpout<String, String> kafkaSpout = new KafkaSpout<>(kafkaSpoutConfig);

        LOG.info("Registered Kafka Spout");

        // Set up the Kafka Bolt
        String outputTopicName = "storm-output-topic";
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerUrl);
        kafkaProps.put("acks", "1");
        kafkaProps.put("key.serializer", StringSerializer.class);
        kafkaProps.put("value.serializer", StringSerializer.class);
        KafkaBolt<String, String> kafkaBolt = new KafkaBolt<String, String>()
                .withProducerProperties(kafkaProps)
                .withTopicSelector(new DefaultTopicSelector(outputTopicName))
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<>("key", "value"));

        // Set up the Topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-spout", kafkaSpout);
        //builder.setBolt("processing-bolt", new ProcessingBolt()).shuffleGrouping("kafka-spout");
        builder.setBolt("window-bolt",
                new WindowBolt()
                        .withTumblingWindow(BaseWindowedBolt.Duration.seconds(20)))
                .shuffleGrouping("kafka-spout");
        builder.setBolt("kafka-bolt", kafkaBolt).shuffleGrouping("window-bolt");

        // Submit the Topology
        Config config = new Config();
        config.put(Config.TOPOLOGY_BOLTS_MESSAGE_ID_FIELD_NAME, "__kafka_offset");
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, 360);
        config.setDebug(true);
        StormSubmitter.submitTopology("my-topology", config, builder.createTopology());
    }
}
