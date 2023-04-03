package data.database.atlassian.jira.projects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Embeddable;

@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class BitbucketProject {

    private Long repositoryId;
    private Long channelId;
    private Long pullRequestChannelId;
    private Long recentPrMessageId;
    private String projectSlug;
    private String repoSlug;

}
