package helper;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.storm.kafka.spout.RecordTranslator;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.List;
import java.util.UUID;

public class KafkaRecordTranslator implements RecordTranslator<String, String> {

    @Override
    public List<Object> apply(ConsumerRecord<String, String> consumerRecord) {
        return new Values(consumerRecord.key(), consumerRecord.value(), getMessageId());
    }

    @Override
    public Fields getFieldsFor(String s) {
        return new Fields("key", "value", "msgid");
    }

    private Long getMessageId(){
        return UUID.randomUUID().getMostSignificantBits() + System.currentTimeMillis();
    }
}
