package data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import data.api.github.Repository;
import data.api.github.Sender;
import data.api.github.WorkflowJob;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowEvent {
    private String action;
    @JsonProperty("workflow_job")
    private WorkflowJob workflowJob;
    private Repository repository;
    private Sender sender;
}
