package org.example;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StockEventsKafkaProducer {
    private static KafkaConsumer<String, String> kafkaConsumer;

    private static final ConcurrentHashMap<String, Long> benchmark = new ConcurrentHashMap<>();

    private static final AtomicInteger throughput = new AtomicInteger(0);

    private static final AtomicReference<Double> latency = new AtomicReference<>(0.0);

    private static final ObjectMapper mapper = new ObjectMapper();

    public StockEventsKafkaProducer() throws IOException {
    }

    public static void main(String[] args) {

        //test();
        int delay = Integer.parseInt(args[0]);

        String topicName = "trade-data";
        String csvFile = "C:/Users/Robert/Downloads/output08-copy.csv";
        String line = "";
        String csvSplitBy = ",";

        Properties props = new Properties();
//        props.put("bootstrap.servers", "kafka:9092");
        props.put("bootstrap.servers", "localhost:9094");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        includeBenchmark();

        try (
                Producer<String, String> producer = new KafkaProducer<>(props);
        )
        {
            ObjectMapper mapper = new ObjectMapper();

            try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
                boolean firstLineRead = false;
                int lineNumber = 0;
                while ((line = br.readLine()) != null) {
                    if(!firstLineRead){
                        firstLineRead = true;
                        continue;
                    }
                    lineNumber++;
                    //System.out.println(lineNumber);
                    String[] data = line.split(csvSplitBy);

                    //System.out.println(Arrays.toWString(data));

                    Event event = new Event(data[0], data[1], Double.parseDouble(data[2]), data[3], data[4]);

                    String key = event.getId();
                    String value = mapper.writeValueAsString(event);

                    long timestamp = EventDateTimeHelper.getDateTimeInMillis(event);

                    ProducerRecord<String, String> record = new ProducerRecord<>(topicName, 0, timestamp, key, value);
                    //ProducerRecord<String, String> record2 = new ProducerRecord<>(topicName, 1, timestamp, key, value);
                    //ProducerRecord<String, String> record = new ProducerRecord<>(topicName, key, value);
                    producer.send(record);
                    //producer.send(record2);
                    String regex = "^\\d\\d:(04|09|14|19|24|29|34|39|44|49|54|59).*";
                    if(event.getTimeOfLastUpdate().matches(regex)){
                        //System.out.println(event.id + "  " + event.timeOfLastUpdate);
                        benchmark.put(event.id + event.timeOfLastUpdate, System.currentTimeMillis());
                    }


                    if(delay != 0){
                        Thread.sleep(delay);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Finished reading");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void includeBenchmark(){
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094"); // Replace with your Kafka broker addresses
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "benchmark-group1");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        kafkaConsumer = new KafkaConsumer<>(props);
        String topic = "flink-output-topic";
        //String topic1 = "storm-output-topic";
        TopicPartition topicPartition = new TopicPartition(topic, 0);
        //TopicPartition topicPartition1 = new TopicPartition(topic1, 0);
        Set<TopicPartition> partitions = new HashSet<>();
        partitions.add(topicPartition);
        //partitions.add(topicPartition1);
        kafkaConsumer.assign(partitions);
        kafkaConsumer.seekToEnd(Collections.singletonList(topicPartition));

        // Create a metric registry
        MetricRegistry registry = new MetricRegistry();

        // Create a Graphite instance with the Graphite server's address
        Graphite graphite = new Graphite(new InetSocketAddress("localhost", 2003));

        // Create a Graphite reporter and configure it
        GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
                .prefixedWith("end_to_end_latency.metrics")
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter((name, metric) -> true)
                .build(graphite);

        // Start the reporter
        reporter.start(30, TimeUnit.SECONDS);

        // Register and use a metric with a single long value
        Gauge<Double> gauge = latency::get;
        registry.register("gauge_flink_latency", gauge);

        Gauge<Integer> gauge2 = throughput::get;
        registry.register("gauge_flink_throughput", gauge2);

        new Thread(() -> {
            try {
                while (true) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(Duration.ofMillis(1000));
//                    int countRecords = count.get() + records.count();
//                    count.set(count.get() + 1);
//                    throughput.set(throughput.get() + (double)(records.count()/countRecords));
                    throughput.set(records.count());
                    for (ConsumerRecord<String, String> record : records) {
                        String key = getIdFrom(record.value());
                        Long millis;

                        if(benchmark.containsKey(key)){
                            System.out.println("Found " + key);
                            millis = benchmark.get(key);
                        } else{
                            continue;
                        }

                        double timePassedInSeconds = (double)(System.currentTimeMillis() - millis) / 1000;
                        //System.out.println("With timestamp " + timePassedInSeconds);
                        latency.set(timePassedInSeconds);
                        benchmark.remove(key);
                    }

                }
            } catch (Exception e) {
                reporter.stop();
                e.printStackTrace();
            }
        }).start();
    }

    private static String getIdFrom(String json) throws JsonProcessingException {
        EventResults eventResults = mapper.readValue(json, EventResults.class);
        return eventResults.getId() + eventResults.getTimeStamp();
    }

//    private static void test(){
//        MetricRegistry registry = new MetricRegistry();
//
//        // Create a Graphite instance with the Graphite server's address
//        Graphite graphite = new Graphite(new InetSocketAddress("localhost", 2003));
//
//        // Create a Graphite reporter and configure it
//        GraphiteReporter reporter = GraphiteReporter.forRegistry(registry)
//                .prefixedWith("latency1.metrics")
//                .convertRatesTo(TimeUnit.SECONDS)
//                .convertDurationsTo(TimeUnit.MILLISECONDS)
//                .filter((name, metric) -> true)
//                .build(graphite);
//
//        // Start the reporter
//        reporter.start(1, TimeUnit.MINUTES);
//
//        // Register and use a metric with a single long value
//        registry.counter("my_latency_counter").inc(42);
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        // Stop the reporter
//        reporter.stop();
//    }
}
