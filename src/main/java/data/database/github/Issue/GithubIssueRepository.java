package data.database.github.Issue;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubIssueRepository extends JpaRepository<GithubIssue, Long> {
}
