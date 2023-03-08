package data.api.atlassian.jira;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraAPIIssue {

    private Issue issue;
    private Changelog changelog;
    @JsonProperty("issue_event_type_name")
    private String issueEventTypeName;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issue {
        private String key;
        private Fields fields;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Fields {
            private Project project;
            private String summary;

            @Getter
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Project {
                private String id;
            }
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Changelog {
        private Item[] items;
        private String id;

        @Getter
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Item {
            private String field;
            private String fromString;
            private String toString;
        }
    }
}
