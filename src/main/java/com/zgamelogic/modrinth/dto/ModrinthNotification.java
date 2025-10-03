package com.zgamelogic.modrinth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class ModrinthNotification {
    private final String id;
    private final String type;
    private final String link;
    private final Boolean read;
    private final Body body;

    @Getter
    @ToString
    @AllArgsConstructor
    public static class Body {
        private final String type;
        @JsonProperty("project_id")
        private final String projectId;
        @JsonProperty("version_id")
        private final String versionId;
    }
}
