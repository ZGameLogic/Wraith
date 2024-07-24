package com.zgamelogic.data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zgamelogic.data.api.github.Repository;
import com.zgamelogic.data.api.github.User;
import com.zgamelogic.data.api.github.WorkflowJob;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkflowEvent {
    private String action;
    @JsonProperty("workflow_job")
    private WorkflowJob workflowJob;
    private Repository repository;
    private User sender;
}
