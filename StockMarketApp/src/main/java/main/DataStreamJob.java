package main;

import domain.Event;
import domain.generators.EventsGenerator;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import window.functions.Deduplicator;
import window.functions.LastObservedPriceReduceFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Skeleton for a Flink DataStream Job.
 *
 * <p>For a tutorial how to write a Flink application, check the
 * tutorials and examples on the <a href="https://flink.apache.org">Flink Website</a>.
 *
 * <p>To package your application into a JAR file for execution, run
 * 'mvn clean package' on the command line.
 *
 * <p>If you change the name of the main class (with the public static void main(String[] args))
 * method, change the respective entry in the POM.xml file (simply search for 'mainClass').
 */
public class DataStreamJob {

    public static void main(String[] args) throws Exception {
        processDummyEvents();
        //sampleCode();
    }

    private static void processDummyEvents() throws Exception {
        try (StreamExecutionEnvironment environment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(new Configuration())) {

            //environment.getConfig().setAutoWatermarkInterval(5L);

            DataStreamSource<Event> events = environment
                    .fromCollection(EventsGenerator.getDummyEvents());

            DataStream<Event> processedEvents = events
                    .keyBy(Event::getId)
                    .reduce(new LastObservedPriceReduceFunction())
                    .keyBy(Event::getId)
                    .flatMap(new Deduplicator());

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
}

