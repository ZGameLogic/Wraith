package com.zgamelogic.devops.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zgamelogic.devops.dto.Repository;
import com.zgamelogic.devops.dto.User;
import com.zgamelogic.devops.dto.WorkflowJob;
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
