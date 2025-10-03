package com.zgamelogic.devops.dto;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zgamelogic.devops.dto.events.LabelEvent;

import java.io.IOException;

public class LabelEventDeserializer extends JsonDeserializer<LabelEvent> {

    @Override
    public LabelEvent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        LabelEvent event = new LabelEvent();
        ObjectMapper om = (ObjectMapper) jsonParser.getCodec();
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Label label = om.treeToValue(node.get("label"), Label.class);
        Repository repo = om.treeToValue(node.get("repository"), Repository.class);

        event.setRepository(repo);
        event.setLabel(label);
        if(node.has("changes")){
            event.setFrom(node.get("changes").get("name").get("from").asText());
        }
        return event;
    }
}
