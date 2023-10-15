package data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import data.api.github.Label;
import data.api.github.Repository;
import data.deserializers.github.LabelEventDeserializer;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = LabelEventDeserializer.class)
public class LabelEvent {
    private Repository repository;
    private Label label;
    private String from;
}
