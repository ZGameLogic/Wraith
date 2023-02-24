package data.api.atlassian.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@ToString
@NoArgsConstructor
public class JiraAPIIssue {

    @JsonProperty
    private Issue issue;

    @JsonProperty
    private Changelog changelog;

    @JsonProperty("issue_event_type_name")
    private String issueEventTypeName;


    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Issue {
        @JsonProperty
        private String key;

        @JsonProperty
        private Fields fields;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @NoArgsConstructor
        @Getter
        @ToString
        public static class Fields {
            @JsonProperty
            private Project project;

            @JsonIgnoreProperties(ignoreUnknown = true)
            @NoArgsConstructor
            @Getter
            @ToString
            public static class Project {
                @JsonProperty
                private String id;
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    @ToString
    public static class Changelog {
        @JsonProperty
        private Item[] items;
        @JsonProperty
        private String id;

        @JsonIgnoreProperties(ignoreUnknown = true)
        @NoArgsConstructor
        @Getter
        @ToString
        public static class Item {
            @JsonProperty
            private String field;
            @JsonProperty
            private String fromString;
            @JsonProperty
            private String toString;
        }
    }
}
