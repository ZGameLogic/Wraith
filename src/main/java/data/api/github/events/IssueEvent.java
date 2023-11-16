package data.api.github.events;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import data.api.github.Issue;
import data.api.github.Repository;
import data.api.github.Sender;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueEvent {
    private Issue issue;
    private Repository repository;
    private Sender sender;
}
