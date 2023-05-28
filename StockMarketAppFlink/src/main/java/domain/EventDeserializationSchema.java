package domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class EventDeserializationSchema implements DeserializationSchema<Event> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Event deserialize(byte[] bytes) throws IOException {
        String json = new String(bytes, StandardCharsets.UTF_8);
        //System.out.println("Here is a json " + json);
        Event event = mapper.readValue(json, Event.class);
        event.setProcessingTime(System.currentTimeMillis());
        return event;
    }

    @Override
    public boolean isEndOfStream(Event event) {
        return false;
    }

    @Override
    public TypeInformation<Event> getProducedType() {
        return TypeInformation.of(Event.class);
    }
}
