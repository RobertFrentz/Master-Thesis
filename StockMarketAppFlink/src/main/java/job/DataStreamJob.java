package job;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Event;
import domain.EventDeserializationSchema;
import helpers.CustomRichMapper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.kafka.common.TopicPartition;
import window.functions.IndicatorsWindowFunction;
import window.functions.LastObservedPriceReduceFunction;

import java.time.*;
import java.util.HashSet;
import java.util.Set;

public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        runJob(args);
    }

    private static void runJob(String[] args) throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        // End-to-End latency tracking
        //environment.getConfig().setLatencyTrackingInterval(100);

        JobOptions options = new JobOptions(args);
        ObjectMapper objectMapper = new ObjectMapper();

        System.out.println("Registering job with the following options");
        System.out.println(options);

        //environment.getConfig().setAutoWatermarkInterval(5L);

        //DataStreamSource<Event> events = environment
        //        .fromCollection(EventsGenerator.getDummyEvents());


        TopicPartition topicPartition = new TopicPartition("trade-data", 0);
        Set<TopicPartition> partitions = new HashSet<>();
        partitions.add(topicPartition);

        KafkaSource<Event> kafkaSource = KafkaSource.<Event>builder()
                .setBootstrapServers(options.brokerUrl)
                //.setTopics("trade-data")
                .setPartitions(partitions)
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new EventDeserializationSchema())
                .build();

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers(options.brokerUrl)
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        //.setTopic("flink-output-topic")
                        .setTopicSelector(new KafkaDynamicTopicSelector())
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .setKeySerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();



        DataStream<Event> events = environment
                .fromSource(
                        kafkaSource,
                        WatermarkStrategy
                                .forBoundedOutOfOrderness(Duration.ofSeconds(10)),
                                //.forMonotonousTimestamps()
                                //.withTimestampAssigner((event, timestamp) -> EventDateTimeHelper.getDateTimeInMillis(event)),
                        "Kafka Source")
                .setParallelism(options.kafkaSourceParallelism);

            Time windowSize = Time.seconds(options.windowTime);

        DataStream<String> processedEvents = events
                .keyBy(Event::getId)
                .window(TumblingEventTimeWindows.of(windowSize))
                .reduce(new LastObservedPriceReduceFunction(), new IndicatorsWindowFunction())
                .setParallelism(options.windowParallelism)
                .map(objectMapper::writeValueAsString);


        processedEvents
                .map(new CustomRichMapper())
                .sinkTo(kafkaSink).setParallelism(options.kafkaSinkParallelism);
        environment.execute("Stock Market Job");
    }
}




