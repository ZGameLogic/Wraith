package data.database.github.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubRepository extends JpaRepository<GitRepo, Long> {
    Optional<GitRepo> getByForumChannelId(Long forumChannelId);
}
