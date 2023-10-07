package data.database.github;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "git_repositories")
@NoArgsConstructor
public class GitRepo {

    @Id
    private Long id;

    private Long categoryId;
    private Long generalId;
    private Long pullRequestId;
    private Long forumChannelId;

    private String repoUrl;
    private String repoName;

    public GitRepo(Long id, String url, String name){
        this.id = id;
        repoUrl = url;
        repoName = name;
    }
}
