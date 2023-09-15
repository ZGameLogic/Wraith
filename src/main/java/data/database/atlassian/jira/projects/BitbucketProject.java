package data.database.atlassian.jira.projects;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Getter
@Setter
@Embeddable
public class BitbucketProject {
    private Long repositoryId;
    private Long channelId;
    private Long pullRequestChannelId;
    private String projectSlug;
    private String repoSlug;
}
