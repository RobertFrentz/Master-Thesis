package job;

import org.apache.flink.connector.kafka.sink.TopicSelector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KafkaDynamicTopicSelector implements TopicSelector<String> {
    @Override
    public String apply(String event) {
        Pattern pattern = Pattern.compile("\"id\":\"(.*?)\"");
        Matcher matcher = pattern.matcher(event);

        String topicName = "unidentified-processed-data";
        if (matcher.find() && (matcher.group(1).equals("IEBBB.FR") || matcher.group(1).equals("IUIFL.FR"))) {
            topicName = matcher.group(1);
        }

        return topicName;
    }
}