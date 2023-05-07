package job;

import domain.Event;
import domain.EventDeserializationSchema;
import domain.EventResults;
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
import window.functions.IndicatorsWindowFunction;
import window.functions.LastObservedPriceReduceFunction;

import java.time.*;

//TODO TEST WITH NEW TIMESTAMPS THEN TEST WITH TOPIC DYNAMIC CREATING THEN TEST QUERIES

public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        runJob(args);
    }

    private static void runJob(String[] args) throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

        JobOptions options = new JobOptions(args);

        System.out.println("Registering job with the following options");
        System.out.println(options);

        //environment.getConfig().setAutoWatermarkInterval(5L);

        //DataStreamSource<Event> events = environment
        //        .fromCollection(EventsGenerator.getDummyEvents());

        KafkaSource<Event> kafkaSource = KafkaSource.<Event>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("trade-data")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new EventDeserializationSchema())
                .build();

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("kafka:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                                //.setTopic("flink-output-topic")
                        .setTopicSelector(new KafkaDynamicTopicSelector())
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();



        DataStream<Event> events = environment
                .fromSource(
                        kafkaSource,
                        WatermarkStrategy
                                .forBoundedOutOfOrderness(Duration.ofSeconds(5)),
//                                .withTimestampAssigner((event, timestamp) -> {
//                                    return EventDateTimeHelper.getDateTimeInMillis(event);
//                                    /*ZonedDateTime epoch = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());
//                                    ZonedDateTime dateTime = ZonedDateTime.of(epoch.toLocalDate(), time, epoch.getZone());
//                                    return dateTime.toInstant().toEpochMilli();*/
//                        }),
                        "Kafka Source")
                .setParallelism(options.kafkaSourceParallelism);

            Time windowSize = Time.seconds(options.windowTime);

        DataStream<String> processedEvents = events
                .keyBy(Event::getId)
                .window(TumblingEventTimeWindows.of(windowSize))
                .reduce(new LastObservedPriceReduceFunction(), new IndicatorsWindowFunction())
                .map(EventResults::toString)
                .setParallelism(options.windowParallelism);

        processedEvents.sinkTo(kafkaSink).setParallelism(options.kafkaSinkParallelism);
        environment.execute("Stock Market Job");
    }
}




