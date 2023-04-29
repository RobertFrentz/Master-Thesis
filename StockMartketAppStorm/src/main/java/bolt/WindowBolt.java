package bolt;

import domain.Event;
import domain.EventResults;
import domain.SmoothingFactors;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.storm.state.KeyValueState;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseStatefulWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.*;

public class WindowBolt extends BaseStatefulWindowedBolt<KeyValueState<String, EventResults>> {
    private KeyValueState<String, EventResults> state;
    private OutputCollector collector;

    private static final Logger LOG = LogManager.getLogger(WindowBolt.class);
    //private AdminClient kafkaAdminClient;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        super.prepare(topoConf, context, collector);
        //this.kafkaAdminClient = createKafkaAdminClient();
        this.collector = collector;
    }

    @Override
    public void initState(KeyValueState<String, EventResults> state) {
        this.state = state;
        // ...
        // restore the state from the last saved state.
        // ...
    }

    @Override
    public void execute(TupleWindow window) {
        // iterate over tuples in the current window
        //TupleWindowIterImpl a = (TupleWindowIterImpl) window;
//        Iterator<Tuple> it = Iterators.unmodifiableIterator((Iterator)window.get());
        LOG.info("Window Processing Setup\n");

        Map<String, Event> eventMap = new HashMap<>();
        Map<String, Tuple> eventTuples = new HashMap<>();
        List<Tuple> tuples = window.get();

        LOG.info("Window Reducing Step");

        for (Tuple tuple : tuples) {
            Event event = (Event) tuple.getValueByField("value");
            String key = event.getId();

            if(eventMap.containsKey(key)){
                if(event.isAfter(eventMap.get(key))){
                    eventMap.put(key, event);
                    eventTuples.put(key, tuple);
                }
            } else {
                eventMap.put(key, event);
                eventTuples.put(key, tuple);
            }
        }

        LOG.info("Begin Window Processing\n");

        for (Event event : eventMap.values()) {

            LOG.info("Event received in window" + "\n" + event.toString() + "\n");
            LOG.info("Previous window event with id " + event.getId() + ": " + state.get(event.getId()) + "\n");

            LOG.info("Computing indicators for event " + event.getId() + "\n");

            EventResults previousResults = this.state.get(event.getId());
            EventResults results = new EventResults();

            results.setId(event.getId());
            results.setPrice(event.getLastTradePrice());

            computeEMA(event, results, previousResults);
            computeSMA(event, results, previousResults);

            LOG.info("Collecting the following results for event " + results + "\n");

            state.put(event.getId(), results);

            collector.emit(new Values(results.getId(), results, eventTuples.get(results.getId()).getMessageId()));
            collector.ack(eventTuples.get(results.getId()));
        }


        // possibly update any state to be maintained across windows

        // emit the results downstream

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        super.declareOutputFields(declarer);
        declarer.declare(new Fields("key", "value", "__kafka_offset"));
    }

    @Override
    public void cleanup() {
        super.cleanup();
        //kafkaAdminClient.close();
    }

    private void computeEMA(Event event, EventResults results, EventResults previousResults){
        System.out.println("Checking EMA previous results for event " + event.getId());

        if (previousResults != null) {
            System.out.println("Previous EMA results for event " + event.getId() + ": " + previousResults.getEMA38() + ", " + previousResults.getEMA100());

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
            System.out.println("Previous SMA result for event " + event.getId() + ": " + previousResults.getSMA2());
            results.setSMA2((previousResults.getPrice() + event.getLastTradePrice()) / 2);
        } else {
            results.setSMA2(event.getLastTradePrice());
        }

        System.out.println("Current Window SMA result for event is: " + results.getSMA2());
    }

    private double computeEmaForCurrentWindow(double currentWindowSymbolPrice, double previousWindowSymbolPrice, Integer smoothingFactor) {

        System.out.println("Current window last price for event: " + currentWindowSymbolPrice);
        System.out.println("Last window EMA for event: " + previousWindowSymbolPrice);

        double factor = smoothingFactor;

        double result = currentWindowSymbolPrice * (2 / (1 + factor)) + previousWindowSymbolPrice * (1 - (2 / (1 + factor)));

        System.out.println("Current Window Ema Result is: " + result);

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

        return "No Pattern Detected";
    }

//    private AdminClient createKafkaAdminClient(){
//        Properties properties = new Properties();
//        properties.put("bootstrap.servers", "kafka:9092");
//        return KafkaAdminClient.create(properties);
//    }
//
//    private boolean topicAlreadyExists(String topicName) throws ExecutionException, InterruptedException {
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
//    private double computeEmaForCurrentWindow(double currentWindowSymbolPrice, double previousWindowSymbolPrice, Integer smoothingFactor) {
//
//        System.out.println("Current window last price for event: " + currentWindowSymbolPrice);
//        System.out.println("Last window EMA for event: " + previousWindowSymbolPrice);
//
//        double factor = smoothingFactor;
//
//        double result = currentWindowSymbolPrice * (2 / (1 + factor)) + previousWindowSymbolPrice * (1 - (2 / (1 + factor)));
//
//        System.out.println("Current Window Ema Result is: " + result);
//
//        return result;
//    }
//
//    private String getBreakoutPattern(EventResults previousResults, EventResults currentResults){
//        final boolean isBearishBreakoutPattern = currentResults.getEMA38() < currentResults.getEMA100()
//                && previousResults.getEMA38() >= previousResults.getEMA100();
//
//        if(isBearishBreakoutPattern){
//            return "Bearish";
//        }
//
//        final boolean isBullishBreakoutPattern = currentResults.getEMA38() > currentResults.getEMA100()
//                && previousResults.getEMA38() <= previousResults.getEMA100();
//
//        if(isBullishBreakoutPattern)
//        {
//            return "Bullish";
//        }
//
//        return "No Pattern Detected";
//    }

}
