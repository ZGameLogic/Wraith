package data.database.github.Issue;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "github_issues")
public class GithubIssue {
    @Id
    private long id;
    private long forumPostId;
}
