package com.zgamelogic.devops.dto;

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

    /**
     * @return -1 for failure, 0 for running, 1 for success
     */
    public int getRunStatus(){
        for(WorkflowJob job: jobs) {
            for(WorkflowJob.Step step: job.getSteps()) {
                if (step.getStatus().equals("queued")) return 0;
                if (step.getConclusion() == null) return 0;
                if (step.getConclusion().equals("failed")) return -1;
            }
        }
        return 1;
    }
}
