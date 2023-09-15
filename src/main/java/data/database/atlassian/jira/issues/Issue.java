package data.database.atlassian.jira.issues;

import lombok.*;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
@Table(name = "jira_issues")
public class Issue {

    @Id
    private long issueId;
    private String issueKey;
    private Long threadChannelId;

}
