package data.database.atlassian.jira.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query(value = "select * from jira_projects p where p.project_key = :projectKey", nativeQuery = true)
    Optional<Project> getProjectByKey(@Param("projectKey") String projectKey);

    @Query(value = "Select * FROM jira_projects INNER JOIN project_repositories pr on jira_projects.project_id = pr.project_id where pr.repository_id = :repoId", nativeQuery = true)
    Optional<Project> getJiraProjectByBitbucketRepoId(@Param("repoId") long repoId);

    @Query(value = "Select * FROM jira_projects INNER JOIN project_repositories pr on jira_projects.project_id = pr.project_id where pr.repo_slug = :repoSlug", nativeQuery = true)
    Optional<Project> getJiraProjectByBitbucketRepoSlug(@Param("repoSlug") String repoId);

//    @Query(value = "select * from project_repositories p where p.repo_slug = :repoSlug", nativeQuery = true)
//    Optional<BitbucketProject> getBitbucketProjectByRepoSlug(@Param("repoSlug") String repoSlug);
//
//    @Query(value = "select * from project_repositories p where p.repository_id = :repoId", nativeQuery = true)
//    Optional<BitbucketProject> getBitbucketProjectByRepoId(@Param("repoId") Long repoId);
}
