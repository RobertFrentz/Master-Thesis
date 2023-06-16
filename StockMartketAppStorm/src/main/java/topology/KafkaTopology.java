package topology;

import bolt.WindowBolt;
import domain.JsonEventDeserializer;
import helper.KafkaRecordTranslator;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.spout.FirstPollOffsetStrategy;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.kafka.spout.subscription.NamedTopicFilter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;
import spout.KafkaBoltCustom;

import java.util.Properties;

public class KafkaTopology {

    private static final Logger LOG = LogManager.getLogger(KafkaTopology.class);
    public static void main(String[] args) throws Exception {

        TopologyOptions options = new TopologyOptions(args);

        LOG.info("The following parameters were received \n" + options + "\n");

        // Set up the Kafka Spout
        // String brokerUrl = "kafka:9092";
        String topicName = "trade-data";
        KafkaSpoutConfig<String, String> kafkaSpoutConfig = new KafkaSpoutConfig.Builder<String, String>(options.brokerUrl, new NamedTopicFilter("trade-data"), new CustomPartitioner())
                .setFirstPollOffsetStrategy(FirstPollOffsetStrategy.EARLIEST)
                .setProcessingGuarantee(KafkaSpoutConfig.ProcessingGuarantee.AT_MOST_ONCE)
                .setEmitNullTuples(false)
                .setRecordTranslator(new KafkaRecordTranslator())
                .setProp(ConsumerConfig.GROUP_ID_CONFIG, "storm-group-bench")
                .setProp(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, "600000")
                .setProp(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
                .setProp(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonEventDeserializer.class)
                .setProp(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
//                .setPollTimeoutMs(0)
//                .setMaxUncommittedOffsets(1000)
//                .setOffsetCommitPeriodMs(1000)
//                .setProp(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 10000000)
//                .setProp(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 100000000)
//                .setProp(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 100)
//                .setProp(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10485760)
//                .setProp(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 10485760)
                .build();
        KafkaSpout<String, String> kafkaSpout = new KafkaSpout<>(kafkaSpoutConfig);

        // Set up the Kafka Bolt
        String outputTopicName = "storm-output-topic";
        Properties kafkaProps = new Properties();
        kafkaProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.brokerUrl);
        kafkaProps.put("acks", options.numOfAcks);
        kafkaProps.put("key.serializer", StringSerializer.class);
        kafkaProps.put("value.serializer", StringSerializer.class);
        //kafkaProps.put("value.serializer", JsonEventSerializer.class);
        KafkaBolt<String, String> kafkaBolt = new KafkaBoltCustom()
                .withProducerProperties(kafkaProps)
                //.withTopicSelector(new DynamicTopicSelector())
                .withTopicSelector(outputTopicName)
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<>("key", "value"));

        // Set up the Topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-spout", kafkaSpout, options.numOfExecutorsForKafkaSpout)
                .setNumTasks(options.numOfTasksForKafkaSpout);
        //builder.setBolt("processing-bolt", new ProcessingBolt()).shuffleGrouping("kafka-spout");
        builder.setBolt("window-bolt",
                        new WindowBolt()
                                .withTumblingWindow(BaseWindowedBolt.Duration.seconds(options.windowDuration))
                                .withMessageIdField("msgid")
                                .withTimestampField("timeStamp")
                                .withLag(BaseWindowedBolt.Duration.seconds(1)),
                        options.numOfExecutorsForWindowBolt)
                .shuffleGrouping("kafka-spout")
                .setNumTasks(options.numOfTasksForWindowBolt);

        builder.setBolt("kafka-bolt", kafkaBolt, options.numOfExecutorsForKafkaBolt)
                .shuffleGrouping("window-bolt")
                .setNumTasks(options.numOfTasksForKafkaBolt);

        // Submit the Topology
        Config config = new Config();
        config.put(Config.TOPOLOGY_BOLTS_MESSAGE_ID_FIELD_NAME, "msgid");
        config.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS, options.windowDuration * 2 + 1);
        config.setNumWorkers(options.numOfWorkers);
        config.setStatsSampleRate(0.001);
        config.setDebug(false);
       // config.setTopologyWorkerMaxHeapSize(1500);
        config.setNumAckers(2);
        //config.setMaxSpoutPending(5000);
//        config.put(Config.TOPOLOGY_TRANSFER_BUFFER_SIZE, 32);
//        config.put(Config.TOPOLOGY_TRANSFER_BATCH_SIZE, 1000);
        //config.put(Config.TOPOLOGY_BATCH_FLUSH_INTERVAL_MILLIS, 1000);
        config.put(Config.TOPOLOGY_PRODUCER_BATCH_SIZE, 1000);
        config.put(Config.TOPOLOGY_EXECUTOR_RECEIVE_BUFFER_SIZE, 131072);
//        int topology_executor_receive_buffer_size = 32768; // intra-worker messaging, default: 32768
//        int topology_transfer_buffer_size = 2048; // inter-worker messaging, default: 1000
//        int topology_producer_batch_size = 10; // intra-worker batch, default: 1
//        int topology_transfer_batch_size = 20; // inter-worker batch, default: 1
//        int topology_batch_flush_interval_millis = 10; // flush tuple creation ms, default: 1
        StormSubmitter.submitTopology("my-topology", config, builder.createTopology());
    }
}
