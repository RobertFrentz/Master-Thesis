package topology;

import bolt.WindowBolt;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;
import spout.KafkaBoltCustom;
import spout.KafkaSpoutCustom;

import java.util.Properties;

public class KafkaTopology {

    private static final Logger LOG = LogManager.getLogger(KafkaTopology.class);
    public static void main(String[] args) throws Exception {

        TopologyOptions options = new TopologyOptions(args);

        LOG.info("The following parameters were received \n" + options + "\n");

        // Set up the Kafka Spout
        // String brokerUrl = "kafka:9092";
        String topicName = "trade-data";
//        KafkaSpoutConfig<String, String> kafkaSpoutConfig = KafkaSpoutConfig.builder(options.brokerUrl, topicName)
//                .setFirstPollOffsetStrategy(FirstPollOffsetStrategy.EARLIEST)
//                .setEmitNullTuples(false)
//                .setRecordTranslator(new KafkaRecordTranslator())
//                .setProp(ConsumerConfig.GROUP_ID_CONFIG, "storm-group1")
//                .setProp(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "600000")
//                .setProp(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
//                .setProp(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonEventDeserializer.class)
//                .setProp(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
//                .build();
//        KafkaSpout<String, String> kafkaSpout = new KafkaSpout<>(kafkaSpoutConfig);

        // Set up the Kafka Bolt
        String outputTopicName = "storm-output-topic";
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.brokerUrl);
        kafkaProps.put("acks", options.numOfAcks);
        kafkaProps.put("key.serializer", StringSerializer.class);
        kafkaProps.put("value.serializer", StringSerializer.class);
        KafkaBolt<String, String> kafkaBolt = new KafkaBoltCustom()
                .withProducerProperties(kafkaProps)
                //.withTopicSelector(new DynamicTopicSelector())
                .withTopicSelector(outputTopicName)
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<>("key", "value"));

        // Set up the Topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-spout", new KafkaSpoutCustom(), options.numOfExecutorsForKafkaSpout)
                .setNumTasks(options.numOfTasksForKafkaSpout);
        //builder.setBolt("processing-bolt", new ProcessingBolt()).shuffleGrouping("kafka-spout");
        builder.setBolt("window-bolt",
                        new WindowBolt()
                                .withTumblingWindow(BaseWindowedBolt.Duration.seconds(options.windowDuration))
                                .withMessageIdField("msgid")
                                .withTimestampField("timeStamp")
                                .withLag(BaseWindowedBolt.Duration.seconds(5)),
                        options.numOfExecutorsForWindowBolt)
                .shuffleGrouping("kafka-spout")
                .setNumTasks(options.numOfTasksForWindowBolt);
        builder.setBolt("kafka-bolt", kafkaBolt, options.numOfExecutorsForKafkaBolt)
                .shuffleGrouping("window-bolt")
                .setNumTasks(options.numOfTasksForKafkaBolt);

        // Submit the Topology
        Config config = new Config();
        config.put(Config.TOPOLOGY_BOLTS_MESSAGE_ID_FIELD_NAME, "msgid");
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, options.windowDuration * 2 + 30);
        config.setNumWorkers(options.numOfWorkers);
        config.setStatsSampleRate(1.0);
        config.setDebug(true);
        StormSubmitter.submitTopology("my-topology", config, builder.createTopology());
    }
}
