package data.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Label {
    private long id;
    private String name;
    private String color;
    private String description;
}