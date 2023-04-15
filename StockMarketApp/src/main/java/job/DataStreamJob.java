package job;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Event;
import domain.EventDeserializationSchema;
import helpers.EventDateTimeHelper;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FilterFunction;
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
import window.functions.DeduplicationFunction;
import window.functions.EMAWindowFunction;
import window.functions.LastObservedPriceReduceFunction;

import java.time.*;

//TODO TEST WITH NEW TIMESTAMPS THEN TEST WITH TOPIC DYNAMIC CREATING THEN TEST QUERIES

public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        runJob();
        //processDummyEvents();
        //sampleCode();
    }

    private static void runJob() throws Exception {
        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();

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
                                .setTopic("trade-trade")
                        //.setTopicSelector(new KafkaDynamicTopicSelector())
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();



        DataStream<Event> events = environment
                .fromSource(
                        kafkaSource,
                        WatermarkStrategy
                                .<Event>forBoundedOutOfOrderness(Duration.ofSeconds(5))
                                .withTimestampAssigner((event, timestamp) -> {
                                    return EventDateTimeHelper.getDateTimeInMillis(event);
                                    /*ZonedDateTime epoch = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());
                                    ZonedDateTime dateTime = ZonedDateTime.of(epoch.toLocalDate(), time, epoch.getZone());
                                    return dateTime.toInstant().toEpochMilli();*/
                        }),
                        "Kafka Source");

//        dataStream.map(value -> "Receiving from Kafka : " + value)
//                .print();

//        DataStream<String> processedStream = events.map(Event::toString);
//
//          processedStream.sinkTo(kafkaSink);



//        DataStream<Event> processedEvents = events
//            .keyBy(Event::getId);
//            .reduce(new LastObservedPriceReduceFunction())
//            .keyBy(Event::getId)
//            .flatMap(new DeduplicationFunction());

            Time windowSize = Time.seconds(20);

//            DataStream<String> processedEvents = events
//                    .keyBy(Event::getId)
//                    .window(TumblingEventTimeWindows.of(windowSize))
//                    .reduce(new LastObservedPriceReduceFunction(), new EMAWindowFunction())
//                    .map(emas -> emas.get(0) + ", " + emas.get(1));
        DataStream<String> processedEvents = events
                .keyBy(Event::getId)
                .window(TumblingEventTimeWindows.of(windowSize))
                .reduce(new LastObservedPriceReduceFunction())
                .map(Event::toString);
//
//            processedEvents.print("Hello World");
//
        processedEvents.sinkTo(kafkaSink);
        environment.execute("Kafka Test");
    }
}
    /*private static void processDummyEvents() throws Exception {
        try (StreamExecutionEnvironment environment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration())) {
            //environment.getConfig().setAutoWatermarkInterval(5L);

            //DataStreamSource<Event> events = environment
            //        .fromCollection(EventsGenerator.getDummyEvents());

            DataStream<Event> events = environment
                    .addSource(new RandomEventSource())
                    .assignTimestampsAndWatermarks(
                            WatermarkStrategy.<Event>forBoundedOutOfOrderness(Duration.ofSeconds(10))
                                    .withTimestampAssigner((event, timestamp) -> {
                                        LocalTime time = LocalTime.parse(event.getTimeOfLastUpdate());
                                        ZonedDateTime epoch = ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());
                                        ZonedDateTime dateTime = ZonedDateTime.of(epoch.toLocalDate(), time, epoch.getZone());
                                        return dateTime.toInstant().toEpochMilli();
                                    })
                                    .withIdleness(Duration.ofMinutes(1)));

            //DataStream<Event> processedEvents = events
            //.keyBy(Event::getId);
            //.reduce(new LastObservedPriceReduceFunction())
            //.keyBy(Event::getId)
            //.flatMap(new DeduplicationFunction());

            Time windowSize = Time.seconds(10);

            DataStream<Event> processedEvents = events
                    .keyBy(Event::getId)
                    .window(TumblingEventTimeWindows.of(windowSize))
                    .reduce(new LastObservedPriceReduceFunction())
                    .keyBy(Event::getId)
                    .flatMap(new DeduplicationFunction());

            processedEvents.print("Hello World");

            environment.execute("Dummy Events Processing");
        }
    }

    private void sampleCode() throws Exception {
        try (StreamExecutionEnvironment environment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration())) {

            DataStreamSource<Long> stream = environment.fromSequence(1, 10);
            stream.filter(number -> number > 2).print("Hello World");

            String filePath = "path/to/your/file";
            DataStream<String> inputStream = environment.readTextFile(filePath);

            List<Event> events = new ArrayList<>();
            events.add(new Event());
            DataStream<Event> dataStream = environment.fromCollection(events);

//
//			// define the tumbling window size
//			Time windowSize = Time.seconds(10);
//
//			// apply tumbling window on the data stream
//			DataStream<String> windowedStream = inputStream
//					.window(TumblingProcessingTimeWindows.of(windowSize))
//					.process(new MyProcessWindowFunction());
//
//			// print the processed data
//			windowedStream.print();

            // execute the Flink job
            environment.execute("File Tumbling Window");
        }
    }
}*/




