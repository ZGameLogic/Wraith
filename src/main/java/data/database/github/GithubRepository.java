package data.database.github;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GithubRepository extends JpaRepository<GitRepo, Long> {
}
