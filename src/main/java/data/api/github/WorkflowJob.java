package data.api.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.LinkedList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowJob {
    private long id;
    @JsonProperty("run_id")
    private long runId;
    @JsonProperty("workflow_name")
    private String workflowName;
    private String status;
    private String conclusion;
    @JsonProperty("started_at")
    private String startedAt;
    @JsonProperty("completed_at")
    private String completedAt;
    private String name;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("run_url")
    private String runUrl;
    private LinkedList<Step> steps;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private String name;
        private String status;
        private String conclusion;
        private int number;

        @JsonProperty("started_at")
        private String startedAt;
        @JsonProperty("completed_at")
        private String completedAt;
    }
}
