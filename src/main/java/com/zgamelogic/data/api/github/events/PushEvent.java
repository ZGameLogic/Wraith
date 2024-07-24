package com.zgamelogic.data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zgamelogic.data.api.github.Repository;
import com.zgamelogic.data.api.github.User;
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
