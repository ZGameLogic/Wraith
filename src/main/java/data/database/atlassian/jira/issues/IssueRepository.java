package data.database.atlassian.jira.issues;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface IssueRepository extends JpaRepository<Issue, Long> {
    @Query(value = "select * from jira_issues p where p.issue_key = :issueKey", nativeQuery = true)
    Optional<Issue> getIssueByKey(@Param("issueKey") String issueKey);
}
