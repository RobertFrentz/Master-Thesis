package spout;

import domain.Event;
import domain.JsonEventDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.time.Duration;
import java.util.*;

public class KafkaSpoutCustom extends BaseRichSpout {
    private SpoutOutputCollector collector;
    private KafkaConsumer<String, Event> kafkaConsumer;

    @Override
    public void open(Map<String, Object> config, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;

        // Set the configuration properties for the KafkaConsumer
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "storm-consumer-custom");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonEventDeserializer.class);

        // Create a KafkaConsumer instance
        kafkaConsumer = new KafkaConsumer<>(props);
        String topic = "trade-data";
        TopicPartition topicPartition = new TopicPartition(topic, 0);
        Set<TopicPartition> partitions = new HashSet<>(kafkaConsumer.assignment());
        partitions.add(topicPartition);
        kafkaConsumer.assign(partitions);
        kafkaConsumer.seekToEnd(Collections.singletonList(topicPartition));
    }

    @Override
    public void nextTuple() {
        ConsumerRecords<String, Event> records = kafkaConsumer.poll(Duration.ofMillis(1000));
        for (ConsumerRecord<String, Event> record : records) {
            // Emit the consumed record as a tuple
            collector.emit(new Values(record.key(), record.value(), record.timestamp(), System.currentTimeMillis(), getMessageId()));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("key", "value", "timeStamp", "processingTime","msgid"));
    }

    @Override
    public void close() {
        kafkaConsumer.close();
    }

    private Long getMessageId(){
        return UUID.randomUUID().getMostSignificantBits() + System.currentTimeMillis();
    }
}