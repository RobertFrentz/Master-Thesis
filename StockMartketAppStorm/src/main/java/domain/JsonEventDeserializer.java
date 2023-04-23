package domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonEventDeserializer implements Deserializer<Event> {

    private final ObjectMapper objectMapper;

    public JsonEventDeserializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public Event deserialize(String topic, byte[] data) {
        try {
            String json = new String(data, StandardCharsets.UTF_8);
            return objectMapper.readValue(json, Event.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON data", e);
        }
    }

    @Override
    public void close() {}
}
