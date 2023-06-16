package bolt;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Event;
import domain.EventResults;
import domain.JsonEventResultsSerializer;
import domain.SmoothingFactors;
import org.apache.storm.state.KeyValueState;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseStatefulWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowBolt extends BaseStatefulWindowedBolt<KeyValueState<String, String>> {

    private ObjectMapper objectMapper;
    public KeyValueState<String, String> state;
    private OutputCollector collector;

    private transient Histogram histogram;
    private transient Meter metric;
    private JsonEventResultsSerializer serializer;

    //private static final Logger LOG = LogManager.getLogger(WindowBolt.class);
    //private AdminClient kafkaAdminClient;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        super.prepare(topoConf, context, collector);
        this.objectMapper = new ObjectMapper();
        //this.kafkaAdminClient = createKafkaAdminClient();
        this.collector = collector;
        this.serializer = new JsonEventResultsSerializer();
        histogram = context.registerHistogram("windowLatencyInMilliseconds");
        metric = context.registerMeter("windowThroughput");
    }

    @Override
    public void initState(KeyValueState<String, String> state) {
        this.state = state;
    }

    @Override
    public void execute(TupleWindow window) {

        //LOG.info("Window Processing Setup\n");
        double eventsLatencyAverage = 0;
        long size = 0;

        Map<String, Event> eventMap = new HashMap<>();
        Map<String, Tuple> eventTuples = new HashMap<>();
        List<Tuple> tuples = window.get();

        //LOG.info("Window Reducing Step");

        for (Tuple tuple : tuples) {
            Event event = (Event) tuple.getValueByField("value");
            String key = event.getId();

            if(eventMap.containsKey(key)){
                if(event.isAfter(eventMap.get(key))){
                    eventMap.put(key, event);
                    eventTuples.put(key, tuple);
                } else {
                    collector.ack(tuple);
                }
            } else {
                eventMap.put(key, event);
                eventTuples.put(key, tuple);
            }
        }

        //LOG.info("Begin Window Processing\n");

        for (Event event : eventMap.values()) {

            String key = event.getId();
            Tuple tuple = eventTuples.get(key);

            //LOG.info("Computing indicators for event " + key + "\n");
            //LOG.info("Event received in window " + key + "\n");

            EventResults previousResults = null;
            try {
                String json = state.get(key);
                if(json != null){
                    previousResults = serializer.deserialize(json);
                    //LOG.info("Previous window event with id " + key + ": " + previousResults.toString() + "\n");
                }

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }


            EventResults results = new EventResults();

            results.setId(key);
            results.setPrice(event.getLastTradePrice());
            results.setTime(event.getTimeOfLastUpdate());
            results.setDate(event.getDateOfLastTrade());

            computeEMA(event, results, previousResults);
            computeSMA(event, results, previousResults);

            //LOG.info("Collecting the following results for event " + results + "\n");

            try {
                String json = serializer.serialize(results);
                //LOG.info("Serializing current results " + json + "\n");
                state.put(key, json);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

//            try {
//                if(!topicAlreadyExists(key)){
//                    createNewTopic(key);
//                }
//            } catch (ExecutionException | InterruptedException e) {
//                throw new RuntimeException(e);
//            }

            long tupleProcessingTime = (long)tuple.getValueByField("processingTime");
            try {
                collector.emit(
                        tuple,
                        new Values(
                                results.getId(),
                                objectMapper.writeValueAsString(results),
                                tupleProcessingTime));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            collector.ack(tuple);
            if(tupleProcessingTime != 0){
                long windowEventLatency = System.currentTimeMillis() - tupleProcessingTime;
                eventsLatencyAverage = (eventsLatencyAverage * size + windowEventLatency) / (size + 1);
                size++;
            }
        }
        if(eventsLatencyAverage != 0 && tuples.size() > 0){
            histogram.update((long)eventsLatencyAverage);
            metric.mark((long)(tuples.size()/(eventsLatencyAverage/1000)));
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        super.declareOutputFields(declarer);
        declarer.declare(new Fields("key", "value", "processingTime"));
    }



    @Override
    public void cleanup() {
        super.cleanup();

        //kafkaAdminClient.close();
    }

    private void computeEMA(Event event, EventResults results, EventResults previousResults){
        //LOG.info("Checking EMA previous results for event " + event.getId() + "\n");

        if (previousResults != null) {
            //LOG.info("Previous EMA results for event " + event.getId() + ": " + previousResults.getEMA38() + ", " + previousResults.getEMA100() + "\n");

            results.setEMA38(computeEmaForCurrentWindow(event.getLastTradePrice(), previousResults.getEMA38(), SmoothingFactors.LOW));
            results.setEMA100(computeEmaForCurrentWindow(event.getLastTradePrice(), previousResults.getEMA100(), SmoothingFactors.HIGH));
            results.setBreakoutPattern(getBreakoutPattern(results, previousResults));
        } else {
            results.setEMA38(event.getLastTradePrice());
            results.setEMA100(event.getLastTradePrice());
            results.setBreakoutPattern("No pattern Detected");
        }
    }

    private void computeSMA(Event event,  EventResults results, EventResults previousResults) {

        if (previousResults != null) {
            //LOG.info("Previous SMA result for event " + event.getId() + ": " + previousResults.getSMA2() + "\n");
            results.setSMA2((previousResults.getPrice() + event.getLastTradePrice()) / 2);
        } else {
            results.setSMA2(event.getLastTradePrice());
        }

        //LOG.info("Current Window SMA result for event is: " + results.getSMA2() + "\n");
    }

    private double computeEmaForCurrentWindow(double currentWindowSymbolPrice, double previousWindowSymbolPrice, Integer smoothingFactor) {

        //LOG.info("Current window last price for event: " + currentWindowSymbolPrice + "\n");
        //LOG.info("Last window EMA for event: " + previousWindowSymbolPrice + "\n");

        double factor = smoothingFactor;

        double result = currentWindowSymbolPrice * (2 / (1 + factor)) + previousWindowSymbolPrice * (1 - (2 / (1 + factor)));

        //LOG.info("Current Window Ema Result is: " + result + "\n");

        return result;
    }

    private String getBreakoutPattern(EventResults previousResults, EventResults currentResults){
        final boolean isBearishBreakoutPattern = currentResults.getEMA38() < currentResults.getEMA100()
                && previousResults.getEMA38() >= previousResults.getEMA100();

        if(isBearishBreakoutPattern){
            return "Bearish";
        }

        final boolean isBullishBreakoutPattern = currentResults.getEMA38() > currentResults.getEMA100()
                && previousResults.getEMA38() <= previousResults.getEMA100();

        if(isBullishBreakoutPattern)
        {
            return "Bullish";
        }

        return previousResults.getBreakoutPattern() == null ? "No Pattern Detected" : previousResults.getBreakoutPattern();
    }

//    private AdminClient createKafkaAdminClient(){
//        Properties properties = new Properties();
//        properties.put("bootstrap.servers", "kafka:9092");
//        return KafkaAdminClient.create(properties);
//    }
//
//    private boolean topicAlreadyExists(String topicName) throws InterruptedException, ExecutionException {
//        ListTopicsResult topicsResult = kafkaAdminClient.listTopics();
//        return topicsResult.names().get().contains(topicName);
//    }
//
//    private void createNewTopic(String topicName){
//        int numPartitions = 1;
//        short replicationFactor = 1;
//        NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
//        kafkaAdminClient.createTopics(Collections.singletonList(newTopic));
//    }

}
