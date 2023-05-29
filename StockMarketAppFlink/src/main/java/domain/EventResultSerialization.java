package domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.serialization.SerializationSchema;

import java.nio.charset.StandardCharsets;

public class EventResultSerialization implements SerializationSchema<String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public byte[] serialize(String json) {
        JsonNode jsonNode;
        String modifiedJsonString;
        try {
            jsonNode = objectMapper.readTree(json);
            ((ObjectNode) jsonNode).remove("processingTime");
            modifiedJsonString = objectMapper.writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return modifiedJsonString.getBytes(StandardCharsets.UTF_8);
    }
}
