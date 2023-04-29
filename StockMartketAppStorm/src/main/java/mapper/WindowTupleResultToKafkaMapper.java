package mapper;

import org.apache.storm.kafka.bolt.mapper.TupleToKafkaMapper;
import org.apache.storm.tuple.Tuple;
import org.checkerframework.checker.units.qual.K;

public class WindowTupleResultToKafkaMapper implements TupleToKafkaMapper<String, String> {
    @Override
    public String getKeyFromTuple(Tuple tuple) {
        return null;
    }

    @Override
    public String getMessageFromTuple(Tuple tuple) {
        return null;
    }
}
