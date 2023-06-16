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

    List<Values> values = new ArrayList<>();

    //private transient Meter meter;

    @Override
    public void open(Map<String, Object> config, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;
        values = new ArrayList<>();

        // Set the configuration properties for the KafkaConsumer
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "storm-consumer5-custom");
//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000000000);
//        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 1000000000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonEventDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Create a KafkaConsumer instance
        kafkaConsumer = new KafkaConsumer<>(props);
        String topic = "trade-data";
        TopicPartition topicPartition = new TopicPartition(topic, 0);
        Set<TopicPartition> partitions = new HashSet<>(kafkaConsumer.assignment());
        partitions.add(topicPartition);
        kafkaConsumer.assign(partitions);
        kafkaConsumer.seekToBeginning(Collections.singletonList(topicPartition));
        //meter = context.registerMeter("spoutThroughput");
    }

    @Override
    public void nextTuple() {
        if(values.isEmpty()){
            ConsumerRecords<String, Event> records = kafkaConsumer.poll(Duration.ofMillis(0));
            //this.meter.mark(records.count());
            for (ConsumerRecord<String, Event> record : records) {
                // Emit the consumed record as a tuple
                values.add(new Values(record.key(), record.value(), record.timestamp(), System.currentTimeMillis(), getMessageId()));
            }
            if(!values.isEmpty()){
                collector.emit(values.get(0));
                values.remove(0);
            }
        } else {
            collector.emit(values.get(0));
            values.remove(0);
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