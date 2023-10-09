package data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import data.api.github.Label;
import data.api.github.Repository;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LabelEvent {
    private Repository repository;
    private Label label;
}
