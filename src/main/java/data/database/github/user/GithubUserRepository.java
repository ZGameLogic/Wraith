package data.database.github.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubUserRepository extends JpaRepository<GithubUser, Long> {
    Optional<GithubUser> getGithubUserByDiscordId(long discordId);
    Optional<GithubUser> getGithubUserByGithubId(long githubId);
}
