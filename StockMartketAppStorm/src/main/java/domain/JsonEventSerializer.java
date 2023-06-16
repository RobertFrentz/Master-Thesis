package domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class JsonEventSerializer implements Serializer<Event> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String serialize(EventResults event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    public EventResults deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, EventResults.class);
    }

    @Override
    public byte[] serialize(String s, Event event) {
        try {
            return objectMapper.writeValueAsString(event).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
