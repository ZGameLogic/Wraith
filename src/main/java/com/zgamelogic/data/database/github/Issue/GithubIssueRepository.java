package com.zgamelogic.data.database.github.Issue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GithubIssueRepository extends JpaRepository<GithubIssue, Long> {
    Optional<GithubIssue> getGithubIssueByForumPostId(long forumPostId);
}
