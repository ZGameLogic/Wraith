package data.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.LinkedList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowRun {
    @JsonProperty("total_count")
    private int total;
    private LinkedList<WorkflowJob> jobs;
}
