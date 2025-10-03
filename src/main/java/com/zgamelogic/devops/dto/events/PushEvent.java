package com.zgamelogic.devops.dto.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zgamelogic.devops.dto.Repository;
import com.zgamelogic.devops.dto.User;
import lombok.Data;

import java.util.LinkedList;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushEvent {
    private Repository repository;
    private LinkedList<Commit> commits;

    @JsonProperty("head_commit")
    private Commit headCommit;

    private User sender;
    private String ref;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Commit {
        private String message;
        private String id;
        private LinkedList<String> added;
        private LinkedList<String> removed;
        private LinkedList<String> modified;
        private String url;
    }
}
