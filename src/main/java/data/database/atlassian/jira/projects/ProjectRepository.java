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
}
