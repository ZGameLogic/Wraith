package data.database.atlassian.jira.issues;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

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
