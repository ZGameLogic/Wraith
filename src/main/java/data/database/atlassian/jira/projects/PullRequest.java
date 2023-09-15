package data.database.atlassian.jira.projects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Embeddable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PullRequest {

    private Long recentPrMessageId;
    private String fromBranch;
    private String repoSlug;

}
