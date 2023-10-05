package data.database.github;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "git_repositories")
public class GitRepo {

    @Id
    private Long id;

    private Long categoryId;
    private Long generalId;
    private Long pullRequestId;
    private Long forumChannelId;

    private String repoUrl;
    private String repoName;
}
