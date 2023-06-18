package bolt;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.esotericsoftware.minlog.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.Event;
import domain.EventResults;
import domain.JsonEventResultsSerializer;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WindowBolt extends BaseStatefulWindowedBolt<KeyValueState<String, String>> {
    public KeyValueState<String, String> state;
    private OutputCollector collector;

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        super.prepare(topoConf, context, collector);
        this.collector = collector;

    }

    @Override
    public void initState(KeyValueState<String, String> state) {
        this.state = state;
    }

    @Override
    public void execute(TupleWindow window) {

        //LOG.info("Window Processing Setup\n");

        Map<String, Event> eventMap = new HashMap<>();
        Map<String, Tuple> filteredTuples = new HashMap<>();
        List<Tuple> tuples = window.get();

        //LOG.info("Window Reducing Step");

        for (Tuple tuple : tuples) {
            Event event = (Event) tuple.getValueByField("value");
            String key = event.getId();

            if(eventMap.containsKey(key)){
                if(event.isAfter(eventMap.get(key))){
                    eventMap.put(key, event);
                    filteredTuples.put(key, tuple);
                } else {
                    collector.ack(tuple);
                }
            } else {
                eventMap.put(key, event);
                filteredTuples.put(key, tuple);
            }
        }

        for (Map.Entry<String, Tuple> entry : filteredTuples.entrySet()) {
            String key = entry.getKey();
            Tuple tuple = entry.getValue();
            Event event = eventMap.get(key);
            long tupleProcessingTime = (long)tuple.getValueByField("processingTime");
            //long msgId = (long)tuple.getValueByField("msgid");

            collector.emit(
                    tuple,
                    new Values(
                            event.getId(),
                            event,
                            tupleProcessingTime));
            //System.out.println("Key: " + key + ", Value: " + value);
            collector.ack(tuple);
        }

        //LOG.info("Begin Window Processing\n");

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



}
