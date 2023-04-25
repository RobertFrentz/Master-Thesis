package domain;

import bolt.ProcessingBolt;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class JsonEventDeserializer implements Deserializer<Event> {

    private final ObjectMapper objectMapper;

    private static final Logger LOG = LogManager.getLogger(JsonEventDeserializer.class);

    public JsonEventDeserializer() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {}

    @Override
    public Event deserialize(String topic, byte[] data) {
        try {
            String json = new String(data, StandardCharsets.UTF_8);
            LOG.info("Json: " + json);
            return objectMapper.readValue(json, Event.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON data", e);
        }
    }

    @Override
    public void close() {}
}
