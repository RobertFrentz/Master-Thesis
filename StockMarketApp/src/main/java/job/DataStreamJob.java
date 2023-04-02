package job;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Event;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
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

        KafkaSource<String> kafkaSource = KafkaSource.<String>builder()
                .setBootstrapServers("kafka:9092")
                .setTopics("trade-data")
                .setStartingOffsets(OffsetsInitializer.earliest())
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .build();

        KafkaSink<String> kafkaSink = KafkaSink.<String>builder()
                .setBootstrapServers("kafka:9092")
                .setRecordSerializer(KafkaRecordSerializationSchema.builder()
                        .setTopicSelector(new KafkaDynamicTopicSelector())
                        .setValueSerializationSchema(new SimpleStringSchema())
                        .build()
                )
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();

        ObjectMapper mapper = new ObjectMapper();

        DataStream<Event> dataStream = environment
                .fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), "Kafka Source")
                .map(json -> mapper.readValue(json, Event.class));

//        dataStream.map(value -> "Receiving from Kafka : " + value)
//                .print();

        DataStream<String> processedStream = dataStream.map(Event::toString);

        processedStream.sinkTo(kafkaSink);



//        DataStream<Event> processedEvents = events
//            .keyBy(Event::getId);
//            .reduce(new LastObservedPriceReduceFunction())
//            .keyBy(Event::getId)
//            .flatMap(new DeduplicationFunction());

//            Time windowSize = Time.seconds(10);
//
//            DataStream<Event> processedEvents = events
//                    .keyBy(Event::getId)
//                    .window(TumblingEventTimeWindows.of(windowSize))
//                    .reduce(new LastObservedPriceReduceFunction())
//                    .keyBy(Event::getId)
//                    .flatMap(new DeduplicationFunction());
//
//            processedEvents.print("Hello World");

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

/*
* In this example, we define a RichWindowFunction that computes a result for each window. We use a MapState to store the result of the previous window for each key.

In the open method, we initialize the MapState using a MapStateDescriptor. In the apply method, we compute the current result using the input data, and then retrieve the previous result from the MapState using the key. If there is a previous result, we combine it with the current result using a custom combineResults function, and then output the combined result. We then update the MapState with the combined result.

If there is no previous result, we output the current result as is, and then store the current result in the MapState for the next window.

Note that you can use a similar approach with the ListState operator if you want to store a list of previous results instead of just the last result.
* */


//public class EMAWindowFunction extends RichWindowFunction<Event, Result, String, TimeWindow> {
//    private MapState<String, Result> previousResults;
//
//    @Override
//    public void open(Configuration config) throws Exception {
//        // Initialize the MapState
//        MapStateDescriptor<String, Result> descriptor =
//                new MapStateDescriptor<>("previousResults", Types.STRING, Types.POJO(Result.class));
//        previousResults = getRuntimeContext().getMapState(descriptor);
//    }
//
//    @Override
//    public void apply(String key, TimeWindow window, Iterable<Event> input, Collector<Result> out) throws Exception {
//        Result currentResult = computeResult(input);
//
//        // Get the previous result from the MapState
//        Result previousResult = previousResults.get(key);
//
//        if (previousResult != null) {
//            // Combine the current result with the previous result
//            Result combinedResult = combineResults(previousResult, currentResult);
//            out.collect(combinedResult);
//
//            // Update the previous result in the MapState
//            previousResults.put(key, combinedResult);
//        } else {
//            // If there is no previous result, output the current result as is
//            out.collect(currentResult);
//
//            // Store the current result in the MapState for the next window
//            previousResults.put(key, currentResult);
//        }
//    }
//}
