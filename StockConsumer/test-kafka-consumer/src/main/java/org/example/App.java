package org.example;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hello world!
 *
 */
public class App 
{
    public static AtomicLong recordsRead = new AtomicLong(0);
    public static void main( String[] args )
    {
        KafkaConsumer<String, String> kafkaConsumer;

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test1-consumer1-custom");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000000000);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 1000000000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Create a KafkaConsumer instance
        kafkaConsumer = new KafkaConsumer<>(props);
        String topic = "trade-data";
        TopicPartition topicPartition = new TopicPartition(topic, 0);
        Set<TopicPartition> partitions = new HashSet<>(kafkaConsumer.assignment());
        partitions.add(topicPartition);
        kafkaConsumer.assign(partitions);
        kafkaConsumer.seekToEnd(Collections.singletonList(topicPartition));

        new Thread(() -> {
            while(true){
                try {
                    Thread.sleep(1000*30);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(recordsRead.get());
            }
        }).start();

        while(true){
            ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(100));
            long x = recordsRead.get();
            recordsRead.set(x + records.count());
            //this.meter.mark(records.count());
        }
    }
}
