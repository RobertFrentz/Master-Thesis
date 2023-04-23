package topology;

import bolt.ProcessingBolt;
//import org.apache.kafka.clients.consumer.ConsumerConfig;
//import org.apache.kafka.common.serialization.StringDeserializer;
import domain.JsonEventDeserializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.Bolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.kafka.spout.*;
import org.apache.storm.kafka.bolt.*;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.topology.*;
import org.apache.storm.Config;

import java.util.Properties;

public class KafkaTopology {

    private static final Logger LOG = LogManager.getLogger(KafkaTopology.class);
    public static void main(String[] args) throws Exception {
        // Set up the Kafka Spout

        String brokerUrl = "kafka:9092";
        String topicName = "trade-data";
        KafkaSpoutConfig<String, String> kafkaSpoutConfig = KafkaSpoutConfig.builder(brokerUrl, topicName)
                .setFirstPollOffsetStrategy(FirstPollOffsetStrategy.EARLIEST)
                .setProp("group.id", "storm-group")
                .setProp("default.api.timeout.ms", "600000")
                .setProp("key.serializer", "org.apache.kafka.common.serialization.StringDeserializer")
                .setProp("value.serializer", new JsonEventDeserializer())
                .setProp("auto.offset.reset", "earliest")
                .build();
        KafkaSpout<String, String> kafkaSpout = new KafkaSpout<>(kafkaSpoutConfig);

        LOG.info("Registered Kafka Spout");

        // Set up the Kafka Bolt
        String outputTopicName = "storm-output-topic";
        Properties kafkaProps = new Properties();
        kafkaProps.put("bootstrap.servers", brokerUrl);
        kafkaProps.put("acks", "1");
        kafkaProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaBolt<String, String> kafkaBolt = new KafkaBolt<String, String>()
                .withProducerProperties(kafkaProps)
                .withTopicSelector(new DefaultTopicSelector(outputTopicName))
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<String, String>("key", "value"));

        // Set up the Topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-spout", kafkaSpout);
        builder.setBolt("processing-bolt", new ProcessingBolt()).shuffleGrouping("kafka-spout");
        builder.setBolt("kafka-bolt", kafkaBolt).shuffleGrouping("processing-bolt");

        // Submit the Topology
        Config config = new Config();
        config.setDebug(true);
        StormSubmitter.submitTopology("my-topology", config, builder.createTopology());
    }
}
