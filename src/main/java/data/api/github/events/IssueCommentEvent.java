package data.api.github.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import data.api.github.Comment;
import data.api.github.Issue;
import data.api.github.Repository;
import data.api.github.User;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IssueCommentEvent {
    private Issue issue;
    private Repository repository;
    private User sender;
    private Comment comment;
}
