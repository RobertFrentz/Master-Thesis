package domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonEventResultsSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String serialize(EventResults event) throws JsonProcessingException {
        return objectMapper.writeValueAsString(event);
    }

    public EventResults deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, EventResults.class);
    }
}
