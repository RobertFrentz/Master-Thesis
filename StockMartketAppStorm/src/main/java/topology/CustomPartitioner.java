package topology;

import org.apache.kafka.common.TopicPartition;
import org.apache.storm.kafka.spout.subscription.ManualPartitioner;
import org.apache.storm.task.TopologyContext;

import java.util.*;

public class CustomPartitioner implements ManualPartitioner {
    @Override
    public Set<TopicPartition> getPartitionsForThisTask(List<TopicPartition> list, TopologyContext topologyContext) {
        return new HashSet<>(list);
    }
}
