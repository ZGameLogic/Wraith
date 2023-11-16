package data.api.github;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.LinkedList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {
    @JsonProperty("html_url")
    private String htmlUrl;
    private long id;
    private String title;
    private String body;
    private LinkedList<Label> labels;
    private String state;
    private String action;
}
